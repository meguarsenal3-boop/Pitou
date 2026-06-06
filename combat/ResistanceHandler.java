package com.megu.neferpitou.combat;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Resistencia da Pitou: reduz TODO dano que ela recebe enquanto transformada,
 * por uma fracao do config (damageResistance). Empilha com a armadura dela.
 *
 * Roda em prioridade BAIXA (depois dos outros), para reduzir o valor ja final.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ResistanceHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (p.level().isClientSide()) return;
        if (!isTransformed(p)) return;

        double res = PitouConfig.COMMON.damageResistance.get();
        if (res <= 0.0) return;

        float reduced = (float) (event.getAmount() * (1.0 - res));
        event.setAmount(reduced);
    }

    private static boolean isTransformed(Player p) {
        return PitouCapability.get(p).resolve().map(PitouData::isTransformed).orElse(false);
    }
}
