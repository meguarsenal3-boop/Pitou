package com.megu.neferpitou.client.aura;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.hunt.HuntClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Aura GLOBAL pelo metodo da "casca": o modelo da entidade e desenhado de novo, levemente
 * inflado, com uma textura de aura emissiva (brilha no escuro). Reusa o modelo de qualquer
 * LivingEntity, entao serve pra player e mobs.
 *
 * O desenho em si acontece dentro de um {@link AuraLayer} (RenderLayer), registrado em
 * {@link AuraLayerRegister}. Por ser uma layer, roda DENTRO do PoseStack ja rotacionado,
 * espelhado e animado pelo LivingEntityRenderer -> a aura fica na orientacao certa e gira
 * junto com a entidade. (Antes isso era feito no RenderLivingEvent.Post, que dispara depois
 * do popPose do renderer: sem o flip nem a rotacao do corpo, por isso a aura saia de cabeca
 * pra baixo e presa numa unica direcao.)
 *
 * Esta classe agora so guarda as texturas/constantes e decide a cor da aura.
 *
 * Por enquanto:
 *   - branca em qualquer ser com Nen (mob cujo nome custom comeca com "Nen x");
 *   - vermelha no player local quando esta cacando (HuntClientState).
 */
public final class AuraRenderHandler {

    private AuraRenderHandler() {}

    public static final ResourceLocation AURA_WHITE =
            new ResourceLocation(Neferpitou.MODID, "textures/entity/aura_white.png");
    public static final ResourceLocation AURA_RED =
            new ResourceLocation(Neferpitou.MODID, "textures/entity/aura_red.png");

    // Quanto a "casca" da aura cresce. IMPORTANTE: como a aura e o modelo escalado no
    // PoseStack, valores altos AFASTAM a casca do corpo durante animacoes com membros
    // bem deslocados (sentar/pulo) -> a aura "sai do personagem". Por isso fica baixo
    // (so o suficiente pra nao dar z-fighting com a pele). Aumente se quiser a casca
    // mais grossa quando parado, sabendo que vai afastar mais nas animacoes grandes.
    public static final float INFLATE = 1.02F;
    public static final float ALPHA = 0.6F;        // opacidade da aura

    /** Decide a cor da aura (ou null se nao tem). */
    public static ResourceLocation auraFor(LivingEntity entity) {
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
