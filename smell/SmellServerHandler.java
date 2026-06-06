package com.megu.neferpitou.smell;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.config.PitouConfig;
import com.megu.neferpitou.util.ServerGlowManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lado SERVIDOR do cheiro. Enquanto o player cheira, registra no ServerGlowManager
 * os mobs no raio (com reducao em cavernas/agua) e tambem o REI, detectado de muito
 * mais longe que qualquer mob (kingSmellRadius).
 *
 * O glow em si e ligado/desligado pelo ServerGlowManager (uniao de fontes), entao o
 * cheiro nao briga com o highlight do Rei.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SmellServerHandler {

    private static final Set<UUID> SMELLING = ConcurrentHashMap.newKeySet();
    private static int counter = 0;

    private static String key(UUID uuid) {
        return "smell:" + uuid;
    }

    public static void setSmelling(ServerPlayer player, boolean smelling) {
        UUID uuid = player.getUUID();
        if (smelling) {
            SMELLING.add(uuid);
        } else {
            SMELLING.remove(uuid);
            ServerGlowManager.clearRequest(key(uuid));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if ((++counter % 5) != 0) return; // a cada 5 ticks

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        var cfg = PitouConfig.COMMON;
        double radius = cfg.smellRadius.get();
        double radiusSqr = radius * radius;
        double caveMul = cfg.smellCaveReduction.get();
        double kingRadius = cfg.kingSmellRadius.get();
        double kingRadiusSqr = kingRadius * kingRadius;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            boolean active = SMELLING.contains(uuid) && isTransformed(player);

            if (!active) {
                ServerGlowManager.clearRequest(key(uuid));
                continue;
            }

            ServerLevel level = player.serverLevel();
            Set<Entity> desired = new HashSet<>();

            AABB box = player.getBoundingBox().inflate(radius);
            List<LivingEntity> mobs = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player && !(e instanceof Player) && e.isAlive());
            for (LivingEntity mob : mobs) {
                double dSqr = player.distanceToSqr(mob);
                boolean hidden = mob.isInWater() || !level.canSeeSky(mob.blockPosition());
                double eff = hidden ? radiusSqr * caveMul * caveMul : radiusSqr;
                if (dSqr <= eff) desired.add(mob);
            }

            // O Rei: cheirado de muito mais longe que qualquer mob.
            UUID kingId = getKingId(player);
            if (kingId != null) {
                ServerPlayer king = server.getPlayerList().getPlayer(kingId);
                if (king != null && king != player && king.level() == level
                        && player.distanceToSqr(king) <= kingRadiusSqr) {
                    desired.add(king);
                }
            }

            ServerGlowManager.setRequest(key(uuid), desired);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            UUID uuid = sp.getUUID();
            SMELLING.remove(uuid);
            ServerGlowManager.clearRequest(key(uuid));
        }
    }

    private static boolean isTransformed(Player p) {
        return PitouCapability.get(p).resolve().map(PitouData::isTransformed).orElse(false);
    }

    private static UUID getKingId(Player p) {
        return PitouCapability.get(p).resolve().map(PitouData::getKingId).orElse(null);
    }
}
