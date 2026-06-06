package com.megu.neferpitou.network;

import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet servidor -> client. Carrega uma copia completa do PitouData (via NBT)
 * para o client poder reagir (camera, auras, HUD). Como o estado e pequeno,
 * mandar o NBT inteiro e simples e seguro.
 */
public class SyncPitouDataPacket {

    private final CompoundTag nbt;

    public SyncPitouDataPacket(PitouData data) {
        CompoundTag tag = new CompoundTag();
        data.saveNBT(tag);
        this.nbt = tag;
    }

    private SyncPitouDataPacket(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public static void encode(SyncPitouDataPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.nbt);
    }

    public static SyncPitouDataPacket decode(FriendlyByteBuf buf) {
        return new SyncPitouDataPacket(buf.readNbt());
    }

    public static void handle(SyncPitouDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Roda no client. Aplica o estado recebido no player local.
            ClientHandler.apply(msg.nbt);
        });
        ctx.get().setPacketHandled(true);
    }

    /** Isolado numa classe interna para nao carregar classes de client no servidor. */
    private static final class ClientHandler {
        static void apply(CompoundTag nbt) {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player == null) return;
            PitouCapability.with(mc.player, data -> data.loadNBT(nbt));
        }
    }
}
