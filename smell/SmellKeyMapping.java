package com.megu.neferpitou.smell;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Keybind do "cheirar". SEGURE a tecla para cheirar. Padrao: G.
 * Remapeavel no menu de Controles (categoria Neferpitou).
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SmellKeyMapping {

    public static final KeyMapping SMELL = new KeyMapping(
            "key.neferpitou.smell",
            GLFW.GLFW_KEY_G,
            "key.categories.neferpitou"
    );

    @SubscribeEvent
    public static void onRegister(RegisterKeyMappingsEvent event) {
        event.register(SMELL);
    }
}
