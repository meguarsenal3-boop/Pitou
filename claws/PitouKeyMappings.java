package com.megu.neferpitou.claws;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Registra o keybind das garras. Tecla padrao: V (pode ser remapeada no menu
 * de Controles do Minecraft, categoria "Neferpitou").
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PitouKeyMappings {

    public static final KeyMapping CLAWS = new KeyMapping(
            "key.neferpitou.claws",       // chave de traducao (lang)
            GLFW.GLFW_KEY_V,              // tecla padrao
            "key.categories.neferpitou"   // categoria
    );

    @SubscribeEvent
    public static void onRegister(RegisterKeyMappingsEvent event) {
        event.register(CLAWS);
    }
}
