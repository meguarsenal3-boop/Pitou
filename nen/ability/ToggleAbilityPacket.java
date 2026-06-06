package com.megu.neferpitou.nen.ability;

import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouMode;
import com.megu.neferpitou.network.PitouNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> servidor: alterna uma habilidade de Nen. */
public class ToggleAbilityPacket {

    private final NenAbility ability;

    public ToggleAbilityPacket(NenAbility ability) {
        this.ability = ability;
    }

    public static void encode(ToggleAbilityPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.ability.ordinal());
    }

    public static ToggleAbilityPacket decode(FriendlyByteBuf buf) {
        return new ToggleAbilityPacket(NenAbility.byId(buf.readVarInt()));
    }

    public static void handle(ToggleAbilityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;

            PitouCapability.with(sp, data -> {
                if (!data.isTransformed()) {
                    sp.displayClientMessage(Component.literal("Transforme-se primeiro."), true);
                    return;
                }
                if (!data.hasUnlocked(PitouMode.NEN)) {
                    sp.displayClientMessage(Component.literal("Voce ainda nao despertou o Nen."), true);
                    return;
                }
                switch (msg.ability) {
                    case RYU -> data.setRyuActive(!data.isRyuActive());
                    case EN  -> data.setEnActive(!data.isEnActive());
                    case REN -> data.setRenActive(!data.isRenActive());
                }
            });

            PitouNetwork.syncTo(sp);
        });
        ctx.get().setPacketHandled(true);
    }
}
