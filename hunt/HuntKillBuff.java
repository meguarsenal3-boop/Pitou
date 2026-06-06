package com.megu.neferpitou.hunt;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.config.PitouConfig;
import com.megu.neferpitou.threat.ThreatClassifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Ao matar um ser forte (STRONG/APEX), a Pitou fica "animada": ganha um buff
 * temporario de velocidade de movimento e attack speed. Sao modificadores de
 * atributo transientes (nao salvam), removidos quando o tempo acaba.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HuntKillBuff {

    private static final UUID SPEED_ID = UUID.fromString("d1f0a1e2-0001-4a00-8000-abcdeabcde01");
    private static final UUID ATK_ID = UUID.fromString("d1f0a1e2-0002-4a00-8000-abcdeabcde02");

    private static final Map<UUID, Integer> TICKS_LEFT = new HashMap<>();

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getSource().getEntity() instanceof ServerPlayer killer && isTransformed(killer)) {
            LivingEntity dead = event.getEntity();
            long gameTime = dead.level().getGameTime();
            if (ThreatClassifier.classify(dead, gameTime).isStrongPrey()) {
                applyBuff(killer);
                TICKS_LEFT.put(killer.getUUID(), PitouConfig.COMMON.huntKillBuffDurationTicks.get());
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || TICKS_LEFT.isEmpty()) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        TICKS_LEFT.entrySet().removeIf(entry -> {
            int left = entry.getValue() - 1;
            if (left <= 0) {
                ServerPlayer p = server.getPlayerList().getPlayer(entry.getKey());
                if (p != null) removeBuff(p);
                return true;
            }
            entry.setValue(left);
            return false;
        });
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            TICKS_LEFT.remove(sp.getUUID());
            removeBuff(sp);
        }
    }

    private static void applyBuff(ServerPlayer p) {
        double pct = PitouConfig.COMMON.huntKillBuffPercent.get();
        addModifier(p, Attributes.MOVEMENT_SPEED, SPEED_ID, "pitou_hunt_speed", pct);
        addModifier(p, Attributes.ATTACK_SPEED, ATK_ID, "pitou_hunt_atkspeed", pct);
    }

    private static void removeBuff(ServerPlayer p) {
        removeModifier(p, Attributes.MOVEMENT_SPEED, SPEED_ID);
        removeModifier(p, Attributes.ATTACK_SPEED, ATK_ID);
    }

    private static void addModifier(Player p, Attribute attr, UUID id, String name, double amount) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) return;
        inst.removeModifier(id); // refresca se ja existia
        inst.addTransientModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void removeModifier(Player p, Attribute attr, UUID id) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst != null) inst.removeModifier(id);
    }

    private static boolean isTransformed(Player p) {
        return PitouCapability.get(p).resolve().map(PitouData::isTransformed).orElse(false);
    }
}
