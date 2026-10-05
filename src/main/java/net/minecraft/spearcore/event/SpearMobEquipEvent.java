package net.minecraft.spearcore.event;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * 生物生成时发放长矛（MobSpearMixin 触发）。
 * 其他模组可修改矛/掉率/取消发放。
 *
 * <p>1.20.1 的 Forge 用 `@Cancelable` 注解标记可取消事件（NeoForge 是 ICancellableEvent 接口），
 * 注解之后 Event#isCanceled()/setCanceled() 才可用。</p>
 */
@Cancelable
public class SpearMobEquipEvent extends Event {

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
}
