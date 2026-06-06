package com.megu.neferpitou.smell;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> servidor: liga/desliga o estado de "cheirando" (para o glow no servidor). */
public class SmellTogglePacket {

    private final boolean smelling;

    public SmellTogglePacket(boolean smelling) {
        this.smelling = smelling;
    }

    public static void encode(SmellTogglePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.smelling);
    }

    public static SmellTogglePacket decode(FriendlyByteBuf buf) {
        return new SmellTogglePacket(buf.readBoolean());
    }

    public static void handle(SmellTogglePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            SmellServerHandler.setSmelling(sp, msg.smelling);
        });
        ctx.get().setPacketHandled(true);
    }
}
