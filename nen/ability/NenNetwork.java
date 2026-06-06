package com.megu.neferpitou.nen.ability;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Canal separado para as habilidades de Nen (Ryu/En/Ren). Auto-registra no setup.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NenNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Neferpitou.MODID, "nen"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    @SubscribeEvent
    public static void onSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
                CHANNEL.messageBuilder(ToggleAbilityPacket.class, 0)
                        .encoder(ToggleAbilityPacket::encode)
                        .decoder(ToggleAbilityPacket::decode)
                        .consumerMainThread(ToggleAbilityPacket::handle)
                        .add());
    }

    public static void sendToggle(NenAbility ability) {
        CHANNEL.sendToServer(new ToggleAbilityPacket(ability));
    }
}
