package com.megu.neferpitou.jump;

import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> Servidor: durante o dash do pulo, a trajetoria bateu num mob. O servidor
 * (autoritativo) aplica dano = ataque atual da Pitou * jumpHitMultiplier (inclui
 * garras/Nen, que sao modificadores no atributo).
 */
public class JumpAttackPacket {

    private final int targetId;

    public JumpAttackPacket(int targetId) {
        this.targetId = targetId;
    }

    public static void encode(JumpAttackPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.targetId);
    }

    public static JumpAttackPacket decode(FriendlyByteBuf buf) {
        return new JumpAttackPacket(buf.readVarInt());
    }

    public static void handle(JumpAttackPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Entity target = player.level().getEntity(msg.targetId);
            if (!(target instanceof LivingEntity living)) return;
            if (player.distanceToSqr(living) > 64.0) return; // anti-cheat basico

            double attack = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            double mult = PitouConfig.COMMON.jumpHitMultiplier.get();
            living.hurt(player.damageSources().playerAttack(player), (float) (attack * mult));
        });
        ctx.get().setPacketHandled(true);
    }
}
