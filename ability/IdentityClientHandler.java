package com.megu.neferpitou.ability;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.config.PitouConfig;
import com.megu.neferpitou.jump.JumpNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Passivas da IDENTIDADE no CLIENT do player local (movimento e mineracao).
 *
 * Pulo carregado = DASH SUSTENTADO: ao pular agachada, a velocidade calculada e
 * MANTIDA por chargedDashDurationTicks, sobrepondo gravidade/atrito. Assim
 * distancia = velocidade * tempo, previsivel e independente de direcao:
 *   - olhando reto pra frente, carga max -> chargedDashForwardBlocks
 *   - olhando reto pra cima,   carga max -> chargedDashUpwardBlocks
 *
 * Durante o dash, se a trajetoria encostar num mob: para o salto e manda o
 * servidor aplicar 2x dano (JumpAttackPacket).
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class IdentityClientHandler {

    // Estado do dash em andamento (client local apenas).
    private static int dashTicksLeft = 0;
    private static Vec3 dashVelocity = Vec3.ZERO;

    // Velocidade maxima por tick (blocos) que o servidor aceita sem acusar
    // "moved too quickly" e teleportar o player de volta (= "o pulo nao funciona").
    // O limite vanilla e ~10 blocos/tick; 7.0 deixa folga. Dashes longos (ex.: 500
    // blocos) so duram MAIS ticks em vez de serem barrados, percorrendo a distancia
    // inteira de verdade.
    private static final double MAX_TICK_SPEED = 7.0;

    // cooldown apos um pulo carregado + deteccao de movimento horizontal
    private static int cooldownLeft = 0;
    private static double prevX, prevZ;
    private static boolean hasPrev = false;

    private static PitouConfig.Common cfg() { return PitouConfig.COMMON; }

    private static boolean isLocal(Player player) {
        return player.level().isClientSide && player == Minecraft.getInstance().player;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isLocal(event.player)) return;
        Player player = event.player;

        // Movimento HORIZONTAL desde o ultimo tick (mudar so o Y NAO conta como mover).
        double dx = player.getX() - prevX;
        double dz = player.getZ() - prevZ;
        final boolean movedH = hasPrev && (dx * dx + dz * dz) > 0.0001; // ~0.01 bloco/tick
        prevX = player.getX();
        prevZ = player.getZ();
        hasPrev = true;

        PitouCapability.with(player, data -> {
            if (!data.isTransformed()) {
                data.setChargedJumpTicks(0);
                dashTicksLeft = 0;
                cooldownLeft = 0;
                return;
            }

            // 1) Dash em andamento: mantem a velocidade e checa colisoes.
            if (dashTicksLeft > 0) {
                // Bateu numa parede/teto? Encerra.
                if (player.horizontalCollision || player.verticalCollision) {
                    dashTicksLeft = 0;
                } else {
                    player.setDeltaMovement(dashVelocity);
                    player.hasImpulse = true;
                    player.fallDistance = 0.0F; // dash nao deve gerar dano de queda
                    dashTicksLeft--;

                    // Colisao com mob na trajetoria.
                    Entity hit = findMobInPath(player);
                    if (hit != null) {
                        JumpNetwork.sendAttack(hit.getId());
                        dashTicksLeft = 0;
                        // freia (mantem so um residuo pra nao travar seco)
                        player.setDeltaMovement(player.getDeltaMovement().scale(0.1));
                    }
                }
                return; // enquanto faz dash, nao acumula carga
            }

            // 2) Cooldown apos um pulo carregado: sem carga durante ele.
            if (cooldownLeft > 0) {
                cooldownLeft--;
                data.setChargedJumpTicks(0);
                return;
            }

            // 3) Carrega segurando shift e SEM mover na horizontal (vale no chao OU no ar).
            //    Mover na horizontal cancela; mudar so o Y (cair/subir) nao cancela.
            if (player.isShiftKeyDown() && !movedH) {
                int cap = cfg().chargedJumpMaxHoldTicks.get();
                data.setChargedJumpTicks(Math.min(cap, data.getChargedJumpTicks() + 1));
            } else {
                data.setChargedJumpTicks(0);
            }
        });
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isLocal(player)) return;

        PitouCapability.with(player, data -> {
            if (!data.isTransformed()) return;

            int charge = data.getChargedJumpTicks();
            if (player.isShiftKeyDown() && charge > 0) {
                double maxTicks = Math.max(1, cfg().chargedJumpMaxHoldTicks.get());
                double ratio = Math.min(1.0, charge / maxTicks);
                double minFrac = cfg().chargedDashMinChargeFraction.get();
                double eff = minFrac + (1.0 - minFrac) * ratio;

                double distH = cfg().chargedDashForwardBlocks.get() * eff;
                double distV = cfg().chargedDashUpwardBlocks.get() * eff;

                Vec3 look = player.getLookAngle(); // unitario, com pitch
                // Deslocamento TOTAL desejado: horizontal escala por distH, vertical por distV.
                Vec3 disp = new Vec3(look.x * distH, look.y * distV, look.z * distH);

                // A duracao configurada e o MINIMO; pra distancias grandes ela se estende
                // o quanto for preciso pra manter a velocidade por tick <= MAX_TICK_SPEED,
                // senao o servidor barra o movimento ("moved too quickly") e o dash falha.
                double minDuration = Math.max(1, cfg().chargedDashDurationTicks.get());
                double duration = Math.max(minDuration, Math.ceil(disp.length() / MAX_TICK_SPEED));
                Vec3 v = disp.scale(1.0 / duration);

                dashVelocity = v;
                dashTicksLeft = (int) duration;
                player.setDeltaMovement(v);
                player.hasImpulse = true;
                data.setChargedJumpTicks(0);
                cooldownLeft = cfg().chargedJumpCooldownTicks.get(); // 3s de cooldown
            } else {
                // Pulo normal um pouco mais alto.
                Vec3 dm = player.getDeltaMovement();
                player.setDeltaMovement(dm.x, dm.y + cfg().baseJumpBonus.get(), dm.z);
                player.hasImpulse = true;
            }
        });
    }

    /** Procura o mob mais proximo (vivo, != player) na trajetoria do salto. */
    private static Entity findMobInPath(Player player) {
        Vec3 motion = player.getDeltaMovement();
        AABB box = player.getBoundingBox().expandTowards(motion).inflate(0.3);
        List<Entity> hits = player.level().getEntities(player, box,
                e -> e instanceof LivingEntity && e != player && e.isAlive());
        Entity nearest = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : hits) {
            double d = player.distanceToSqr(e);
            if (d < bestDist) { bestDist = d; nearest = e; }
        }
        return nearest;
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        PitouCapability.with(player, data -> {
            if (data.isTransformed()) {
                event.setNewSpeed((float) (event.getNewSpeed() * cfg().breakSpeedMultiplier.get()));
            }
        });
    }
}
