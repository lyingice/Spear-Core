package net.minecraft.spearcore.init;

import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.config.SpearConfig;
import net.minecraft.spearcore.item.BaseSpearItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.fml.event.config.ModConfigEvent;


public class SpearCoreItems {
    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getModId().equals(SpearcoreMod.MODID)) {
            registerItems();
        }
    }
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(SpearcoreMod.MODID);

    public static DeferredItem<Item> WOODEN_SPEAR;
    public static DeferredItem<Item> STONE_SPEAR;
    public static DeferredItem<Item> IRON_SPEAR;
    public static DeferredItem<Item> GOLDEN_SPEAR;
    public static DeferredItem<Item> DIAMOND_SPEAR;
    public static DeferredItem<Item> NETHERITE_SPEAR;
    public static DeferredItem<Item> COPPER_SPEAR;

    public static void registerItems() {
        if (SpearConfig.ENABLE_VANILLA_SPEARS.get()) {
            WOODEN_SPEAR = REGISTRY.register("wooden_spear", BaseSpearItem.WoodenSpearItem::new);
            STONE_SPEAR = REGISTRY.register("stone_spear", BaseSpearItem.StoneSpearItem::new);
            IRON_SPEAR = REGISTRY.register("iron_spear", BaseSpearItem.IronSpearItem::new);
            GOLDEN_SPEAR = REGISTRY.register("golden_spear", BaseSpearItem.GoldenSpearItem::new);
            DIAMOND_SPEAR = REGISTRY.register("diamond_spear", BaseSpearItem.DiamondSpearItem::new);
            NETHERITE_SPEAR = REGISTRY.register("netherite_spear", BaseSpearItem.NetheriteSpearItem::new);
        }
        if (SpearConfig.ENABLE_COPPER_SPEAR.get()) {
            COPPER_SPEAR = REGISTRY.register("copper_spear", BaseSpearItem.CopperSpearItem::new);
        }
    }
}
