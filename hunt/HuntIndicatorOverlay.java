package com.megu.neferpitou.hunt;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Mostra, no topo da tela, onde esta a presa forte (nome, distancia e direcao)
 * quando o player esta alto o suficiente (HuntClientState.seeingIndicator).
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HuntIndicatorOverlay {

    private static final String[] DIRS = {"N", "NE", "L", "SE", "S", "SO", "O", "NO"};

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("pitou_hunt_indicator", HuntIndicatorOverlay::render);
    }

    private static void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        if (!HuntClientState.seeingIndicator || HuntClientState.targetId == -1) return;

        Entity target = mc.level.getEntity(HuntClientState.targetId);
        if (target == null) return;

        double dx = target.getX() - mc.player.getX();
        double dz = target.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        // direcao em bussola (0 = norte/-Z)
        double angle = Math.toDegrees(Math.atan2(dx, -dz));
        if (angle < 0) angle += 360.0;
        String dir = DIRS[(int) Math.round(angle / 45.0) % 8];

        String name = target.getName().getString();
        Component text = Component.literal("\u25B2 Presa forte: " + name + " \u2014 "
                + (int) dist + "m  " + dir);

        int tw = mc.font.width(text);
        int x = (width - tw) / 2;
        int y = 8;

        g.fill(x - 4, y - 2, x + tw + 4, y + 11, 0x80000000);
        g.drawString(mc.font, text, x, y, 0xFFFF5555, true);
    }
}
