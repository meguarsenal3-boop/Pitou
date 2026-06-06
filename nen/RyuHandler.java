package com.megu.neferpitou.nen;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.capability.PitouMode;
import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.UUID;

/**
 * Ryu (toggle): dano +50%, resistencia +50%, velocidade +25% no inicio. Quanto mais
 * usa, mais forte fica, ate os tetos (dano/resistencia 300%, velocidade 50%).
 *
 * Dano e velocidade sao modificadores de atributo (MULTIPLY_TOTAL). A resistencia
 * reduz o dano recebido no LivingHurtEvent: dano / (1 + bonus).
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RyuHandler {

    private RyuHandler() {}

    private static final UUID RYU_DMG = UUID.fromString("22222222-0000-4000-8000-0000000000d1");
    private static final UUID RYU_SPD = UUID.fromString("22222222-0000-4000-8000-0000000000d2");

    private static int counter = 0;

    private static boolean active(PitouData d) {
        return d != null && d.isTransformed() && d.hasUnlocked(PitouMode.NEN) && d.isRyuActive();
    }

    private static double frac(PitouData d) {
        double max = PitouConfig.COMMON.ryuMasteryMaxTicks.get();
        return max <= 0 ? 1.0 : Math.min(1.0, d.getRyuMastery() / max);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if ((++counter % 5) != 0) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        var cfg = PitouConfig.COMMON;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PitouData data = PitouCapability.get(player).resolve().orElse(null);
            if (!active(data)) {
                clear(player);
                continue;
            }

            // cresce com o uso
            data.setRyuMastery(data.getRyuMastery() + 5);

            double f = frac(data);
            double dmg = lerp(cfg.ryuDamageResistStart.get(), cfg.ryuDamageResistMax.get(), f);
            double spd = lerp(cfg.ryuSpeedStart.get(), cfg.ryuSpeedMax.get(), f);

            setMod(player, Attributes.ATTACK_DAMAGE, RYU_DMG, "ryu_dmg", dmg);
            setMod(player, Attributes.MOVEMENT_SPEED, RYU_SPD, "ryu_spd", spd);
        }
    }

    /** Resistencia do Ryu: reduz o dano recebido. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PitouData data = PitouCapability.get(player).resolve().orElse(null);
        if (!active(data)) return;

        double resBonus = lerp(PitouConfig.COMMON.ryuDamageResistStart.get(),
                PitouConfig.COMMON.ryuDamageResistMax.get(), frac(data));
        event.setAmount((float) (event.getAmount() / (1.0 + resBonus)));
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static void clear(ServerPlayer player) {
        removeMod(player, Attributes.ATTACK_DAMAGE, RYU_DMG);
        removeMod(player, Attributes.MOVEMENT_SPEED, RYU_SPD);
    }

    private static void setMod(Player player, Attribute attr, UUID id, String name, double amount) {
        AttributeInstance inst = player.getAttribute(attr);
        if (inst == null) return;
        AttributeModifier existing = inst.getModifier(id);
        if (existing != null) {
            if (existing.getAmount() == amount) return;
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
