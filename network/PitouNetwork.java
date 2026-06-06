package com.megu.neferpitou.network;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PitouNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Neferpitou.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int id = 0;
    private static int nextId() { return id++; }

    public static void register() {
        CHANNEL.messageBuilder(SyncPitouDataPacket.class, nextId())
                .encoder(SyncPitouDataPacket::encode)
                .decoder(SyncPitouDataPacket::decode)
                .consumerMainThread(SyncPitouDataPacket::handle)
                .add();
    }

    /** Envia o estado atual do servidor para o client daquele player. */
    public static void syncTo(ServerPlayer player) {
        PitouCapability.with(player, data ->
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncPitouDataPacket(data)));
    }
}
