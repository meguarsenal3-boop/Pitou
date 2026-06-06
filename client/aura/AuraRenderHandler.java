package com.megu.neferpitou.client.aura;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.hunt.HuntClientState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Aura GLOBAL pelo metodo da "casca": depois que a entidade e desenhada, o seu modelo
 * e desenhado de novo, levemente inflado, com uma textura de aura emissiva (brilha no
 * escuro). Reusa o modelo de qualquer LivingEntity, entao serve pra player e mobs.
 *
 * Por enquanto:
 *   - branca em qualquer ser com Nen (mob cujo nome custom comeca com "Nen x");
 *   - vermelha no player local quando esta cacando (HuntClientState).
 * Depois da pra ligar outras cores a outras condicoes (modelos invocados, etc).
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AuraRenderHandler {

    private AuraRenderHandler() {}

    public static final ResourceLocation AURA_WHITE =
            new ResourceLocation(Neferpitou.MODID, "textures/entity/aura_white.png");
    public static final ResourceLocation AURA_RED =
            new ResourceLocation(Neferpitou.MODID, "textures/entity/aura_red.png");

    private static final float INFLATE = 1.08F;     // quanto a casca cresce
    private static final float ALPHA = 0.6F;        // opacidade da aura

    @SubscribeEvent
    public static void onRenderPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        ResourceLocation tex = auraFor(entity);
        if (tex == null) return;

        EntityModel<?> model = event.getRenderer().getModel();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();

        RenderType renderType = RenderType.entityTranslucentEmissive(tex);
        VertexConsumer vc = buffer.getBuffer(renderType);

        pose.pushPose();
        pose.scale(INFLATE, INFLATE, INFLATE);
        renderModel(model, pose, vc);
        pose.popPose();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void renderModel(EntityModel model, PoseStack pose, VertexConsumer vc) {
        model.renderToBuffer(pose, vc, LightTexture.FULL_BRIGHT, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, ALPHA);
    }

    /** Decide a cor da aura (ou null se nao tem). */
    private static ResourceLocation auraFor(LivingEntity entity) {
        // player local cacando -> vermelha
        Minecraft mc = Minecraft.getInstance();
        if (entity == mc.player && HuntClientState.targetId >= 0) {
            PitouData data = PitouCapability.get(mc.player).resolve().orElse(null);
            if (data != null && data.isTransformed()) return AURA_RED;
        }

        // mob com Nen (nome custom "Nen x...") -> branca
        Component name = entity.getCustomName();
        if (name != null && name.getString().startsWith("Nen x")) {
            return AURA_WHITE;
        }

        return null;
    }
}
