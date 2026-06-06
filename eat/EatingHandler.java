package com.megu.neferpitou.eat;

import com.megu.neferpitou.Neferpitou;
import com.megu.neferpitou.capability.PitouCapability;
import com.megu.neferpitou.capability.PitouData;
import com.megu.neferpitou.config.PitouConfig;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Identidade felina ligada a comida:
 *  - Peixe da comida EXTRA (mais ainda se for cru).
 *  - Villagers sao comestiveis: clique direito neles (transformada) para devorar.
 *
 * Tudo no servidor (a fome e autoritativa la).
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EatingHandler {

    // ---------------- Peixe da mais comida ----------------
    @SubscribeEvent
    public static void onFinishEating(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!isPitou(player)) return;

        ItemStack stack = event.getItem();
        if (!stack.is(ItemTags.FISHES)) return;

        boolean cooked = stack.is(Items.COOKED_COD) || stack.is(Items.COOKED_SALMON);
        int bonus = cooked
                ? PitouConfig.COMMON.fishFoodBonusCooked.get()
                : PitouConfig.COMMON.fishFoodBonusRaw.get();
        float satMod = PitouConfig.COMMON.fishSaturationModifier.get().floatValue();

        // Comida EXTRA por cima da que o item ja deu.
        player.getFoodData().eat(bonus, satMod);
    }

    // ---------------- Comer villagers no clique direito ----------------
    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return; // evita disparar 2x (off-hand)
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!isPitou(player)) return;

        if (event.getTarget() instanceof AbstractVillager villager) {
            // Impede abrir a tela de trade.
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);

            int food = PitouConfig.COMMON.villagerEatFood.get();
            float satMod = PitouConfig.COMMON.villagerSaturationModifier.get().floatValue();
            player.getFoodData().eat(food, satMod);

            // Som de arroto (a "refeicao").
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.8F, 1.0F);

            // Mata o villager como abate do player (drops/xp normais).
            villager.hurt(player.damageSources().playerAttack(player), Float.MAX_VALUE);
        }
    }

    private static boolean isPitou(Player p) {
        return PitouCapability.get(p).resolve().map(PitouData::isTransformed).orElse(false);
    }
}
