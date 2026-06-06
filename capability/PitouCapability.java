package com.megu.neferpitou.capability;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.network.PitouNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.NonNullConsumer;

/**
 * Liga o PitouData a cada player, persiste atraves de morte/dimensao,
 * e sincroniza o estado para o client nos momentos certos.
 */
public class PitouCapability {

    public static final ResourceLocation ID = new ResourceLocation(Neferpitou.MODID, "pitou_data");

    /** Registrado no MOD bus. */
    public static void register(IEventBus modBus) {
        modBus.addListener(PitouCapability::onRegisterCapabilities);
        modBus.addListener(PitouCapability::onCommonSetup);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PitouData.class);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // reservado para setup futuro se necessario
    }

    // ---- Helper de acesso rapido ----
    public static LazyOptional<PitouData> get(Player player) {
        return player.getCapability(PitouDataProvider.PITOU_DATA);
    }

    /** Roda algo com o PitouData se existir. */
    public static void with(Player player, NonNullConsumer<PitouData> action) {
        get(player).ifPresent(action);
    }

    // ======================= eventos do FORGE bus =======================

    @SubscribeEvent
    public static void onAttach(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PitouDataProvider.PITOU_DATA).isPresent()) {
                event.addCapability(ID, new PitouDataProvider());
            }
        }
    }

    /** Copia o estado ao morrer/clonar (respawn ou volta do End). */
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        // Reativa a capability do player antigo para conseguir ler.
        event.getOriginal().reviveCaps();
        get(event.getOriginal()).ifPresent(oldData ->
                get(event.getEntity()).ifPresent(newData -> newData.copyFrom(oldData)));
        event.getOriginal().invalidateCaps();
    }

    /** Sincroniza ao entrar no mundo. */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            PitouNetwork.syncTo(sp);
        }
    }

    /** Sincroniza ao renascer. */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            PitouNetwork.syncTo(sp);
        }
    }

    /** Sincroniza ao trocar de dimensao. */
    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            PitouNetwork.syncTo(sp);
        }
    }
}