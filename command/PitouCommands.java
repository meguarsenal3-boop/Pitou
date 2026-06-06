package com.megu.neferpitou.command;

import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.capability.PitouMode;
import com.megu.neferpitou.network.PitouNetwork;
import com.megu.neferpitou.threat.ThreatClassifier;
import com.megu.neferpitou.threat.ThreatLevel;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Comandos:
 *  /pitou transform                 - liga/desliga a transformacao
 *  /pitou debug mode <tier>         - forca o tier desbloqueado (IDENTITY/NEN/HATSU/TERPSICHORA)
 *  /pitou debug king <player>       - define o Rei
 *  /pitou debug king clear          - remove o Rei
 *  /pitou debug paranoia <0-4>      - seta o nivel de paranoia
 *  /pitou debug status              - mostra o estado atual
 *  /pitou debug reset               - zera tudo
 *  /pitou debug threat              - mostra o ThreatLevel da entidade que voce esta mirando
 *
 * Sem permissao alta de proposito (level 2) para voce conseguir testar no servidor do amigo.
 */
public class PitouCommands {

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("pitou")
                .requires(src -> src.hasPermission(2))

                .then(Commands.literal("transform")
                        .executes(ctx -> transform(ctx.getSource())))

                .then(Commands.literal("debug")

                        .then(Commands.literal("mode")
                                .then(Commands.argument("tier", StringArgumentType.word())
                                        .suggests((c, b) -> {
                                            for (PitouMode m : PitouMode.values()) b.suggest(m.name());
                                            return b.buildFuture();
                                        })
                                        .executes(ctx -> setMode(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "tier")))))

                        .then(Commands.literal("king")
                                .then(Commands.literal("clear")
                                        .executes(ctx -> clearKing(ctx.getSource())))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> setKing(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player")))))

                        .then(Commands.literal("paranoia")
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 4))
                                        .executes(ctx -> setParanoia(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "level")))))

                        .then(Commands.literal("status")
                                .executes(ctx -> status(ctx.getSource())))

                        .then(Commands.literal("reset")
                                .executes(ctx -> reset(ctx.getSource())))

                        .then(Commands.literal("threat")
                                .executes(ctx -> threatLookedAt(ctx.getSource())))
                )
        );
    }

    private static ServerPlayer self(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return src.getPlayerOrException();
    }

    private static int transform(CommandSourceStack src) {
        try {
            ServerPlayer p = self(src);
            PitouCapability.with(p, data -> {
                boolean now = !data.isTransformed();
                data.setTransformed(now);
                src.sendSuccess(() -> Component.literal(now
                        ? "Voce virou a Neferpitou."
                        : "Voce voltou ao normal."), false);
            });
            PitouNetwork.syncTo(p);
            return 1;
        } catch (Exception e) {
            src.sendFailure(Component.literal("So um player pode se transformar."));
            return 0;
        }
    }

    private static int setMode(CommandSourceStack src, String tierName) {
        try {
            ServerPlayer p = self(src);
            PitouMode mode = PitouMode.byNameSafe(tierName);
            if (mode == null) {
                src.sendFailure(Component.literal("Tier invalido. Use: IDENTITY, NEN, HATSU ou TERPSICHORA."));
                return 0;
            }
            PitouCapability.with(p, data -> {
                data.setUnlocked(mode);
                if (!data.isTransformed()) data.setTransformed(true);
                src.sendSuccess(() -> Component.literal("Tier desbloqueado: " + mode.name()), false);
            });
            PitouNetwork.syncTo(p);
            return 1;
        } catch (Exception e) {
            src.sendFailure(Component.literal("So funciona como player."));
            return 0;
        }
    }

    private static int setKing(CommandSourceStack src, ServerPlayer king) {
        try {
            ServerPlayer p = self(src);
            PitouCapability.with(p, data -> {
                data.setKingId(king.getUUID());
                src.sendSuccess(() -> Component.literal("O Rei agora e: " + king.getGameProfile().getName()), false);
            });
            PitouNetwork.syncTo(p);
            return 1;
        } catch (Exception e) {
            src.sendFailure(Component.literal("So funciona como player."));
            return 0;
        }
    }

    private static int clearKing(CommandSourceStack src) {
        try {
            ServerPlayer p = self(src);
            PitouCapability.with(p, data -> {
                data.setKingId(null);
                src.sendSuccess(() -> Component.literal("O Rei foi removido."), false);
            });
            PitouNetwork.syncTo(p);
            return 1;
        } catch (Exception e) {
            src.sendFailure(Component.literal("So funciona como player."));
            return 0;
        }
    }

    private static int setParanoia(CommandSourceStack src, int level) {
        try {
            ServerPlayer p = self(src);
            PitouCapability.with(p, data -> {
                data.setParanoia(level);
                src.sendSuccess(() -> Component.literal("Paranoia setada para " + level + "."), false);
            });
            PitouNetwork.syncTo(p);
            return 1;
        } catch (Exception e) {
            src.sendFailure(Component.literal("So funciona como player."));
            return 0;
        }
    }

    private static int status(CommandSourceStack src) {
        try {
            ServerPlayer p = self(src);
            PitouCapability.with(p, data -> {
                String king = data.hasKing() ? data.getKingId().toString() : "(nenhum)";
                String msg = "=== Neferpitou ===\n"
                        + "Transformada: " + data.isTransformed() + "\n"
                        + "Tier: " + data.getUnlocked().name() + "\n"
                        + "Rei: " + king + "\n"
                        + "Mortes do Rei: " + data.getKingDeaths() + "\n"
                        + "Paranoia: " + data.getParanoia() + "\n"
                        + "Maestria Ryu: " + String.format("%.2f", data.getRyuMastery()) + "\n"
                        + "Maestria Blythe: " + String.format("%.2f", data.getBlytheMastery()) + "\n"
                        + "En ativo: " + data.isEnActive()
                        + " | Ryu: " + data.isRyuActive()
                        + " | Blythe: " + data.isBlytheActive()
                        + " | Terpsichora: " + data.isTerpsichoraActive();
                src.sendSuccess(() -> Component.literal(msg), false);
            });
            return 1;
        } catch (Exception e) {
            src.sendFailure(Component.literal("So funciona como player."));
            return 0;
        }
    }

    private static int reset(CommandSourceStack src) {
        try {
            ServerPlayer p = self(src);
            PitouCapability.with(p, PitouData::clearAll);
            PitouNetwork.syncTo(p);
            src.sendSuccess(() -> Component.literal("Estado da Pitou zerado."), false);
            return 1;
        } catch (Exception e) {
            src.sendFailure(Component.literal("So funciona como player."));
            return 0;
        }
    }

    /** Mostra o ThreatLevel da entidade que o player esta olhando (ate 64 blocos). */
    private static int threatLookedAt(CommandSourceStack src) {
        try {
            ServerPlayer p = self(src);
            Entity hit = rayTraceEntity(p, 64.0);
            if (!(hit instanceof LivingEntity living)) {
                src.sendFailure(Component.literal("Mire numa criatura viva (ate 64 blocos)."));
                return 0;
            }
            ThreatLevel level = ThreatClassifier.computeFresh(living);
            double score = ThreatClassifier.score(living);
            src.sendSuccess(() -> Component.literal(
                    living.getName().getString() + " -> " + level.name()
                            + " (score " + String.format("%.1f", score) + ")"), false);
            return 1;
        } catch (Exception e) {
            src.sendFailure(Component.literal("So funciona como player."));
            return 0;
        }
    }

    /** Raycast simples mirando entidades a partir da visao do player. */
    private static Entity rayTraceEntity(ServerPlayer player, double reach) {
        var eye = player.getEyePosition();
        var look = player.getViewVector(1.0F);
        var end = eye.add(look.x * reach, look.y * reach, look.z * reach);
        var box = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0);

        var result = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                player.level(), player, eye, end, box,
                e -> e instanceof LivingEntity && e != player);

        return result != null ? result.getEntity() : null;
    }
}
