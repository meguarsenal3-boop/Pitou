package com.megu.neferpitou.client;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Barra de carga do pulo carregado. Aparece SO enquanto voce segura shift
 * (transformada). Enche conforme chargedJumpTicks vai para o maximo do config.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ChargedJumpHud {

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("pitou_charged_jump", ChargedJumpHud::render);
    }

    private static void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (!mc.player.isShiftKeyDown()) return;

        var opt = PitouCapability.get(mc.player).resolve();
        if (opt.isEmpty()) return;
        PitouData data = opt.get();
        if (!data.isTransformed()) return;

        int max = PitouConfig.COMMON.chargedJumpMaxHoldTicks.get();
        int charge = Math.min(data.getChargedJumpTicks(), max);
        float frac = max <= 0 ? 0f : (float) charge / (float) max;

        int barW = 90, barH = 7;
        int x = (width - barW) / 2;
        int y = height - 62;

        // borda + fundo
        g.fill(x - 1, y - 1, x + barW + 1, y + barH + 1, 0xAA000000);
        g.fill(x, y, x + barW, y + barH, 0xFF2A2A2A);

        // preenchimento
        int fillW = (int) (barW * frac);
        int color = frac >= 1.0f ? 0xFFFFD23F : 0xFFE0408F; // cheio = dourado, senao magenta
        if (fillW > 0) {
            g.fill(x, y, x + fillW, y + barH, color);
        }
    }
}
