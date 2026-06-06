package com.megu.neferpitou.claws;

import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.network.PitouNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Client -> servidor. Pede para alternar as garras (dentro/fora).
 * Sem payload: o servidor so inverte o estado atual.
 *
 * Ao EXPOR as garras, toca o som do Saint's Dragons (se o mod estiver presente),
 * para todos os players proximos ouvirem (categoria MASTER).
 */
public class ToggleClawsPacket {

    // Som do mod Saint's Dragons. Se o mod nao estiver carregado, simplesmente nao toca.
    private static final ResourceLocation CLAW_SOUND =
            new ResourceLocation("saintsdragons", "varasuchus_claw");

    public ToggleClawsPacket() {}

    public static void encode(ToggleClawsPacket msg, FriendlyByteBuf buf) {
        // sem dados
    }

    public static ToggleClawsPacket decode(FriendlyByteBuf buf) {
        return new ToggleClawsPacket();
    }

    public static void handle(ToggleClawsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;

            PitouCapability.with(sp, data -> {
                if (!data.isTransformed()) {
                    sp.displayClientMessage(Component.literal("Transforme-se primeiro."), true);
                    return;
                }
                boolean now = !data.isClawsExposed();
                data.setClawsExposed(now);
                if (now) {
                    playClawSound(sp);
                }
            });

            // Reaproveita o sync existente para empurrar o estado novo ao client.
            PitouNetwork.syncTo(sp);
        });
        ctx.get().setPacketHandled(true);
    }

    private static void playClawSound(ServerPlayer sp) {
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(CLAW_SOUND);
        if (sound == null) return; // Saint's Dragons ausente: nao crasha, so nao toca.
        if (sp.level() instanceof ServerLevel level) {
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    sound, SoundSource.MASTER, 1.0F, 1.0F);
        }
    }
}
