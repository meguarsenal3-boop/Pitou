package com.megu.neferpitou.client.tail;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.client.aura.AuraRenderHandler;
import com.megu.neferpitou.hunt.HuntClientState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

/**
 * Desenha o rabo por cima do player, ancorado no corpo (body) para acompanhar as
 * animacoes do PlayerAnimator (sentar/pulo).
 *
 * AJUSTES IN-GAME (mexa nestes numeros se precisar):
 *  - FLIP_*  : orientacao. O espaco do modelo do player tem Y invertido em relacao ao
 *              GeckoLib (Y pra cima), por isso o flip; se ficar de cabeca pra baixo ou
 *              espelhado, mude os sinais.
 *  - ANCHOR_*: posicao do rabo (em blocos). Y sobe/desce, Z afasta/aproxima das costas.
 */
public class TailLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Neferpitou.MODID, "textures/entity/tail.png");

    // orientacao (corrige o "de cabeca pra baixo")
    private static final float FLIP_X = -1.0F;
    private static final float FLIP_Y = -1.0F;
    private static final float FLIP_Z = 1.0F;

    // ancoragem (TUNAVEL, em blocos)
    private static final double ANCHOR_X = 0.0D;
    private static final double ANCHOR_Y = 0.0D;
    private static final double ANCHOR_Z = 0.10D;

    private final GeoObjectRenderer<Tail> renderer;

    public TailLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
        this.renderer = new GeoObjectRenderer<>(new TailModel());
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        PitouData data = PitouCapability.get(player).resolve().orElse(null);
        if (data == null || !data.isTransformed()) return;

        // rabo parado quando Ren ativo ou Rei perto; senao balanca
        Tail.frozen = data.isRenActive() || data.isKingNear();

        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack); // segue o corpo animado
        poseStack.scale(FLIP_X, FLIP_Y, FLIP_Z);             // corrige orientacao
        poseStack.translate(ANCHOR_X, ANCHOR_Y, ANCHOR_Z);   // ancoragem

        RenderType rt = RenderType.entityCutoutNoCull(TEXTURE);
        renderer.render(poseStack, Tail.INSTANCE, buffer, rt, buffer.getBuffer(rt), packedLight);

        // aura vermelha no rabo quando o player local esta cacando
        if (player == Minecraft.getInstance().player && HuntClientState.targetId >= 0) {
            poseStack.pushPose();
            poseStack.scale(1.1F, 1.1F, 1.1F);
            RenderType aura = RenderType.entityTranslucentEmissive(AuraRenderHandler.AURA_RED);
            renderer.render(poseStack, Tail.INSTANCE, buffer, aura, buffer.getBuffer(aura), packedLight);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
