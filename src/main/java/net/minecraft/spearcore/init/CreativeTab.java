package net.minecraft.spearcore.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.spearcore.config.SpearConfig;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = "spearcore", value = Dist.CLIENT)
public class CreativeTab {
    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> CombatTab = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath("minecraft", "combat"));
        if (event.getTabKey() != CombatTab) return;

        boolean vanillaSpears = SpearConfig.ENABLE_VANILLA_SPEARS.get();
        boolean copperSpear = SpearConfig.ENABLE_COPPER_SPEAR.get();
        if (!vanillaSpears && !copperSpear) return;

        // 材质档次顺序：木 → 石 → 铜 → 铁 → 金 → 钻石 → 下界合金
        // insertAfter 是"紧跟锚点"，所以必须逆序调用，最终顺序才等于上面的档次顺序。
        // 铜夹在石与铁之间（与 1.20.1 分支、以及本模组的数值表一致）。
        ItemStack anchor = new ItemStack(Items.NETHERITE_SWORD);
        CreativeModeTab.TabVisibility visible = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        if (vanillaSpears) event.insertAfter(anchor, SpearCoreItems.NETHERITE_SPEAR.get().getDefaultInstance(), visible);
        if (vanillaSpears) event.insertAfter(anchor, SpearCoreItems.DIAMOND_SPEAR.get().getDefaultInstance(), visible);
        if (vanillaSpears) event.insertAfter(anchor, SpearCoreItems.GOLDEN_SPEAR.get().getDefaultInstance(), visible);
        if (vanillaSpears) event.insertAfter(anchor, SpearCoreItems.IRON_SPEAR.get().getDefaultInstance(), visible);
        if (copperSpear) event.insertAfter(anchor, SpearCoreItems.COPPER_SPEAR.get().getDefaultInstance(), visible);
        if (vanillaSpears) event.insertAfter(anchor, SpearCoreItems.STONE_SPEAR.get().getDefaultInstance(), visible);
        if (vanillaSpears) event.insertAfter(anchor, SpearCoreItems.WOODEN_SPEAR.get().getDefaultInstance(), visible);
    }
}
