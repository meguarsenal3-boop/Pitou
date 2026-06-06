package com.megu.neferpitou.nen;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;
import java.util.UUID;

/**
 * Da Nen aos mobs hostis ao spawnarem (chance/forca por NenTier) e dropa o cerebro
 * quando um mob com Nen morre.
 *
 * Os boosts sao modificadores PERMANENTES (salvos com o mob), e marcamos o mob com
 * uma tag no persistent data, entao recarregar o mundo NAO reaplica os boosts. Usamos
 * FinalizeSpawn (so dispara em spawn de verdade, nao em load de chunk).
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NenMobHandler {

    private NenMobHandler() {}

    public static final String NEN_KEY = "neferpitou:nen"; // guarda o multiplier no persistent data
    private static final Random RANDOM = new Random();

    private static final UUID HP_ID    = UUID.fromString("11111111-0000-4000-8000-0000000000a1");
    private static final UUID ATK_ID   = UUID.fromString("11111111-0000-4000-8000-0000000000a2");
    private static final UUID ARMOR_ID = UUID.fromString("11111111-0000-4000-8000-0000000000a3");
    private static final UUID TOUGH_ID = UUID.fromString("11111111-0000-4000-8000-0000000000a4");
    private static final UUID SPEED_ID = UUID.fromString("11111111-0000-4000-8000-0000000000a5");
    private static final UUID KB_ID    = UUID.fromString("11111111-0000-4000-8000-0000000000a6");

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        Mob mob = event.getEntity();
        if (mob.level().isClientSide()) return;
        if (!(mob instanceof Enemy)) return;
        if (mob.getPersistentData().contains(NEN_KEY)) return; // ja tem

        var cfg = PitouConfig.COMMON;
        if (!cfg.nenEnabled.get()) return;

        NenTier tier = NenTier.roll(RANDOM.nextDouble(), cfg.nenMaxMultiplier.get());
        if (tier == null) return;

        applyNen(mob, tier, cfg);
    }

    private static void applyNen(Mob mob, NenTier tier, PitouConfig.Common cfg) {
        int m = tier.multiplier;
        double mult = m - 1.0; // MULTIPLY_TOTAL: total = base * (1 + mult) = base * m

        addMult(mob, Attributes.MAX_HEALTH, HP_ID, "nen_hp", mult);
        addMult(mob, Attributes.ATTACK_DAMAGE, ATK_ID, "nen_atk", mult);
        addMult(mob, Attributes.ARMOR, ARMOR_ID, "nen_armor", mult);
        addMult(mob, Attributes.ARMOR_TOUGHNESS, TOUGH_ID, "nen_tough", mult);

        // velocidade: bonus reduzido (m / divisor). 2x -> +2%, 1000x -> +1000%
        double speedBonus = m / cfg.nenSpeedDivisor.get();
        addMult(mob, Attributes.MOVEMENT_SPEED, SPEED_ID, "nen_speed", speedBonus);

        // resistencia a knockback pra nao sair voando (capa em 1.0)
        addAmount(mob, Attributes.KNOCKBACK_RESISTANCE, KB_ID, "nen_kb", Math.min(1.0, m * 0.01));

        mob.setHealth(mob.getMaxHealth());
        mob.getPersistentData().putInt(NEN_KEY, m);

        mob.setCustomName(Component.literal("Nen x" + m));
        mob.setCustomNameVisible(false);
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide()) return;
        if (!dead.getPersistentData().contains(NEN_KEY)) return;

        var cfg = PitouConfig.COMMON;
        if (RANDOM.nextDouble() >= cfg.nenBrainDropChance.get()) return;
        if (NenRegistry.NEN_BRAIN_ITEM.get() == null) return;

        ItemStack stack = new ItemStack(NenRegistry.NEN_BRAIN_ITEM.get());
        ItemEntity drop = new ItemEntity(dead.level(), dead.getX(), dead.getY() + 0.5, dead.getZ(), stack);
        drop.setDefaultPickUpDelay();
        event.getDrops().add(drop);
    }

    private static void addMult(Mob mob, Attribute attr, UUID id, String name, double amount) {
        AttributeInstance inst = mob.getAttribute(attr);
        if (inst == null) return;
        if (inst.getModifier(id) != null) inst.removeModifier(id);
        inst.addPermanentModifier(new AttributeModifier(id, name, amount,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void addAmount(Mob mob, Attribute attr, UUID id, String name, double amount) {
        AttributeInstance inst = mob.getAttribute(attr);
        if (inst == null) return;
        if (inst.getModifier(id) != null) inst.removeModifier(id);
        inst.addPermanentModifier(new AttributeModifier(id, name, amount,
                AttributeModifier.Operation.ADDITION));
    }
}
