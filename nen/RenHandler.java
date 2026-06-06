package com.megu.neferpitou.nen;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.capability.PitouMode;
import com.megu.neferpitou.config.PitouConfig;
import com.megu.neferpitou.threat.ThreatClassifier;
import com.megu.neferpitou.threat.ThreatLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.UUID;

/**
 * Ren (toggle, nao evolui): dano +75%, velocidade +25%. Cria uma aura assustadora num
 * raio (renRadius) que afeta os seres por tier:
 *   TRIVIAL  -> morrem
 *   WEAK     -> paralisados (slowness/weakness altissimos)
 *   MEDIUM   -> muito lentos e fracos
 *   STRONG   -> um pouco lentos e fracos
 *   VERY_STRONG/APEX/PREY -> nada
 * Players levam slowness/weakness baixos; o Rei e imune.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RenHandler {

    private RenHandler() {}

    private static final UUID REN_DMG = UUID.fromString("33333333-0000-4000-8000-0000000000e1");
    private static final UUID REN_SPD = UUID.fromString("33333333-0000-4000-8000-0000000000e2");

    private static int counter = 0;
    private static final int EFFECT_TICKS = 40; // > intervalo, para nao piscar

    private static boolean active(PitouData d) {
        return d != null && d.isTransformed() && d.hasUnlocked(PitouMode.NEN) && d.isRenActive();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var cfg = PitouConfig.COMMON;
        int interval = cfg.threatTickInterval.get();
        if ((++counter % interval) != 0) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PitouData data = PitouCapability.get(player).resolve().orElse(null);
            if (!active(data)) {
                clearBuff(player);
                continue;
            }

            setMod(player, Attributes.ATTACK_DAMAGE, REN_DMG, "ren_dmg", cfg.renDamageBonus.get());
            setMod(player, Attributes.MOVEMENT_SPEED, REN_SPD, "ren_spd", cfg.renSpeedBonus.get());

            ServerLevel level = player.serverLevel();
            double radius = cfg.renRadius.get();
            AABB box = player.getBoundingBox().inflate(radius);
            UUID kingId = data.getKingId();

            List<LivingEntity> around = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player && e.isAlive());
            for (LivingEntity le : around) {
                if (le instanceof Player p) {
                    if (kingId != null && kingId.equals(p.getUUID())) continue; // Rei imune
                    apply(p, 0, 0); // players: slowness/weakness baixos
                    continue;
                }
                ThreatLevel tier = ThreatClassifier.computeFresh(le);
                switch (tier) {
                    case TRIVIAL -> le.hurt(level.damageSources().magic(), 1000.0F);
                    case WEAK -> apply(le, 6, 4);   // paralisado
                    case MEDIUM -> apply(le, 3, 3); // muito lento e fraco
                    case STRONG -> apply(le, 1, 1); // pouco lento e fraco
                    default -> { /* REALLY_STRONG / APEX / PREY: nada */ }
                }
            }
        }
    }

    private static void apply(LivingEntity e, int slowAmp, int weakAmp) {
        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_TICKS, slowAmp, true, false));
        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_TICKS, weakAmp, true, false));
    }

    private static void clearBuff(ServerPlayer player) {
        removeMod(player, Attributes.ATTACK_DAMAGE, REN_DMG);
        removeMod(player, Attributes.MOVEMENT_SPEED, REN_SPD);
    }

    private static void setMod(Player player, Attribute attr, UUID id, String name, double amount) {
        AttributeInstance inst = player.getAttribute(attr);
        if (inst == null) return;
        if (inst.getModifier(id) != null) {
            if (inst.getModifier(id).getAmount() == amount) return;
            inst.removeModifier(id);
        }
        inst.addTransientModifier(new AttributeModifier(id, name, amount,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void removeMod(Player player, Attribute attr, UUID id) {
        AttributeInstance inst = player.getAttribute(attr);
        if (inst != null && inst.getModifier(id) != null) inst.removeModifier(id);
    }
}
