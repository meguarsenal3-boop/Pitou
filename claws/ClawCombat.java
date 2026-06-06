package com.megu.neferpitou.claws;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Multiplicador de garras no dano melee. Quando o modo "brincar" esta ligado,
 * NAO multiplica (o PlayHandler define o dano final), para nao conflitar.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClawCombat {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        DamageSource src = event.getSource();
        if (src.getDirectEntity() instanceof Player p && src.getEntity() == src.getDirectEntity()) {
            PitouData data = PitouCapability.get(p).resolve().orElse(null);
            if (data == null) return;
            if (!data.isTransformed() || !data.isClawsExposed()) return;
            if (data.isPlayWithPrey()) return; // modo brincar manda no dano

            float mult = PitouConfig.COMMON.clawDamageMultiplier.get().floatValue();
            event.setAmount(event.getAmount() * mult);
        }
    }
}
