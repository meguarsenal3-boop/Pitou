package com.megu.neferpitou.client.tail;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Animatable singleton do rabo. Renderizado por cima do player (TailLayer).
 * Toca "tail_normal" (balanco) normalmente, e "tail_notmoving" (parado) quando
 * frozen = true (Ren ativo OU Rei perto). O frozen e setado antes de cada render.
 */
public class Tail implements GeoAnimatable {

    public static final Tail INSTANCE = new Tail();

    /** Setado pelo TailLayer antes de renderizar: true = rabo parado. */
    public static boolean frozen = false;

    private static final RawAnimation MOVE = RawAnimation.begin().thenLoop("tail_normal");
    private static final RawAnimation STILL = RawAnimation.begin().thenLoop("tail_notmoving");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "tail", 5, state -> {
            state.setAnimation(frozen ? STILL : MOVE);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object entity) {
        return software.bernie.geckolib.util.RenderUtils.getCurrentTick();
    }
}
