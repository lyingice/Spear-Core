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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 为生物添加长矛使用AI，并在生成时有概率持有长矛。
 * 所有长矛的启用/禁用均受 {@link SpearConfig} 控制。
 *
 * <p><b>重要：</b> 不要在静态初始化块中调用 {@code DeferredHolder.get()}！<br>
 * 物品注册在 mod 构造之后才完成，static 块中调 .get() 会因未绑定值而抛 NPE。<br>
 * 因此所有物品引用均以 {@link Supplier} 形式延迟解析。</p>
 */
@EventBusSubscriber(modid = "spearcore")
public class SpearMobEventHandler {

    // ==================== 配置 ====================
    // 每个生物类型的基础概率 (0.0 ~ 1.0)
    private static final Map<EntityType<?>, Float> SPAWN_WITH_SPEAR_CHANCE = new HashMap<>();

    // 每个生物类型使用哪种矛 (null = 随机选择) —— 使用 Supplier 延迟解析
    private static final Map<EntityType<?>, Supplier<ItemStack>> CUSTOM_SPEAR = new HashMap<>();

    // AI 参数配置
    private static final Map<EntityType<?>, AiParams> CUSTOM_AI_PARAMS = new HashMap<>();

    // 默认AI参数
    private static final AiParams DEFAULT_AI_PARAMS = new AiParams(1.0, 1.0, 4.0F, 3.0F);

    static {
        // ===== 僵尸系列 =====
        register(EntityType.ZOMBIE, 0.08f);
        register(EntityType.HUSK, 0.10f);
        register(EntityType.DROWNED, 0.06f);
        register(EntityType.ZOMBIE_VILLAGER, 0.08f);

        // ===== 骷髅系列 =====
        register(EntityType.SKELETON, 0.05f);
        register(EntityType.STRAY, 0.05f);

        // ===== 猪灵系列（固定金矛） =====
        registerCustom(EntityType.PIGLIN_BRUTE, 0.30f,
                () -> new ItemStack(SpearCoreItems.GOLDEN_SPEAR.get()),
                new AiParams(1.2, 1.2, 5.0F, 3.5F));

        registerCustom(EntityType.PIGLIN, 0.15f,
                () -> new ItemStack(SpearCoreItems.GOLDEN_SPEAR.get()),
                new AiParams(1.0, 1.1, 5.0F, 3.0F));

        registerCustom(EntityType.ZOMBIFIED_PIGLIN, 0.10f,
                () -> new ItemStack(SpearCoreItems.GOLDEN_SPEAR.get()),
                new AiParams(1.0, 1.0, 4.0F, 3.0F));

        register(EntityType.WITHER_SKELETON, 0.12f,
                () -> new ItemStack(SpearCoreItems.STONE_SPEAR.get()));

        // ===== 灾厄村民 =====
        register(EntityType.VINDICATOR, 0.15f);
        register(EntityType.PILLAGER, 0.08f);

        // ===== 其他 =====
        register(EntityType.CREEPER, 0.03f);
    }

    /** 仅注册概率，随机选矛 */
    private static void register(EntityType<?> type, float chance) {
        SPAWN_WITH_SPEAR_CHANCE.put(type, chance);
    }

    /** 注册概率 + 固定矛 */
    private static void register(EntityType<?> type, float chance, Supplier<ItemStack> fixedSpear) {
        SPAWN_WITH_SPEAR_CHANCE.put(type, chance);
        CUSTOM_SPEAR.put(type, fixedSpear);
    }

    /** 注册概率 + 固定矛 + 自定义AI参数 */
    private static void registerCustom(EntityType<?> type, float chance, Supplier<ItemStack> entry, AiParams aiParams) {
        SPAWN_WITH_SPEAR_CHANCE.put(type, chance);
        CUSTOM_SPEAR.put(type, entry);
        CUSTOM_AI_PARAMS.put(type, aiParams);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Monster monster)) return;

        // ========== 检查至少有一种长矛已启用 ==========
        List<Item> enabledSpears = getEnabledSpears();
        if (enabledSpears.isEmpty()) return;

        EntityType<?> type = monster.getType();
        Float chance = SPAWN_WITH_SPEAR_CHANCE.get(type);
        if (chance == null) return;

        // 随机决定是否持矛
        if (monster.getRandom().nextFloat() >= chance) return;

        // 已有矛则跳过
        if (monster.getMainHandItem().getItem() instanceof net.minecraft.spearcore.item.SpearItem) return;

        // 设置手持矛
        ItemStack spearStack = createSpearFor(monster, enabledSpears);
        if (spearStack.isEmpty()) return;
        monster.setItemSlot(EquipmentSlot.MAINHAND, spearStack);
        monster.setDropChance(EquipmentSlot.MAINHAND, 0.15F); // 15% 掉落率

        // 添加 AI
        addSpearGoal(monster, type);
    }

    /**
     * 根据 Config 设置获取所有已启用的长矛列表。
     * 此方法仅在事件触发时调用，此时注册已完成，.get() 安全。
     */
    private static List<Item> getEnabledSpears() {
        List<Item> enabled = new ArrayList<>();

        if (!SpearConfig.ENABLE_VANILLA_SPEARS.get()) {
            if (SpearConfig.ENABLE_COPPER_SPEAR.get()) {
                enabled.add(SpearCoreItems.COPPER_SPEAR.get());
            }
            return enabled;
        }

        enabled.add(SpearCoreItems.WOODEN_SPEAR.get());
        enabled.add(SpearCoreItems.STONE_SPEAR.get());
        enabled.add(SpearCoreItems.IRON_SPEAR.get());
        enabled.add(SpearCoreItems.GOLDEN_SPEAR.get());
        enabled.add(SpearCoreItems.DIAMOND_SPEAR.get());
        enabled.add(SpearCoreItems.NETHERITE_SPEAR.get());

        if (SpearConfig.ENABLE_COPPER_SPEAR.get()) {
            enabled.add(SpearCoreItems.COPPER_SPEAR.get());
        }

        return enabled;
    }

    /**
     * 检查某个特定长矛是否受 Config 启用。
     */
    private static boolean isSpearEnabled(Item spear) {
        if (spear == SpearCoreItems.COPPER_SPEAR.get()) {
            return SpearConfig.ENABLE_COPPER_SPEAR.get();
        }
        return SpearConfig.ENABLE_VANILLA_SPEARS.get();
    }

    /**
     * 为怪物生成一把长矛，优先使用自定义配置，否则从已启用列表中随机选取。
     */
    private static ItemStack createSpearFor(Monster monster, List<Item> enabledSpears) {
        // 先检查该生物类型是否有自定义矛
        Supplier<ItemStack> customSupplier = CUSTOM_SPEAR.get(monster.getType());
        if (customSupplier != null) {
            ItemStack customStack = customSupplier.get();
            if (!customStack.isEmpty() && isSpearEnabled(customStack.getItem())) {
                return customStack;
            }
        }

        // 随机选择已启用的长矛
        Item chosen = enabledSpears.get(monster.getRandom().nextInt(enabledSpears.size()));
        return new ItemStack(chosen);
    }

    private static void addSpearGoal(Monster monster, EntityType<?> type) {
        AiParams params = CUSTOM_AI_PARAMS.getOrDefault(type, DEFAULT_AI_PARAMS);

        // 移除旧的矛目标（如果有）
        monster.goalSelector.getAvailableGoals().removeIf(
                wrapped -> wrapped.getGoal() instanceof SpearUseGoal
        );

        // 添加新目标：优先级 2，高于一般攻击
        monster.goalSelector.addGoal(2, new SpearUseGoal<>(
                monster,
                params.speedModifierWhenCharging,
                params.speedModifierWhenRepositioning,
                params.approachDistance,
                params.targetInRangeRadius
        ));
    }

    // ==================== 内部类型 ====================

    private record AiParams(
            double speedModifierWhenCharging,
            double speedModifierWhenRepositioning,
            float approachDistance,
            float targetInRangeRadius
    ) {}
}
