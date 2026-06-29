package net.minecraft.spearcore.entity.ai.goal;

import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import java.util.EnumSet;

public class SpearChargeGoal<T extends Monster> extends Goal {

    private final T mob;
    private final double speed;
    private int chargeTicks;
    private double maxChargeRangeSq;

    public SpearChargeGoal(T mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (!(mob.getMainHandItem().getItem() instanceof SpearItem spear)) return false;
        if (mob.isUsingItem()) return false;

        // 必须在最大攻击范围内
        maxChargeRangeSq = spear.getMaxRange() * spear.getMaxRange();
        double distSq = mob.distanceToSqr(target);
        if (distSq > maxChargeRangeSq) return false;

        // 需要视野
        return mob.getSensing().hasLineOfSight(target);
    }

    @Override
    public void start() {
        mob.setAggressive(true);
        mob.startUsingItem(InteractionHand.MAIN_HAND);
        SpearItem spear = (SpearItem) mob.getMainHandItem().getItem();
        // 蓄力总时长 = 到伤害阶段结束为止（onUseTick 会自动处理每 tick 的碰撞检测）
        chargeTicks = spear.getDamageEndTick();
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;
        mob.lookAt(target, 30, 30);
        mob.getNavigation().moveTo(target, speed);
        chargeTicks--;

        // 如果目标超出最大范围，提前结束蓄力
        // （onUseTick 会在 damageEndTick 时自动 stopUsingItem）
    }

    @Override
    public boolean canContinueToUse() {
        if (chargeTicks <= 0) return false;
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;

        // 目标跑出太远则放弃蓄力
        return mob.distanceToSqr(target) <= maxChargeRangeSq * 2.0;
    }

    @Override
    public void stop() {
        mob.stopUsingItem();
        mob.setAggressive(false);
    }
}