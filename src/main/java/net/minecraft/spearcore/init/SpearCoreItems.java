package net.minecraft.spearcore.init;

import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.config.SpearConfig;
import net.minecraft.spearcore.item.BaseSpearItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SpearCoreItems {
    public static DeferredRegister.Items REGISTRY = DeferredRegister.createItems(SpearcoreMod.MODID);
    public static DeferredItem<Item> WOODEN_SPEAR = null;
    public static DeferredItem<Item> STONE_SPEAR = null;
    public static DeferredItem<Item> IRON_SPEAR = null;
    public static DeferredItem<Item> GOLDEN_SPEAR = null;
    public static DeferredItem<Item> DIAMOND_SPEAR = null;
    public static DeferredItem<Item> NETHERITE_SPEAR = null;
    public static DeferredItem<Item> COPPER_SPEAR = null;
    static {
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
