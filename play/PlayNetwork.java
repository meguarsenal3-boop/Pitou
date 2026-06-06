package com.megu.neferpitou.play;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Canal proprio do modo brincar (client -> servidor: alterna o modo). */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PlayNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Neferpitou.MODID, "play"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    @SubscribeEvent
    public static void onSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
                CHANNEL.messageBuilder(TogglePlayPacket.class, 0)
                        .encoder(TogglePlayPacket::encode)
                        .decoder(TogglePlayPacket::decode)
                        .consumerMainThread(TogglePlayPacket::handle)
                        .add());
    }

    public static void sendToggle() {
        CHANNEL.sendToServer(new TogglePlayPacket());
    }
}
