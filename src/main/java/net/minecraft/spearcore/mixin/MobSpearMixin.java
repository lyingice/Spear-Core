package net.minecraft.spearcore.mixin;

import net.minecraft.spearcore.config.SpearConfig;
import net.minecraft.spearcore.entity.ai.goal.SpearUseGoal;
import net.minecraft.spearcore.init.SpearCoreItems;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
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

@Mixin(Mob.class)
public abstract class MobSpearMixin {

    @Unique
    private static final Map<EntityType<?>, Float> spearcore$CHANCES = new HashMap<>();
    @Unique
    private static final Map<EntityType<?>, Supplier<ItemStack>> spearcore$FIXED_SPEARS = new HashMap<>();

    static {
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
        if (monster.getMainHandItem().getItem() instanceof net.minecraft.spearcore.item.SpearItem) return;

        List<Item> enabledSpears = spearcore$getEnabledSpears();
        if (enabledSpears.isEmpty()) return;

        Float chance = spearcore$CHANCES.get(monster.getType());
        if (chance == null) return;
        if (monster.getRandom().nextFloat() >= chance) return;

        Supplier<ItemStack> fixed = spearcore$FIXED_SPEARS.get(monster.getType());
        ItemStack spearStack = fixed != null
                ? fixed.get()
                : new ItemStack(enabledSpears.get(monster.getRandom().nextInt(enabledSpears.size())));
        SpearMobEquipEvent equipEvent = new SpearMobEquipEvent(monster, spearStack, 0.15F);
        MinecraftForge.EVENT_BUS.post(equipEvent);
        if (equipEvent.isCanceled() || equipEvent.getSpearStack().isEmpty()) return;
        spearStack = equipEvent.getSpearStack();
        monster.setItemSlot(EquipmentSlot.MAINHAND, spearStack);
        monster.setDropChance(EquipmentSlot.MAINHAND, 0.15F);

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
