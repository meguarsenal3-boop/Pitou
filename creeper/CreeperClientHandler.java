package com.megu.neferpitou.creeper;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.client.GlowManager;
import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lado CLIENT da caca de creeper: highlight (via GlowManager) + flick de camera.
 * Tudo local: so afeta o seu jogo.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CreeperClientHandler {

    private static final String GLOW_SOURCE = "creeper";

    /** Ticks ate o proximo flick. 0 = pronto para flickar imediatamente. */
    private static int flickCooldown = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) {
            GlowManager.clearRequest(GLOW_SOURCE);
            return;
        }

        if (!isTransformed(player)) {
            GlowManager.clearRequest(GLOW_SOURCE);
            flickCooldown = 0;
            return;
        }

        double radius = PitouConfig.COMMON.creeperDetectRadius.get();
        double radiusSqr = radius * radius;
        AABB box = player.getBoundingBox().inflate(radius);

        List<Creeper> creepers = mc.level.getEntitiesOfClass(Creeper.class, box,
                c -> c.isAlive() && player.distanceToSqr(c) <= radiusSqr
                        && mc.level.canSeeSky(c.blockPosition()));

        Set<Integer> glow = new HashSet<>();
        Creeper nearest = null;
        double nearestSqr = Double.MAX_VALUE;

        for (Creeper c : creepers) {
            glow.add(c.getId());
            double d = player.distanceToSqr(c);
            if (d < nearestSqr) {
                nearestSqr = d;
                nearest = c;
            }
        }

        GlowManager.setRequest(GLOW_SOURCE, glow);

        // perto do Rei a Pitou nao se distrai: mantem o destaque, mas a camera nao flicka
        if (isKingNear(player)) {
            flickCooldown = 0;
            return;
        }

        // ---------------- flick de camera ----------------
        if (nearest == null) {
            flickCooldown = 0; // rearma para flickar quando um aparecer
            return;
        }
        if (mc.screen != null) return; // nao puxa a camera em menus

        if (flickCooldown <= 0) {
            snapTo(player, nearest);
            flickCooldown = PitouConfig.COMMON.creeperFlickIntervalTicks.get();
        } else {
            flickCooldown--;
        }
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

    private static boolean isTransformed(Player p) {
        return PitouCapability.get(p).resolve().map(PitouData::isTransformed).orElse(false);
    }

    private static boolean isKingNear(Player p) {
        return PitouCapability.get(p).resolve().map(PitouData::isKingNear).orElse(false);
    }
}
