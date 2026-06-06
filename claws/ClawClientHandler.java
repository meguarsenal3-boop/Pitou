package com.megu.neferpitou.claws;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Le a tecla das garras a cada tick do client e envia o pedido de toggle ao servidor.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClawClientHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().player == null) return;

        // consumeClick() retorna true uma vez por aperto (lida com varios apertos no mesmo tick).
        while (PitouKeyMappings.CLAWS.consumeClick()) {
            ClawNetwork.sendToggle();
        }
    }
}
