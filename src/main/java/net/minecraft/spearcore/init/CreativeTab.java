package net.minecraft.spearcore.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.config.SpearConfig;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.MutableHashedLinkedMap;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * 把七把矛挂到原版"战斗"页，位置与 1.21.1 分支一致：紧跟在**下界合金剑**后面。
 *
 * <p><b>bus 必须写 MOD：</b>这个事件 implements IModBusEvent，是在模组总线上触发的；
 * 不写 bus 时 @Mod.EventBusSubscriber 默认是 Bus.FORGE（游戏总线），处理器永远不会被调用，
 * 表现就是"开关打开了，创造栏里还是没有矛"（已用日志实证：改对总线后战斗页里出现 7 把矛）。</p>
 *
 * <p>1.20.1 的事件本身没有 insertAfter，但它给的 {@code getEntries()} 是
 * {@link MutableHashedLinkedMap}，自带 {@code putAfter}/{@code putBefore}，所以位置照样能控。</p>
 */
@Mod.EventBusSubscriber(modid = SpearcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CreativeTab {
    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> combatTab = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                new ResourceLocation("minecraft", "combat"));
        if (event.getTabKey() != combatTab) return;

        // 材质档次顺序：木 → 石 → 铜 → 铁 → 金 → 钻石 → 下界合金
        // （铜夹在石与铁之间，和本模组的数值表一致）
        boolean vanillaSpears = SpearConfig.ENABLE_VANILLA_SPEARS.get();
        boolean copperSpear = SpearConfig.ENABLE_COPPER_SPEAR.get();
        if (!vanillaSpears && !copperSpear) return;

        List<Item> tierOrder = new ArrayList<>();
        if (vanillaSpears) {
            tierOrder.add(SpearCoreItems.WOODEN_SPEAR.get());
            tierOrder.add(SpearCoreItems.STONE_SPEAR.get());
        }
        if (copperSpear) {
            tierOrder.add(SpearCoreItems.COPPER_SPEAR.get());
        }
        if (vanillaSpears) {
            tierOrder.add(SpearCoreItems.IRON_SPEAR.get());
            tierOrder.add(SpearCoreItems.GOLDEN_SPEAR.get());
            tierOrder.add(SpearCoreItems.DIAMOND_SPEAR.get());
            tierOrder.add(SpearCoreItems.NETHERITE_SPEAR.get());
        }

        MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> entries = event.getEntries();
        ItemStack anchor = new ItemStack(Items.NETHERITE_SWORD);
        if (entries.contains(anchor)) {
            // putAfter 是"紧跟锚点"，所以要从后往前插，最终顺序才等于 tierOrder
            for (int i = tierOrder.size() - 1; i >= 0; i--) {
                entries.putAfter(anchor, new ItemStack(tierOrder.get(i)),
                        CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        } else {
            // 兜底：万一锚点物品不在这个页里，就直接追加到页尾（顺序仍与 tierOrder 一致）
            for (Item item : tierOrder) {
                event.accept(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }
}
