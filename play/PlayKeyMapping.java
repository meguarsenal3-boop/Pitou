package com.megu.neferpitou.play;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Keybind do modo "brincar com a presa". Toggle. Padrao: B. */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PlayKeyMapping {

    public static final KeyMapping PLAY = new KeyMapping(
            "key.neferpitou.play",
            GLFW.GLFW_KEY_B,
            "key.categories.neferpitou"
    );

    @SubscribeEvent
    public static void onRegister(RegisterKeyMappingsEvent event) {
        event.register(PLAY);
    }
}
