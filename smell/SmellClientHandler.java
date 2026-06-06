package com.megu.neferpitou.smell;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * "Cheirar" (instinto). TOGGLE com a tecla G.
 *
 * O HIGHLIGHT agora e feito no SERVIDOR (glow nativo, atravessa paredes). Este lado
 * client cuida de:
 *   - avisar o servidor quando comeca/para de cheirar (packet), para ele acender o glow;
 *   - acumular a nausea por cheiro ruim (mortos-vivos; afogados pior), que decai sempre;
 *   - dirigir a distorcao de tela de forma SUAVE (escalada por smellNauseaIntensity).
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SmellClientHandler {

    private static boolean smellingToggle = false;
    private static boolean lastSent = false;
    private static double nausea = 0.0;

    /** 0..1: 0 abaixo do gatilho, sobe ate 1 no maximo. Usado pela vinheta e pela distorcao. */
    public static float getNauseaLevel() {
        double trig = PitouConfig.COMMON.smellNauseaTrigger.get();
        double max = PitouConfig.COMMON.smellNauseaMax.get();
        if (nausea < trig || max <= trig) return 0f;
        return (float) Math.min(1.0, (nausea - trig) / (max - trig));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        var cfg = PitouConfig.COMMON;

        if (player == null || mc.level == null) {
            if (lastSent) { SmellNetwork.sendSmelling(false); lastSent = false; }
            smellingToggle = false;
            nausea = 0.0;
            return;
        }

        boolean transformed = isTransformed(player);

        while (SmellKeyMapping.SMELL.consumeClick()) {
            if (transformed) smellingToggle = !smellingToggle;
        }
        if (!transformed) smellingToggle = false;

        boolean smelling = transformed && smellingToggle;

        // Avisa o servidor so quando muda (liga/desliga o glow la).
        if (smelling != lastSent) {
            SmellNetwork.sendSmelling(smelling);
            lastSent = smelling;
        }

        if (!smelling) {
            nausea = 0.0;
            driveDistortion(mc);
            return;
        }

        // Calcula a nausea localmente (so para a distorcao/vinheta).
        double radius = cfg.smellRadius.get();
        double radiusSqr = radius * radius;
        double caveMul = cfg.smellCaveReduction.get();

        AABB box = player.getBoundingBox().inflate(radius);
        List<LivingEntity> mobs = mc.level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && !(e instanceof Player) && e.isAlive());

        double nauseaGain = 0.0;
        for (LivingEntity mob : mobs) {
            double strength = smellStrength(mob);
            if (strength <= 0) continue;

            double dSqr = player.distanceToSqr(mob);
            boolean hidden = mob.isInWater() || !mc.level.canSeeSky(mob.blockPosition());
            double effRadiusSqr = hidden ? radiusSqr * caveMul * caveMul : radiusSqr;
            if (dSqr > effRadiusSqr) continue;

            double prox = 1.0 - Math.sqrt(dSqr) / radius;
            if (prox < 0) prox = 0;
            nauseaGain += strength * prox * cfg.smellNauseaPerTick.get();
        }

        nausea = clamp(nausea + nauseaGain - cfg.smellNauseaDecay.get(),
                0.0, cfg.smellNauseaMax.get());

        driveDistortion(mc);
    }

    /** Distorcao SUAVE: nivel de nausea (0..1) vezes a intensidade maxima do config. */
    private static void driveDistortion(Minecraft mc) {
        LocalPlayer lp = mc.player;
        if (lp == null) return;
        float target = getNauseaLevel() * PitouConfig.COMMON.smellNauseaIntensity.get().floatValue();
        if (target > 0f) {
            lp.spinningEffectIntensity = target;
            lp.oSpinningEffectIntensity = target;
        }
        // target == 0: o jogo baixa sozinho.
    }

    private static double smellStrength(LivingEntity mob) {
        if (mob instanceof Drowned) return 2.0;
        if (mob.getMobType() == MobType.UNDEAD) return 1.0;
        return 0.0;
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : Math.min(v, hi);
    }

    private static boolean isTransformed(Player p) {
        return PitouCapability.get(p).resolve().map(PitouData::isTransformed).orElse(false);
    }
}
