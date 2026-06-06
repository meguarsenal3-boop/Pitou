package com.megu.neferpitou.nen.ability;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Teclas das habilidades de Nen (categoria Neferpitou, remapeavel nos Controles). */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NenKeyMappings {

    private static final String CAT = "key.categories.neferpitou";

    public static final KeyMapping RYU = new KeyMapping("key.neferpitou.ryu", GLFW.GLFW_KEY_R, CAT);
    public static final KeyMapping EN  = new KeyMapping("key.neferpitou.en",  GLFW.GLFW_KEY_N, CAT);
    public static final KeyMapping REN = new KeyMapping("key.neferpitou.ren", GLFW.GLFW_KEY_H, CAT);

    @SubscribeEvent
    public static void onRegister(RegisterKeyMappingsEvent event) {
        event.register(RYU);
        event.register(EN);
        event.register(REN);
    }
}
