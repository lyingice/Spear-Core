package net.minecraft.spearcore.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.spearcore.config.SpearConfig;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 1.20.1 的 BuildCreativeModeTabContentsEvent 没有 insertAfter，只能追加到该标签页末尾，
 * 所以这里用 accept(...) 把矛挂到原版"战斗"页。
 */
@Mod.EventBusSubscriber(modid = "spearcore", value = Dist.CLIENT)
public class CreativeTab {
    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> combatTab = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                new ResourceLocation("minecraft", "combat"));
        if (event.getTabKey() != combatTab) return;

        if (SpearConfig.ENABLE_VANILLA_SPEARS.get()) {
            event.accept(new ItemStack(SpearCoreItems.NETHERITE_SPEAR.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(new ItemStack(SpearCoreItems.DIAMOND_SPEAR.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(new ItemStack(SpearCoreItems.GOLDEN_SPEAR.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(new ItemStack(SpearCoreItems.IRON_SPEAR.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(new ItemStack(SpearCoreItems.STONE_SPEAR.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(new ItemStack(SpearCoreItems.WOODEN_SPEAR.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
        if (SpearConfig.ENABLE_COPPER_SPEAR.get()) {
            event.accept(new ItemStack(SpearCoreItems.COPPER_SPEAR.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
