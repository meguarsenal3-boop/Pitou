package com.megu.neferpitou.jump;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Canal proprio do pulo-ataque (auto-registra, nao depende do PitouNetwork). */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class JumpNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Neferpitou.MODID, "jump"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    @SubscribeEvent
    public static void onSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
                CHANNEL.messageBuilder(JumpAttackPacket.class, 0)
                        .encoder(JumpAttackPacket::encode)
                        .decoder(JumpAttackPacket::decode)
                        .consumerMainThread(JumpAttackPacket::handle)
                        .add());
    }

    public static void sendAttack(int targetId) {
        CHANNEL.sendToServer(new JumpAttackPacket(targetId));
    }
}
