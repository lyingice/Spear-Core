package net.minecraft.spearcore.event;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 生物生成时发放长矛（MobSpearMixin 触发）。
 * 其他模组可修改矛/掉率/取消发放。
 */
public class SpearMobEquipEvent extends Event implements ICancellableEvent {

    private final Mob mob;
    private ItemStack spearStack;
    private float dropChance;

    public SpearMobEquipEvent(Mob mob, ItemStack spearStack, float dropChance) {
        this.mob = mob;
        this.spearStack = spearStack;
        this.dropChance = dropChance;
    }

    public Mob getMob() { return mob; }
    public ItemStack getSpearStack() { return spearStack; }
    public void setSpearStack(ItemStack spearStack) { this.spearStack = spearStack; }
    public float getDropChance() { return dropChance; }
    public void setDropChance(float dropChance) { this.dropChance = dropChance; }

    // 注意：不再自己声明 isCanceled()/setCanceled()
    // 这两个方法由 ICancellableEvent 接口提供默认实现
}