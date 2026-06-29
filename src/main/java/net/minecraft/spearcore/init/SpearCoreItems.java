package net.minecraft.spearcore.init;

import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.item.BaseSpearItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class SpearCoreItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(SpearcoreMod.MODID);

    public static final DeferredItem<Item> WOODEN_SPEAR = REGISTRY.register("wooden_spear", BaseSpearItem.WoodenSpearItem::new);
    public static final DeferredItem<Item> STONE_SPEAR = REGISTRY.register("stone_spear", BaseSpearItem.StoneSpearItem::new);
    public static final DeferredItem<Item> IRON_SPEAR = REGISTRY.register("iron_spear", BaseSpearItem.IronSpearItem::new);
    public static final DeferredItem<Item> GOLDEN_SPEAR = REGISTRY.register("golden_spear", BaseSpearItem.GoldenSpearItem::new);
    public static final DeferredItem<Item> DIAMOND_SPEAR = REGISTRY.register("diamond_spear", BaseSpearItem.DiamondSpearItem::new);
    public static final DeferredItem<Item> NETHERITE_SPEAR = REGISTRY.register("netherite_spear", BaseSpearItem.NetheriteSpearItem::new);
    public static final DeferredItem<Item> COPPER_SPEAR = REGISTRY.register("copper_spear", BaseSpearItem.CopperSpearItem::new);
}
