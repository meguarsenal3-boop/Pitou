package com.megu.neferpitou.ability;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Passivas da IDENTIDADE (lado logico/servidor): visao noturna, slowness na agua,
 * dano de queda reduzido e os atributos base (velocidade, forca, step height,
 * resistencia). Aplicado enquanto o player estiver transformado; removido ao sair.
 *
 * Atributos sao aplicados como modificadores TRANSITORIOS (nao salvam em disco),
 * recalculados a cada sessao -> sem risco de empilhar ao relogar.
 * A velocidade some quando a Pitou esta na agua (gatos odeiam agua).
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IdentityHandler {

    // UUIDs fixos pra cada modificador (gerados uma vez, nunca mude).
    private static final UUID SPEED_ID     = UUID.fromString("a1f3c0de-0001-4a00-8000-000000000001");
    private static final UUID ATTACK_ID    = UUID.fromString("a1f3c0de-0002-4a00-8000-000000000002");
    private static final UUID STEP_ID      = UUID.fromString("a1f3c0de-0003-4a00-8000-000000000003");
    private static final UUID KB_RES_ID    = UUID.fromString("a1f3c0de-0004-4a00-8000-000000000004");
    private static final UUID ARMOR_ID     = UUID.fromString("a1f3c0de-0005-4a00-8000-000000000005");
    private static final UUID TOUGHNESS_ID = UUID.fromString("a1f3c0de-0006-4a00-8000-000000000006");

    private static PitouConfig.Common cfg() { return PitouConfig.COMMON; }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return; // servidor e autoritativo

        PitouCapability.with(player, data -> {
            if (data.isTransformed()) {
                applyEffects(player);
                applyAttributes(player);
            } else {
                removeAttributes(player);
            }
        });
    }

    private static void applyEffects(Player player) {
        // Duracao 400 reaplicada todo tick -> fica acima do limiar de "piscar" da visao noturna.
        if (cfg().nightVision.get()) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false, false));
        }
        if (cfg().waterSlowness.get() && player.isInWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, true, false, false));
        }
    }

    private static void applyAttributes(Player player) {
        boolean inWater = player.isInWater();

        // Velocidade: multiplicador. SUPRIMIDA na agua (perde o buff de velocidade).
        setModifier(player, Attributes.MOVEMENT_SPEED, SPEED_ID, "pitou_speed",
                cfg().speedBonus.get(), AttributeModifier.Operation.MULTIPLY_TOTAL, !inWater);

        setModifier(player, Attributes.ATTACK_DAMAGE, ATTACK_ID, "pitou_attack",
                cfg().attackBonus.get(), AttributeModifier.Operation.ADDITION, true);

        AttributeInstance stepInst = player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (stepInst != null) {
            setModifier(player, ForgeMod.STEP_HEIGHT_ADDITION.get(), STEP_ID, "pitou_step",
                    cfg().stepHeightBonus.get(), AttributeModifier.Operation.ADDITION, true);
        }

        setModifier(player, Attributes.KNOCKBACK_RESISTANCE, KB_RES_ID, "pitou_kbres",
                cfg().knockbackResistance.get(), AttributeModifier.Operation.ADDITION, true);

        setModifier(player, Attributes.ARMOR, ARMOR_ID, "pitou_armor",
                cfg().armorBonus.get(), AttributeModifier.Operation.ADDITION, true);

        setModifier(player, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_ID, "pitou_toughness",
                cfg().armorToughnessBonus.get(), AttributeModifier.Operation.ADDITION, true);
    }

    private static void removeAttributes(Player player) {
        remove(player, Attributes.MOVEMENT_SPEED, SPEED_ID);
        remove(player, Attributes.ATTACK_DAMAGE, ATTACK_ID);
        remove(player, ForgeMod.STEP_HEIGHT_ADDITION.get(), STEP_ID);
        remove(player, Attributes.KNOCKBACK_RESISTANCE, KB_RES_ID);
        remove(player, Attributes.ARMOR, ARMOR_ID);
        remove(player, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_ID);
    }

    /** Garante que o modificador exista (com o valor certo) ou seja removido. */
    private static void setModifier(Player player, Attribute attr, UUID id, String name,
                                    double amount, AttributeModifier.Operation op, boolean shouldHave) {
        AttributeInstance inst = player.getAttribute(attr);
        if (inst == null) return;
        AttributeModifier existing = inst.getModifier(id);
        if (shouldHave) {
            if (existing == null) {
                inst.addTransientModifier(new AttributeModifier(id, name, amount, op));
            } else if (existing.getAmount() != amount) {
                inst.removeModifier(id);
                inst.addTransientModifier(new AttributeModifier(id, name, amount, op));
            }
        } else if (existing != null) {
            inst.removeModifier(id);
        }
    }

    private static void remove(Player player, Attribute attr, UUID id) {
        AttributeInstance inst = player.getAttribute(attr);
        if (inst != null && inst.getModifier(id) != null) {
            inst.removeModifier(id);
        }
    }

    /** Dano de queda MUITO reduzido. */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        PitouCapability.with(player, data -> {
            if (data.isTransformed()) {
                event.setDamageMultiplier(event.getDamageMultiplier() * (float) (double) cfg().fallDamageMultiplier.get());
            }
        });
    }
}
