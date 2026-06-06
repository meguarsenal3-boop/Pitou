package com.megu.neferpitou.threat;

import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Npc;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Classifica a forca de uma entidade num ThreatLevel (7 tiers).
 *
 * Prioridade:
 *   1) ThreatOverrides (lista do KubeJS) - manda em tudo.
 *   2) Tags de EntityType (datapack): force_apex/strong/weak/trivial.
 *   3) Passivo -> TRIVIAL.
 *   4) Score calculado -> faixas de tier (config, secao [threat]).
 *
 * Score = vidaEfetiva + (threatAttackWeight * danoEfetivo).
 * vidaEfetiva = vidaMax * (1 + armorWeight*armadura + toughnessWeight*tenacidade).
 */
public final class ThreatClassifier {

    private ThreatClassifier() {}

    public static final TagKey<net.minecraft.world.entity.EntityType<?>> FORCE_APEX =
            TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, rl("force_apex"));
    public static final TagKey<net.minecraft.world.entity.EntityType<?>> FORCE_STRONG =
            TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, rl("force_strong"));
    public static final TagKey<net.minecraft.world.entity.EntityType<?>> FORCE_WEAK =
            TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, rl("force_weak"));
    public static final TagKey<net.minecraft.world.entity.EntityType<?>> FORCE_TRIVIAL =
            TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, rl("force_trivial"));

    private static ResourceLocation rl(String path) {
        return new ResourceLocation("neferpitou", path);
    }

    private record Cached(ThreatLevel level, long expiresAtTick) {}
    private static final WeakHashMap<UUID, Cached> CACHE = new WeakHashMap<>();

    public static ThreatLevel classify(LivingEntity entity, long gameTime) {
        UUID id = entity.getUUID();
        Cached c = CACHE.get(id);
        if (c != null && gameTime < c.expiresAtTick()) {
            return c.level();
        }
        ThreatLevel level = compute(entity);
        long ttl = PitouConfig.COMMON.threatCacheTtlTicks.get();
        CACHE.put(id, new Cached(level, gameTime + ttl));
        return level;
    }

    public static ThreatLevel computeFresh(LivingEntity entity) {
        return compute(entity);
    }

    private static ThreatLevel compute(LivingEntity entity) {
        // 1) override do KubeJS (prioridade total)
        ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        ThreatLevel override = ThreatOverrides.get(typeId);
        if (override != null) return override;

        // 2) tags de datapack
        var type = entity.getType();
        if (type.is(FORCE_APEX)) return ThreatLevel.APEX;
        if (type.is(FORCE_STRONG)) return ThreatLevel.STRONG;
        if (type.is(FORCE_WEAK)) return ThreatLevel.WEAK;
        if (type.is(FORCE_TRIVIAL)) return ThreatLevel.TRIVIAL;

        // 3) passivo -> TRIVIAL
        if (isPassive(entity)) return ThreatLevel.TRIVIAL;

        // 4) score -> faixa de tier
        double score = score(entity);
        var cfg = PitouConfig.COMMON;
        if (score <= cfg.tierTrivialMax.get()) return ThreatLevel.TRIVIAL;
        if (score <= cfg.tierWeakMax.get()) return ThreatLevel.WEAK;
        if (score <= cfg.tierMediumMax.get()) return ThreatLevel.MEDIUM;
        if (score <= cfg.tierStrongMax.get()) return ThreatLevel.STRONG;
        if (score <= cfg.tierReallyStrongMax.get()) return ThreatLevel.REALLY_STRONG;
        if (score <= cfg.tierApexMax.get()) return ThreatLevel.APEX;
        return ThreatLevel.PREY;
    }

    private static boolean isPassive(LivingEntity entity) {
        if (entity instanceof Enemy) return false;
        if (entity instanceof Animal) return true;
        if (entity instanceof Npc) return true;
        if (entity instanceof NeutralMob neutral) {
            return !neutral.isAngry() && !hasTarget(entity);
        }
        if (entity instanceof Mob) {
            return !hasTarget(entity);
        }
        return false;
    }

    private static boolean hasTarget(LivingEntity entity) {
        return entity instanceof Mob mob && mob.getTarget() != null;
    }

    public static double score(LivingEntity entity) {
        var cfg = PitouConfig.COMMON;

        double maxHp = entity.getMaxHealth();
        double armor = getAttr(entity, Attributes.ARMOR, 0.0);
        double toughness = getAttr(entity, Attributes.ARMOR_TOUGHNESS, 0.0);

        double effectiveHp = maxHp * (1.0
                + cfg.threatArmorWeight.get() * armor
                + cfg.threatToughnessWeight.get() * toughness);

        double attack = cfg.threatFallbackAttack.get();
        var attackInst = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackInst != null) {
            attack = attackInst.getValue();
        }

        return effectiveHp + cfg.threatAttackWeight.get() * attack;
    }

    private static double getAttr(LivingEntity entity, Attribute attr, double fallback) {
        var inst = entity.getAttribute(attr);
        return inst != null ? inst.getValue() : fallback;
    }

    public static void invalidate(Entity entity) {
        CACHE.remove(entity.getUUID());
    }
}
