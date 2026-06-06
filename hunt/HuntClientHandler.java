package com.megu.neferpitou.hunt;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.config.PitouConfig;
import com.megu.neferpitou.threat.ThreatClassifier;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Caca de seres fortes (client):
 *  - Detecta o ser forte (STRONG/APEX) mais proximo no raio.
 *  - Toca um "barulhinho" quando detecta um alvo novo.
 *  - Indica no HUD quando voce esta alto (y >= strongPreyVisionMinY) -> HuntClientState.
 *  - Distrai: a camera flicka na direcao do alvo de tempos em tempos
 *    (mas o creeper tem prioridade na distracao).
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HuntClientHandler {

    private static int prevTarget = -1;
    private static int scanCooldown = 0;
    private static int flickCooldown = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        var cfg = PitouConfig.COMMON;

        if (player == null || mc.level == null || !isTransformed(player)) {
            reset();
            return;
        }

        // --- detecta o alvo (a cada 10 ticks) ---
        if (scanCooldown <= 0) {
            HuntClientState.targetId = findStrongPrey(mc, player);
            scanCooldown = 10;
        } else {
            scanCooldown--;
        }

        int targetId = HuntClientState.targetId;

        // alvo sumiu? confere se a entidade ainda existe
        Entity target = targetId != -1 ? mc.level.getEntity(targetId) : null;
        if (target == null || !target.isAlive()) {
            HuntClientState.targetId = -1;
            targetId = -1;
        }

        // som ao detectar um alvo NOVO
        if (targetId != -1 && targetId != prevTarget) {
            player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 2.5F, 0.7F);
        }
        prevTarget = targetId;

        // indicacao visivel quando alto o suficiente
        HuntClientState.seeingIndicator = targetId != -1
                && player.getY() >= cfg.strongPreyVisionMinY.get();

        // --- distracao: flicka a camera para o alvo (creeper tem prioridade) ---
        if (targetId == -1 || mc.screen != null || creeperNearby(mc, player)) {
            flickCooldown = 0;
            return;
        }
        if (flickCooldown <= 0) {
            // perto do alvo (<= 10m): nao flicka mais
            if (player.distanceToSqr(target) <= 100.0) {
                return;
            }
            snapTo(player, target);
            flickCooldown = cfg.huntFlickIntervalTicks.get();
        } else {
            flickCooldown--;
        }
    }

    private static int findStrongPrey(Minecraft mc, Player player) {
        double radius = PitouConfig.COMMON.strongPreyDetectRadius.get();
        double radiusSqr = radius * radius;
        AABB box = player.getBoundingBox().inflate(radius);
        long gameTime = mc.level.getGameTime();

        List<LivingEntity> mobs = mc.level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && !(e instanceof Player) && e.isAlive());

        int best = -1;
        double bestSqr = Double.MAX_VALUE;
        for (LivingEntity mob : mobs) {
            double dSqr = player.distanceToSqr(mob);
            if (dSqr > radiusSqr) continue;
            if (!ThreatClassifier.classify(mob, gameTime).isStrongPrey()) continue;
            if (dSqr < bestSqr) {
                bestSqr = dSqr;
                best = mob.getId();
            }
        }
        return best;
    }

    private static boolean creeperNearby(Minecraft mc, Player player) {
        double r = PitouConfig.COMMON.creeperDetectRadius.get();
        AABB box = player.getBoundingBox().inflate(r);
        return !mc.level.getEntitiesOfClass(Creeper.class, box,
                c -> c.isAlive() && player.distanceToSqr(c) <= r * r).isEmpty();
    }

    private static void snapTo(Player player, Entity target) {
        double dx = target.getX() - player.getX();
        double dy = target.getEyeY() - player.getEyeY();
        double dz = target.getZ() - player.getZ();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, horiz)));
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.yRotO = yaw;
        player.xRotO = pitch;
        player.setYHeadRot(yaw);
    }

    private static void reset() {
        HuntClientState.targetId = -1;
        HuntClientState.seeingIndicator = false;
        prevTarget = -1;
        flickCooldown = 0;
    }

    private static boolean isTransformed(Player p) {
        return PitouCapability.get(p).resolve().map(PitouData::isTransformed).orElse(false);
    }
}
