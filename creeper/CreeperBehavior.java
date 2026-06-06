package com.megu.neferpitou.creeper;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Predicate;

/**
 * Lado SERVIDOR do comportamento de creeper perto da Pitou.
 *
 * Dois efeitos:
 *  1) Creepers FOGEM da Pitou transformada (AvoidEntityGoal em prioridade alta,
 *     para vencer o SwellGoal de explodir).
 *  2) Creepers NAO conseguem mirar a Pitou como alvo (cancelamos a troca de alvo),
 *     entao nem chegam a inflar/atacar.
 *
 * Registrado automaticamente pelo @EventBusSubscriber (nao precisa tocar na main).
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CreeperBehavior {

    /** "Esta entidade e uma Pitou transformada?" - usada pelos goals e pelo target. */
    private static final Predicate<LivingEntity> IS_PITOU =
            e -> e instanceof Player p && isTransformedPitou(p);

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof Creeper creeper) {
            float dist = PitouConfig.COMMON.creeperDetectRadius.get().floatValue();

            // Prioridade 1: vence o SwellGoal (prioridade 2). walk=1.0, sprint=1.2 (corre fugindo).
            // O predicado so dispara para players que sejam Pitou transformada.
            creeper.goalSelector.addGoal(1, new AvoidEntityGoal<>(
                    creeper, Player.class, IS_PITOU, dist, 1.0D, 1.2D, IS_PITOU));
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Creeper) {
            LivingEntity target = event.getNewTarget();
            if (target instanceof Player p && isTransformedPitou(p)) {
                // Impede o creeper de travar na Pitou como alvo -> nao infla, nao explode.
                event.setCanceled(true);
            }
        }
    }

    private static boolean isTransformedPitou(Player p) {
        return PitouCapability.get(p).resolve().map(PitouData::isTransformed).orElse(false);
    }
}
