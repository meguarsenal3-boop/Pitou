package com.megu.neferpitou.nen;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.capability.PitouMode;
import com.megu.neferpitou.config.PitouConfig;
import com.megu.neferpitou.network.PitouNetwork;
import com.megu.neferpitou.threat.ThreatClassifier;
import com.megu.neferpitou.threat.ThreatLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Progressao para desbloquear o estagio do Nen:
 *  - matar (config) seres STRONG ou acima;
 *  - matar (config) seres MEDIUM;
 *  - clicar com graveto em (config) cerebros.
 * Cumpridas as tres, desbloqueia PitouMode.NEN.
 *
 * So conta enquanto transformada e ainda sem Nen.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NenProgression {

    private NenProgression() {}

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        PitouData data = PitouCapability.get(player).resolve().orElse(null);
        if (data == null || !data.isTransformed() || data.hasUnlocked(PitouMode.NEN)) return;

        LivingEntity dead = event.getEntity();
        ThreatLevel tier = ThreatClassifier.computeFresh(dead);

        boolean changed = false;
        if (tier.isStrongPrey()) {            // STRONG ou acima
            data.addNenStrongKill();
            changed = true;
        } else if (tier == ThreatLevel.MEDIUM) {
            data.addNenMediumKill();
            changed = true;
        }

        if (changed) {
            checkUnlock(player, data);
            PitouNetwork.syncTo(player);
        }
    }

    /** Chamado pelo BrainBlock. Retorna true se o cerebro foi consumido. */
    public static boolean tryAddBrain(ServerPlayer player) {
        PitouData data = PitouCapability.get(player).resolve().orElse(null);
        if (data == null || !data.isTransformed() || data.hasUnlocked(PitouMode.NEN)) {
            return false;
        }
        data.addNenBrain();
        checkUnlock(player, data);
        PitouNetwork.syncTo(player);
        return true;
    }

    private static void checkUnlock(ServerPlayer player, PitouData data) {
        var cfg = PitouConfig.COMMON;
        if (data.hasUnlocked(PitouMode.NEN)) return;
        if (data.getNenStrongKills() >= cfg.nenStrongKillsNeeded.get()
                && data.getNenMediumKills() >= cfg.nenMediumKillsNeeded.get()
                && data.getNenBrains() >= cfg.nenBrainsNeeded.get()) {
            data.setUnlocked(PitouMode.NEN);
            player.sendSystemMessage(Component.literal("Voce despertou o Nen."));
        }
    }
}
