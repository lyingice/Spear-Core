package net.minecraft.spearcore.mixin;

import net.minecraft.spearcore.config.SpearConfig;
import net.minecraft.spearcore.entity.ai.goal.SpearUseGoal;
import net.minecraft.spearcore.init.SpearCoreItems;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.spearcore.util.SpearRules;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.spearcore.event.SpearMobEquipEvent;
import net.minecraftforge.common.MinecraftForge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 让怪物出生时携带长矛。
 *
 * <p>名单与概率由 {@code behavior.mobSpearEquipTable} 决定：
 * JAVA = 原版名单（僵尸/尸壳/僵尸村民拿铁矛，猪灵/僵尸猪灵拿金矛，约 5%）；
 * MODDED = 本模组原本的概率表 + 随机材质。
 * 掉落率由 {@code behavior.mobSpearDropChance} 决定：JAVA 8.5%（抢夺每级 +1%），MODDED 固定 15%。</p>
 */
@Mixin(Mob.class)
public abstract class MobSpearMixin {

    /** 原版名单：材质固定，概率统一约 5%。 */
    @Unique
    private static final Map<EntityType<?>, Supplier<Item>> spearcore$VANILLA_SPEARS = new HashMap<>();

    /** 原版携带概率（wiki 未给出具体数值，取社区常用的 5% 近似值）。 */
    @Unique
    private static final float spearcore$VANILLA_CHANCE = 0.05F;

    @Unique
    private static final Map<EntityType<?>, Float> spearcore$CHANCES = new HashMap<>();
    @Unique
    private static final Map<EntityType<?>, Supplier<ItemStack>> spearcore$FIXED_SPEARS = new HashMap<>();

    static {
        spearcore$VANILLA_SPEARS.put(EntityType.ZOMBIE, () -> SpearCoreItems.IRON_SPEAR.get());
        spearcore$VANILLA_SPEARS.put(EntityType.HUSK, () -> SpearCoreItems.IRON_SPEAR.get());
        spearcore$VANILLA_SPEARS.put(EntityType.ZOMBIE_VILLAGER, () -> SpearCoreItems.IRON_SPEAR.get());
        spearcore$VANILLA_SPEARS.put(EntityType.PIGLIN, () -> SpearCoreItems.GOLDEN_SPEAR.get());
        spearcore$VANILLA_SPEARS.put(EntityType.ZOMBIFIED_PIGLIN, () -> SpearCoreItems.GOLDEN_SPEAR.get());

        spearcore$CHANCES.put(EntityType.ZOMBIE, 0.08f);
        spearcore$CHANCES.put(EntityType.HUSK, 0.10f);
        spearcore$CHANCES.put(EntityType.DROWNED, 0.06f);
        spearcore$CHANCES.put(EntityType.ZOMBIE_VILLAGER, 0.08f);
        spearcore$CHANCES.put(EntityType.SKELETON, 0.05f);
        spearcore$CHANCES.put(EntityType.STRAY, 0.05f);
        spearcore$CHANCES.put(EntityType.WITHER_SKELETON, 0.12f);
        spearcore$CHANCES.put(EntityType.VINDICATOR, 0.15f);
        spearcore$CHANCES.put(EntityType.PILLAGER, 0.08f);

        spearcore$FIXED_SPEARS.put(EntityType.PIGLIN_BRUTE, () -> new ItemStack(SpearCoreItems.GOLDEN_SPEAR.get()));
        spearcore$FIXED_SPEARS.put(EntityType.PIGLIN, () -> new ItemStack(SpearCoreItems.GOLDEN_SPEAR.get()));
        spearcore$FIXED_SPEARS.put(EntityType.ZOMBIFIED_PIGLIN, () -> new ItemStack(SpearCoreItems.GOLDEN_SPEAR.get()));
    }

    /**
     * finalizeSpawn 是原版发放装备的最佳时机：
     * - 装备不会被后续逻辑覆盖
     * - Goal 在此添加不会和 registerGoals() 冲突
     * - 只在服务器端执行
     */
    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void spearcore$equipAndAddGoal(ServerLevelAccessor level, DifficultyInstance difficulty,
                                           MobSpawnType spawnType, SpawnGroupData spawnData,
                                           net.minecraft.nbt.CompoundTag tag,
                                           CallbackInfoReturnable<SpawnGroupData> cir) {

        Mob self = (Mob) (Object) this;
        if (self.level().isClientSide) return;
        if (!(self instanceof Monster monster)) return;

        // 已经有矛的跳过
        if (monster.getMainHandItem().getItem() instanceof SpearItem) return;

        // 原版装备掉落率 8.5%，抢夺每级 +1%（原版 Mob.dropCustomDeathLoot 会自动加）；MODDED 固定 15%。
        float dropChance = SpearRules.usesVanillaDropChance() ? 0.085F : 0.15F;

        ItemStack spearStack;
        if (SpearRules.usesVanillaMobTable()) {
            Supplier<Item> vanilla = spearcore$VANILLA_SPEARS.get(monster.getType());
            if (vanilla == null) return;
            // 原版长矛被关掉时怪物也不带，避免出现"拿得到但创造栏里没有"的矛。
            if (!SpearConfig.ENABLE_VANILLA_SPEARS.get()) return;
            if (monster.getRandom().nextFloat() >= spearcore$VANILLA_CHANCE) return;
            spearStack = new ItemStack(vanilla.get());
        } else {
            List<Item> enabledSpears = spearcore$getEnabledSpears();
            if (enabledSpears.isEmpty()) return;

            Float chance = spearcore$CHANCES.get(monster.getType());
            if (chance == null) return;
            if (monster.getRandom().nextFloat() >= chance) return;

            Supplier<ItemStack> fixed = spearcore$FIXED_SPEARS.get(monster.getType());
            spearStack = fixed != null
                    ? fixed.get()
                    : new ItemStack(enabledSpears.get(monster.getRandom().nextInt(enabledSpears.size())));
        }

        SpearMobEquipEvent equipEvent = new SpearMobEquipEvent(monster, spearStack, dropChance);
        MinecraftForge.EVENT_BUS.post(equipEvent);
        if (equipEvent.isCanceled() || equipEvent.getSpearStack().isEmpty()) return;
        spearStack = equipEvent.getSpearStack();
        monster.setItemSlot(EquipmentSlot.MAINHAND, spearStack);
        monster.setDropChance(EquipmentSlot.MAINHAND, dropChance);

        monster.goalSelector.addGoal(1, new SpearUseGoal<>(monster, 1.0, 1.0, 4.5f));
    }

    @Unique
    private static List<Item> spearcore$getEnabledSpears() {
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
