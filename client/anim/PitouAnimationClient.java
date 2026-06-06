package com.megu.neferpitou.client.anim;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Decide qual animacao de corpo tocar no player LOCAL:
 *   - JUMP: enquanto esta carregando o pulo (chargedJumpTicks > 0)
 *   - SIT : parada (sem mover na horizontal, no chao, sem shift) por 5s
 *   - NONE: caso contrario
 * Mover na horizontal zera o tempo de "parada" e cancela ambas.
 *
 * (Por enquanto so o player local ve a propria animacao; sincronizar para os outros
 * players e um passo separado.)
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PitouAnimationClient {

    private PitouAnimationClient() {}

    private static final ResourceLocation SIT_ID = new ResourceLocation(Neferpitou.MODID, "sitting");
    private static final ResourceLocation JUMP_ID = new ResourceLocation(Neferpitou.MODID, "jump_charge");
    private static final int SIT_DELAY_TICKS = 100; // 5s

    private enum State { NONE, SIT, JUMP }

    private static State current = State.NONE;
    private static int idleTicks = 0;
    private static double prevX, prevZ;
    private static boolean hasPrev = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.isPaused()) return;

        double dx = player.getX() - prevX;
        double dz = player.getZ() - prevZ;
        boolean movedH = hasPrev && (dx * dx + dz * dz) > 0.0001;
        prevX = player.getX();
        prevZ = player.getZ();
        hasPrev = true;

        PitouData data = PitouCapability.get(player).resolve().orElse(null);
        boolean transformed = data != null && data.isTransformed();
        int charge = data == null ? 0 : data.getChargedJumpTicks();

        boolean canSit = transformed && player.onGround() && !movedH
                && !player.isShiftKeyDown() && charge == 0;
        idleTicks = canSit ? idleTicks + 1 : 0;

        State desired;
        if (!transformed) desired = State.NONE;
        else if (charge > 0) desired = State.JUMP;
        else if (idleTicks > SIT_DELAY_TICKS) desired = State.SIT;
        else desired = State.NONE;

        if (desired != current) {
            apply(player, desired);
            current = desired;
        }
    }

    @SuppressWarnings("unchecked")
    private static void apply(LocalPlayer player, State state) {
        Object raw = PlayerAnimationAccess.getPlayerAssociatedData(player).get(PitouAnimationSetup.LAYER_ID);
        if (!(raw instanceof ModifierLayer)) {
            Neferpitou.LOGGER.warn("[Pitou] layer de animacao NULL/invalida ({}). Animacao nao vai tocar.", raw);
            return;
        }
        ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>) raw;

        KeyframeAnimation anim = switch (state) {
            case SIT -> PlayerAnimationRegistry.getAnimation(SIT_ID);
            case JUMP -> PlayerAnimationRegistry.getAnimation(JUMP_ID);
            case NONE -> null;
        };
        Neferpitou.LOGGER.info("[Pitou] anim -> {} (carregada={})", state, anim != null);

        if (anim == null) {
            layer.setAnimation(null);
        } else {
            layer.setAnimation(new KeyframeAnimationPlayer(anim));
        }
    }
}
