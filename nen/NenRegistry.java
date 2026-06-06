package com.megu.neferpitou.nen;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registra o cerebro de Nen: um bloco que cai dos mobs com Nen, pode ser colocado no
 * chao e, ao ser clicado com botao direito segurando um graveto, da +1 de progressao.
 *
 * O modelo/textura ficam pra depois (o davig vai mandar). Por enquanto usa um cubo
 * placeholder; sem textura ele aparece rosa/preto, mas funciona normalmente.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class NenRegistry {

    private NenRegistry() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Neferpitou.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Neferpitou.MODID);

    public static final RegistryObject<Block> NEN_BRAIN = BLOCKS.register("nen_brain",
            () -> new BrainBlock(BlockBehaviour.Properties.of()
                    .strength(0.4F)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()));

    public static final RegistryObject<Item> NEN_BRAIN_ITEM = ITEMS.register("nen_brain",
            () -> new BlockItem(NEN_BRAIN.get(), new Item.Properties()));

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }

    @SubscribeEvent
    public static void addToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(NEN_BRAIN_ITEM.get());
        }
    }
}
