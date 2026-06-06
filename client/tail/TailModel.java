package com.megu.neferpitou.client.tail;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** Aponta para os arquivos do rabo. A textura o davig manda depois. */
public class TailModel extends GeoModel<Tail> {

    private static final ResourceLocation GEO =
            new ResourceLocation(Neferpitou.MODID, "geo/tail.geo.json");
    private static final ResourceLocation TEX =
            new ResourceLocation(Neferpitou.MODID, "textures/entity/tail.png");
    private static final ResourceLocation ANIM =
            new ResourceLocation(Neferpitou.MODID, "animations/tail.animation.json");

    @Override
    public ResourceLocation getModelResource(Tail animatable) { return GEO; }

    @Override
    public ResourceLocation getTextureResource(Tail animatable) { return TEX; }

    @Override
    public ResourceLocation getAnimationResource(Tail animatable) { return ANIM; }
}
