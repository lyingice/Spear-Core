package net.minecraft.spearcore.event;

import net.minecraft.spearcore.config.SpearConfig;
import net.minecraft.spearcore.entity.ai.goal.SpearUseGoal;
import net.minecraft.spearcore.init.SpearCoreItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.*;
import java.util.function.Supplier;

@EventBusSubscriber(modid = "spearcore")
public class SpearMobHandler {

    private static final Map<EntityType<?>, Float> CHANCES = new HashMap<>();
    private static final Map<EntityType<?>, Supplier<ItemStack>> FIXED_SPEARS = new HashMap<>();

    static {
        CHANCES.put(EntityType.ZOMBIE, 0.08f);
        CHANCES.put(EntityType.HUSK, 0.10f);
        CHANCES.put(EntityType.DROWNED, 0.06f);
        CHANCES.put(EntityType.ZOMBIE_VILLAGER, 0.08f);
        CHANCES.put(EntityType.SKELETON, 0.05f);
        CHANCES.put(EntityType.STRAY, 0.05f);
        CHANCES.put(EntityType.WITHER_SKELETON, 0.12f);
        CHANCES.put(EntityType.VINDICATOR, 0.15f);
        CHANCES.put(EntityType.PILLAGER, 0.08f);
        CHANCES.put(EntityType.CREEPER, 0.03f);

        FIXED_SPEARS.put(EntityType.PIGLIN_BRUTE, () -> new ItemStack(SpearCoreItems.GOLDEN_SPEAR.get()));
        FIXED_SPEARS.put(EntityType.PIGLIN, () -> new ItemStack(SpearCoreItems.GOLDEN_SPEAR.get()));
        FIXED_SPEARS.put(EntityType.ZOMBIFIED_PIGLIN, () -> new ItemStack(SpearCoreItems.GOLDEN_SPEAR.get()));
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Monster monster)) return;

        List<Item> enabledSpears = getEnabledSpears();
        if (enabledSpears.isEmpty()) return;

        Float chance = CHANCES.get(monster.getType());
        if (chance == null) return;
        if (monster.getRandom().nextFloat() >= chance) return;
        if (monster.getMainHandItem().getItem() instanceof net.minecraft.spearcore.item.SpearItem) return;

        // 分配矛
        Supplier<ItemStack> fixed = FIXED_SPEARS.get(monster.getType());
        ItemStack spearStack = fixed != null ? fixed.get() : new ItemStack(enabledSpears.get(monster.getRandom().nextInt(enabledSpears.size())));
        monster.setItemSlot(EquipmentSlot.MAINHAND, spearStack);
        monster.setDropChance(EquipmentSlot.MAINHAND, 0.15F);

        // 注册单 Goal 长矛 AI（仿 JerotesSpearUseGoal 机制）
        // 不再需要移除 MeleeAttackGoal——SpearUseGoal.canUse() 不检查距离，
        // 在 tick() 内部通过 engageTime/fleeingTime 状态机驱动战斗循环，
        // 单个 Goal 优先级足够高即可覆盖近战行为。
        monster.goalSelector.addGoal(1, new SpearUseGoal<>(monster, 1.0, 1.0, 4.5f));
    }

    private static List<Item> getEnabledSpears() {
        List<Item> list = new ArrayList<>();
        if (!SpearConfig.ENABLE_VANILLA_SPEARS.get()) return list;
        list.add(SpearCoreItems.WOODEN_SPEAR.get());
        list.add(SpearCoreItems.STONE_SPEAR.get());
        list.add(SpearCoreItems.IRON_SPEAR.get());
        list.add(SpearCoreItems.GOLDEN_SPEAR.get());
        list.add(SpearCoreItems.DIAMOND_SPEAR.get());
        list.add(SpearCoreItems.NETHERITE_SPEAR.get());
        if (SpearConfig.ENABLE_COPPER_SPEAR.get()) list.add(SpearCoreItems.COPPER_SPEAR.get());
        return list;
    }
}