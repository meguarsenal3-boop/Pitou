package com.megu.neferpitou.play;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.capability.PitouMode;
import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Modo "brincar com a presa": substitui o dano melee por uma fracao da vida MAXIMA
 * do alvo, com um teto (cap) baseado no proprio dano da Pitou.
 *
 *  - sem garras: 5% da vida maxima, cap = dano melee base
 *  - com garras: 10% da vida maxima, cap = dano melee x multiplicador de garras
 *  - com Nen:    20% da vida maxima, cap = dano atual (melee [+ garras])
 *
 * Roda em prioridade LOWEST: sobrescreve o que o ClawCombat tiver feito.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource src = event.getSource();
        // so melee direto (atacante == quem encostou)
        if (!(src.getDirectEntity() instanceof Player p)) return;
        if (src.getEntity() != src.getDirectEntity()) return;
        if (p.level().isClientSide()) return;

        PitouData data = PitouCapability.get(p).resolve().orElse(null);
        if (data == null || !data.isTransformed() || !data.isPlayWithPrey()) return;

        LivingEntity target = event.getEntity();
        double maxHp = target.getMaxHealth();
        double atk = p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double clawMult = PitouConfig.COMMON.clawDamageMultiplier.get();

        boolean claws = data.isClawsExposed();
        boolean nen = data.hasUnlocked(PitouMode.NEN); // proxy ate o Nen existir de fato

        double pct;
        double cap;
        if (nen) {
            pct = PitouConfig.COMMON.playDamagePctNen.get();
            cap = claws ? atk * clawMult : atk; // + bonus de Nen quando existir
        } else if (claws) {
            pct = PitouConfig.COMMON.playDamagePctClaw.get();
            cap = atk * clawMult;
        } else {
            pct = PitouConfig.COMMON.playDamagePctNoClaw.get();
            cap = atk;
        }

        double dmg = Math.min(pct * maxHp, cap);
        event.setAmount((float) Math.max(0.0, dmg));
    }
}
