package com.megu.neferpitou.play;

import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.network.PitouNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> servidor: alterna o modo "brincar com a presa". */
public class TogglePlayPacket {

    public TogglePlayPacket() {}

    public static void encode(TogglePlayPacket msg, FriendlyByteBuf buf) {}

    public static TogglePlayPacket decode(FriendlyByteBuf buf) {
        return new TogglePlayPacket();
    }

    public static void handle(TogglePlayPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            PitouCapability.with(sp, data -> {
                if (!data.isTransformed()) {
                    sp.displayClientMessage(Component.literal("Transforme-se primeiro."), true);
                    return;
                }
                boolean now = !data.isPlayWithPrey();
                data.setPlayWithPrey(now);
                sp.displayClientMessage(Component.literal(now
                        ? "Modo brincar: ligado (dano nerfado)."
                        : "Modo brincar: desligado."), true);
            });
            PitouNetwork.syncTo(sp);
        });
        ctx.get().setPacketHandled(true);
    }
}
