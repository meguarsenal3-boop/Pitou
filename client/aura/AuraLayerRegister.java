package com.megu.neferpitou.client.aura;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Adiciona o {@link AuraLayer} a TODOS os renderers de entidades vivas (mobs) e tambem aos
 * renderers de player (skins default e slim). Assim a aura usa o modelo de cada entidade.
 *
 * OBS: em 1.20.1 o AddLayers nao expoe getEntityTypes(), entao iteramos o registro de
 * EntityType. event.getRenderer(...) so e valido para LivingEntityRenderer e estoura
 * ClassCastException para renderers que nao sao de LivingEntity (flecha, item, barco...),
 * por isso o try/catch + instanceof.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AuraLayerRegister {

    private AuraLayerRegister() {}

    @SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        // mobs / entidades vivas
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            try {
                EntityRenderer<?> r = event.getRenderer((EntityType) type);
                if (r instanceof LivingEntityRenderer lr) {
                    lr.addLayer(new AuraLayer(lr));
                }
            } catch (ClassCastException ignored) {
                // renderer que nao e de LivingEntity -> ignora
            }
        }

        // players (skins default e slim)
        for (String skin : event.getSkins()) {
            EntityRenderer<?> r = event.getSkin(skin);
            if (r instanceof LivingEntityRenderer lr) {
                lr.addLayer(new AuraLayer(lr));
            }
        }
    }
}
