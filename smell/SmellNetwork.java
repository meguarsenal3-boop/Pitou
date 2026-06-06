package com.megu.neferpitou.smell;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Canal proprio do cheiro (client -> servidor: "estou cheirando?").
 * Separado para nao mexer no PitouNetwork nem no ClawNetwork.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SmellNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Neferpitou.MODID, "smell"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    @SubscribeEvent
    public static void onSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
                CHANNEL.messageBuilder(SmellTogglePacket.class, 0)
                        .encoder(SmellTogglePacket::encode)
                        .decoder(SmellTogglePacket::decode)
                        .consumerMainThread(SmellTogglePacket::handle)
                        .add());
    }

    public static void sendSmelling(boolean smelling) {
        CHANNEL.sendToServer(new SmellTogglePacket(smelling));
    }
}
