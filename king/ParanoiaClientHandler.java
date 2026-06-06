package com.megu.neferpitou.king;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/**
 * Lado CLIENT da paranoia: de vez em quando da leves "flickadas" na camera, com
 * frequencia e tamanho proporcionais ao nivel de paranoia (0-4) sincronizado do
 * servidor. Nivel 0 = nada.
 *
 * Usa java.util.Random de proposito (em vez de RandomSource.create()): aquele e
 * metodo estatico de interface e quebra o reobf no jar de producao.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ParanoiaClientHandler {

    private ParanoiaClientHandler() {}

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.isPaused()) return;

        PitouData data = PitouCapability.get(player).resolve().orElse(null);
        if (data == null) return;
        int level = data.getParanoia();
        if (level <= 0 || !data.isParanoiaActive()) return;

        var cfg = PitouConfig.COMMON;

        double chance = level * cfg.paranoiaFlickChancePerLevel.get();
        if (RANDOM.nextDouble() >= chance) return;

        double deg = cfg.paranoiaFlickMaxDeg.get() * (level / 4.0);
        float dYaw = (float) ((RANDOM.nextDouble() * 2.0 - 1.0) * deg);
        float dPitch = (float) ((RANDOM.nextDouble() * 2.0 - 1.0) * deg * 0.5);

        float newPitch = player.getXRot() + dPitch;
        newPitch = Math.max(-90.0F, Math.min(90.0F, newPitch));

        player.setYRot(player.getYRot() + dYaw);
        player.setXRot(newPitch);
    }
}
