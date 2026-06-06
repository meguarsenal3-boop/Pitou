package com.megu.neferpitou.claws;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Canal de rede separado, so para as garras. Feito assim de proposito para nao
 * precisar editar o PitouNetwork existente (e arriscar mexer no seu pulo).
 *
 * Registrado automaticamente no setup pelo @EventBusSubscriber (bus MOD).
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClawNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Neferpitou.MODID, "claws"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    @SubscribeEvent
    public static void onSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
                CHANNEL.messageBuilder(ToggleClawsPacket.class, 0)
                        .encoder(ToggleClawsPacket::encode)
                        .decoder(ToggleClawsPacket::decode)
                        .consumerMainThread(ToggleClawsPacket::handle)
                        .add());
    }

    /** Client -> servidor: pede para alternar as garras. */
    public static void sendToggle() {
        CHANNEL.sendToServer(new ToggleClawsPacket());
    }
}
