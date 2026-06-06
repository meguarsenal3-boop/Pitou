package com.megu.neferpitou.client.anim;

import com.megu.neferpitou.Neferpitou;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Registra UMA camada de animacao (ModifierLayer) por player no PlayerAnimator.
 * A logica de QUAL animacao tocar fica no PitouAnimationClient.
 *
 * OBS: a API do player-animation-lib pode ter nomes ligeiramente diferentes na versao
 * do pack. Se nao compilar, o erro vai apontar o import/metodo exato a corrigir.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PitouAnimationSetup {

    private PitouAnimationSetup() {}

    public static final ResourceLocation LAYER_ID = new ResourceLocation(Neferpitou.MODID, "body");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                        LAYER_ID,
                        1000,
                        PitouAnimationSetup::create));
    }

    private static IAnimation create(AbstractClientPlayer player) {
        return new ModifierLayer<>();
    }
}
