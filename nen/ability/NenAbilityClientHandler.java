package com.megu.neferpitou.nen.ability;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Le as teclas de Nen e manda o toggle ao servidor. */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class NenAbilityClientHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().player == null) return;

        while (NenKeyMappings.RYU.consumeClick()) NenNetwork.sendToggle(NenAbility.RYU);
        while (NenKeyMappings.EN.consumeClick())  NenNetwork.sendToggle(NenAbility.EN);
        while (NenKeyMappings.REN.consumeClick()) NenNetwork.sendToggle(NenAbility.REN);
    }
}
