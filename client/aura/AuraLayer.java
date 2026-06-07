package com.megu.neferpitou.client.aura;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Desenha a aura como uma "casca" emissiva por cima da entidade, REUSANDO o modelo do
 * proprio renderer. Por ser um RenderLayer, este metodo roda dentro do PoseStack que o
 * LivingEntityRenderer ja rotacionou (setupRotations), espelhou (scale -1,-1,1) e animou
 * (setupAnim). Por isso a aura fica na orientacao certa e gira junto com a entidade --
 * exatamente como o rabo (que tambem e uma layer) ja funcionava.
 */
public class AuraLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public AuraLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        ResourceLocation tex = AuraRenderHandler.auraFor(entity);
        if (tex == null) return;

        M model = getParentModel();
        RenderType renderType = RenderType.entityTranslucentEmissive(tex);
        VertexConsumer vc = buffer.getBuffer(renderType);

        poseStack.pushPose();
        poseStack.scale(AuraRenderHandler.INFLATE, AuraRenderHandler.INFLATE, AuraRenderHandler.INFLATE);
        model.renderToBuffer(poseStack, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, AuraRenderHandler.ALPHA);
        poseStack.popPose();
    }
}
