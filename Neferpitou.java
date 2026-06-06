package com.megu.neferpitou;

import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.command.PitouCommands;
import com.megu.neferpitou.config.PitouConfig;
import com.megu.neferpitou.nen.NenRegistry;
import com.megu.neferpitou.network.PitouNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Ponto de entrada do mod Neferpitou.
 *
 * Arquitetura geral:
 *  - O estado da Pitou (transformada, tier desbloqueado, Rei, paranoia, maestrias)
 *    vive numa Capability ligada ao player (ver pacote 'capability'). Autoritativo no servidor.
 *  - O servidor sincroniza esse estado para o client via packet (ver pacote 'network').
 *  - Efeitos visuais/camera (flicks, sustos, auras) sao puro client, disparados por packet.
 *  - Numeros ajustaveis vivem no config .toml (ver pacote 'config'), editavel sem recompilar.
 *  - Classificacao de forca de mob (En, caca) usa ThreatClassifier (ver pacote 'threat').
 */
@Mod(Neferpitou.MODID)
public class Neferpitou {

    public static final String MODID = "neferpitou";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Neferpitou() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(this::commonSetup);

        // Capabilities sao registradas no mod bus.
        PitouCapability.register(modBus);
        NenRegistry.register(modBus);
        // Config common (.toml), editavel a mao em config/neferpitou-common.toml
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PitouConfig.SPEC, "neferpitou-common.toml");

        // Eventos de jogo (attach de capability, persistencia, sync) no bus do Forge.
        MinecraftForge.EVENT_BUS.register(PitouCapability.class);
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Neferpitou carregada. Vamos cacar.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(PitouNetwork::register);
    }

    @SubscribeEvent
    public void onRegisterCommands(final RegisterCommandsEvent event) {
        PitouCommands.register(event.getDispatcher());
    }
}
