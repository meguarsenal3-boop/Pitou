package com.megu.neferpitou.client;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.smell.SmellClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Vinheta verde nas bordas da tela quando a Pitou esta enjoada de cheiro ruim.
 * Independe do setting "Distortion Effects", entao da feedback mesmo com a
 * distorcao de tela desligada.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NauseaOverlay {

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("pitou_nausea", NauseaOverlay::render);
    }

    private static void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        float level = SmellClientHandler.getNauseaLevel();
        if (level <= 0.01f) return;

        // Pulso leve para dar a sensacao de enjoo.
        long time = mc.level != null ? mc.level.getGameTime() : 0L;
        float pulse = 0.78f + 0.22f * (float) Math.sin(time * 0.2);

        int alpha = (int) (level * pulse * 140f); // 0..~140
        if (alpha <= 2) return;

        int top = (alpha << 24) | 0x4FA63D;          // verde com alpha
        int transparent = 0x004FA63D;                 // mesmo verde, alpha 0

        int edge = Math.max(24, Math.min(width, height) / 4);

        // Vinheta em cima e embaixo (fillGradient e vertical).
        g.fillGradient(0, 0, width, edge, top, transparent);
        g.fillGradient(0, height - edge, width, height, transparent, top);
    }
}
