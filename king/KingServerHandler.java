package com.megu.neferpitou.king;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.config.PitouConfig;
import com.megu.neferpitou.network.PitouNetwork;
import com.megu.neferpitou.util.ServerGlowManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Logica do Rei (lado servidor):
 *  - destaca mobs hostis perto do Rei (glow via ServerGlowManager);
 *  - quando um mob esta agredindo o Rei, da buff de velocidade + velocidade de ataque
 *    a Pitou, escalado pela vida do Rei (vida cheia = minimo; <= piso = maximo);
 *  - impede a Pitou de ferir o Rei;
 *  - conta as mortes do Rei e mede ha quanto tempo a Pitou esta longe;
 *  - calcula o nivel de paranoia (0-4) e sincroniza para o client.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class KingServerHandler {

    private KingServerHandler() {}

    private static final UUID KING_SPEED = UUID.fromString("c0ffee01-0000-4000-8000-000000000001");
    private static final UUID KING_ATK   = UUID.fromString("c0ffee01-0000-4000-8000-000000000002");

    /** ticks acumulados longe do Rei, por player. */
    private static final Map<UUID, Integer> FAR_TIMER = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> NEAR_TIMER = new ConcurrentHashMap<>();
    private static int counter = 0;

    private static String glowKey(UUID uuid) {
        return "king:" + uuid;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if ((++counter % 5) != 0) return; // a cada 5 ticks

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        var cfg = PitouConfig.COMMON;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            PitouData data = PitouCapability.get(player).resolve().orElse(null);
            if (data == null) continue;

            if (!data.isTransformed() || !data.hasKing()) {
                ServerGlowManager.clearRequest(glowKey(uuid));
                clearBuff(player);
                FAR_TIMER.remove(uuid);
                NEAR_TIMER.remove(uuid);
                // paranoia fica GUARDADA (nao zera); so nao flicka
                boolean changed = syncActive(data, false);
                if (data.isKingNear()) { data.setKingNear(false); changed = true; }
                if (changed) PitouNetwork.syncTo(player);
                continue;
            }

            ServerPlayer king = server.getPlayerList().getPlayer(data.getKingId());
            boolean far;
            boolean kingNear = false;

            if (king == null || king == player) {
                // Rei offline (ou apontou pra si mesmo): sem highlight/buff; conta como longe.
                ServerGlowManager.clearRequest(glowKey(uuid));
                clearBuff(player);
                far = true;
            } else {
                ServerLevel kingLevel = king.serverLevel();

                // 1) highlight de mobs hostis perto do Rei
                AABB hlBox = king.getBoundingBox().inflate(cfg.kingHighlightRadius.get());
                List<LivingEntity> near = kingLevel.getEntitiesOfClass(LivingEntity.class, hlBox,
                        e -> e instanceof Enemy && e.isAlive());
                ServerGlowManager.setRequest(glowKey(uuid), new HashSet<>(near));

                // 2) buff se algum mob esta com o Rei como alvo
                AABB agBox = king.getBoundingBox().inflate(cfg.kingAggroRange.get());
                boolean aggro = !kingLevel.getEntitiesOfClass(Mob.class, agBox,
                        m -> m.isAlive() && m.getTarget() == king).isEmpty();

                if (aggro) {
                    double ratio = king.getHealth() / Math.max(1.0F, king.getMaxHealth());
                    double frac = computeFrac(ratio,
                            cfg.kingBuffFloorHealthRatio.get(),
                            cfg.kingBuffMinFraction.get());
                    double amount = cfg.kingBuffMaxMultiplier.get() * frac;
                    applyBuff(player, amount);
                } else {
                    clearBuff(player);
                }

                // 3) longe? / perto?
                boolean sameDim = king.level() == player.level();
                double dist = sameDim ? player.distanceTo(king) : Double.MAX_VALUE;
                far = !sameDim || dist > cfg.kingFarDistance.get();
                kingNear = sameDim && dist <= cfg.kingCloseDistance.get();
            }

            boolean dirty = false;

            if (far) {
                NEAR_TIMER.remove(uuid);
                int ft = FAR_TIMER.getOrDefault(uuid, 0) + 5;
                if (ft >= cfg.kingFarTimeTicks.get()) {
                    ft = 0;
                    if (data.getParanoia() < 4) {
                        data.setParanoia(data.getParanoia() + 1);
                        dirty = true;
                    }
                }
                FAR_TIMER.put(uuid, ft);
            } else {
                FAR_TIMER.remove(uuid);
                int nt = NEAR_TIMER.getOrDefault(uuid, 0) + 5;
                if (nt >= cfg.kingCalmTicks.get()) {
                    nt = 0;
                    if (data.getParanoia() > 0) {
                        data.setParanoia(data.getParanoia() - 1);
                        dirty = true;
                    }
                }
                NEAR_TIMER.put(uuid, nt);
            }

            // a paranoia so flicka quando longe; perto fica guardada
            if (syncActive(data, far)) dirty = true;
            if (data.isKingNear() != kingNear) { data.setKingNear(kingNear); dirty = true; }
            if (dirty) PitouNetwork.syncTo(player);
        }
    }

    /** Conta morte do Rei: incrementa kingDeaths em qualquer Pitou que tenha ele como Rei. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer dead)) return;
        MinecraftServer server = dead.getServer();
        if (server == null) return;
        UUID deadId = dead.getUUID();

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            PitouCapability.get(p).resolve().ifPresent(d -> {
                if (deadId.equals(d.getKingId())) {
                    d.addKingDeath();
                    if (d.getParanoia() < 4) d.setParanoia(d.getParanoia() + 1);
                    PitouNetwork.syncTo(p);
                }
            });
        }
    }

    /** Pitou nao pode ferir o Rei. */
    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Entity src = event.getSource().getEntity();
        if (!(src instanceof ServerPlayer attacker)) return;

        PitouData data = PitouCapability.get(attacker).resolve().orElse(null);
        if (data == null || !data.isTransformed()) return;

        if (victim.getUUID().equals(data.getKingId())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            UUID uuid = sp.getUUID();
            ServerGlowManager.clearRequest(glowKey(uuid));
            clearBuff(sp);
            FAR_TIMER.remove(uuid);
            NEAR_TIMER.remove(uuid);
        }
    }

    // ---- helpers ----

    /** Fracao do buff: minFrac na vida cheia, 1.0 quando ratio <= floor. */
    private static double computeFrac(double ratio, double floor, double minFrac) {
        if (ratio <= floor) return 1.0;
        if (ratio >= 1.0) return minFrac;
        double t = (1.0 - ratio) / (1.0 - floor); // 0 na vida cheia, 1 no piso
        return minFrac + (1.0 - minFrac) * t;
    }

    /** Atualiza a flag de "paranoia ativa" (longe). Retorna true se mudou. */
    private static boolean syncActive(PitouData data, boolean active) {
        if (data.isParanoiaActive() != active) {
            data.setParanoiaActive(active);
            return true;
        }
        return false;
    }

    private static void applyBuff(ServerPlayer player, double amount) {
        setMod(player, Attributes.MOVEMENT_SPEED, KING_SPEED, "pitou_king_speed", amount);
        setMod(player, Attributes.ATTACK_SPEED, KING_ATK, "pitou_king_atk", amount);
    }

    private static void clearBuff(ServerPlayer player) {
        removeMod(player, Attributes.MOVEMENT_SPEED, KING_SPEED);
        removeMod(player, Attributes.ATTACK_SPEED, KING_ATK);
    }

    private static void setMod(ServerPlayer player, Attribute attr, UUID id, String name, double amount) {
        AttributeInstance inst = player.getAttribute(attr);
        if (inst == null) return;
        AttributeModifier existing = inst.getModifier(id);
        if (existing != null) {
            if (existing.getAmount() == amount) return; // ja esta certo
            inst.removeModifier(id);
        }
        inst.addTransientModifier(new AttributeModifier(id, name, amount,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void removeMod(ServerPlayer player, Attribute attr, UUID id) {
        AttributeInstance inst = player.getAttribute(attr);
        if (inst != null && inst.getModifier(id) != null) {
            inst.removeModifier(id);
        }
    }
}
