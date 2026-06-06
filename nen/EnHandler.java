package com.megu.neferpitou.nen;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.capability.PitouMode;
import com.megu.neferpitou.config.PitouConfig;
import com.megu.neferpitou.threat.ThreatClassifier;
import com.megu.neferpitou.threat.ThreatLevel;
import com.megu.neferpitou.util.ServerGlowManager;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * En (toggle): highlight gigante (enRadius) de TUDO que esta vivo e dos itens caidos,
 * COLORIDO por tier. A cor vem de scoreboard teams (e o unico jeito de glow colorido
 * que atravessa parede e renderiza no client do davig). O liga/desliga do glow vai
 * pelo ServerGlowManager (uniao com cheiro/rei).
 *
 *   TRIVIAL=branco  WEAK=ciano  MEDIUM=amarelo  STRONG=dourado
 *   VERY_STRONG=vermelho  APEX=vermelho-escuro  PREY=preto
 *   Rei=roxo  itens=verde
 *
 * Guardamos o time anterior de cada entidade para restaurar quando ela sai do En
 * (importante para o Rei, que pode ter time proprio).
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EnHandler {

    private EnHandler() {}

    // player -> (scoreboardName -> time anterior, "" se nenhum)
    private static final Map<UUID, Map<String, String>> TRACK = new ConcurrentHashMap<>();
    private static int counter = 0;

    private static final String T_TRIVIAL = "nef_en_trivial";
    private static final String T_WEAK    = "nef_en_weak";
    private static final String T_MEDIUM  = "nef_en_medium";
    private static final String T_STRONG  = "nef_en_strong";
    private static final String T_VSTRONG = "nef_en_vstrong";
    private static final String T_APEX    = "nef_en_apex";
    private static final String T_PREY    = "nef_en_prey";
    private static final String T_KING    = "nef_en_king";
    private static final String T_ITEM    = "nef_en_item";

    private static boolean active(PitouData d) {
        return d != null && d.isTransformed() && d.hasUnlocked(PitouMode.NEN) && d.isEnActive();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var cfg = PitouConfig.COMMON;
        if ((++counter % cfg.threatTickInterval.get()) != 0) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            PitouData data = PitouCapability.get(player).resolve().orElse(null);

            if (!active(data)) {
                clearEn(player);
                continue;
            }

            ServerLevel level = player.serverLevel();
            Scoreboard sb = level.getScoreboard();
            ensureTeams(sb);

            AABB box = player.getBoundingBox().inflate(cfg.enRadius.get());
            Set<Entity> glow = new HashSet<>();
            Map<String, String> old = TRACK.getOrDefault(uuid, Map.of());
            Map<String, String> now = new HashMap<>();

            for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive())) {
                if (le == player) continue;
                assign(sb, le, teamFor(le, data), old, now);
                glow.add(le);
            }

            if (cfg.enHighlightItems.get()) {
                for (ItemEntity it : level.getEntitiesOfClass(ItemEntity.class, box, e -> e.isAlive())) {
                    assign(sb, it, T_ITEM, old, now);
                    glow.add(it);
                }
            }

            // entidades que sairam do alcance: restaura o time delas
            for (Map.Entry<String, String> e : old.entrySet()) {
                if (!now.containsKey(e.getKey())) restore(sb, e.getKey(), e.getValue());
            }

            TRACK.put(uuid, now);
            ServerGlowManager.setRequest("en:" + uuid, glow);
        }
    }

    private static String teamFor(LivingEntity le, PitouData data) {
        if (le instanceof Player p) {
            UUID kingId = data.getKingId();
            return (kingId != null && kingId.equals(p.getUUID())) ? T_KING : T_TRIVIAL;
        }
        ThreatLevel tier = ThreatClassifier.computeFresh(le);
        return switch (tier) {
            case TRIVIAL -> T_TRIVIAL;
            case WEAK -> T_WEAK;
            case MEDIUM -> T_MEDIUM;
            case STRONG -> T_STRONG;
            case REALLY_STRONG -> T_VSTRONG;
            case APEX -> T_APEX;
            case PREY -> T_PREY;
        };
    }

    private static void assign(Scoreboard sb, Entity entity, String teamName,
                               Map<String, String> old, Map<String, String> now) {
        String name = entity.getScoreboardName();
        // preserva o time ORIGINAL: se ja rastreado, mantem o de antes; senao, o atual
        String prev;
        if (old.containsKey(name)) {
            prev = old.get(name);
        } else {
            PlayerTeam cur = sb.getPlayersTeam(name);
            prev = (cur == null) ? "" : cur.getName();
        }
        PlayerTeam team = sb.getPlayerTeam(teamName);
        if (team != null) sb.addPlayerToTeam(name, team);
        now.put(name, prev);
    }

    private static void restore(Scoreboard sb, String name, String prevName) {
        if (prevName != null && !prevName.isEmpty()) {
            PlayerTeam prev = sb.getPlayerTeam(prevName);
            if (prev != null) {
                sb.addPlayerToTeam(name, prev);
                return;
            }
        }
        PlayerTeam cur = sb.getPlayersTeam(name);
        if (cur != null && cur.getName().startsWith("nef_en_")) {
            sb.removePlayerFromTeam(name, cur);
        }
    }

    private static void clearEn(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Map<String, String> old = TRACK.remove(uuid);
        if (old != null) {
            Scoreboard sb = player.serverLevel().getScoreboard();
            for (Map.Entry<String, String> e : old.entrySet()) {
                restore(sb, e.getKey(), e.getValue());
            }
        }
        ServerGlowManager.clearRequest("en:" + uuid);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) clearEn(sp);
    }

    private static void ensureTeams(Scoreboard sb) {
        team(sb, T_TRIVIAL, ChatFormatting.WHITE);
        team(sb, T_WEAK, ChatFormatting.AQUA);
        team(sb, T_MEDIUM, ChatFormatting.YELLOW);
        team(sb, T_STRONG, ChatFormatting.GOLD);
        team(sb, T_VSTRONG, ChatFormatting.RED);
        team(sb, T_APEX, ChatFormatting.DARK_RED);
        team(sb, T_PREY, ChatFormatting.BLACK);
        team(sb, T_KING, ChatFormatting.DARK_PURPLE);
        team(sb, T_ITEM, ChatFormatting.GREEN);
    }

    private static void team(Scoreboard sb, String name, ChatFormatting color) {
        PlayerTeam t = sb.getPlayerTeam(name);
        if (t == null) t = sb.addPlayerTeam(name);
        t.setColor(color);
    }
}
