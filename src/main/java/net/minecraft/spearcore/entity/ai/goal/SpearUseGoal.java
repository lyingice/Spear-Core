package net.minecraft.spearcore.entity.ai.goal;

import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.spearcore.util.SpearCollision;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 *
 * 状态机（由 phase 字段驱动）：
 *   APPROACH  → 距离 > attackRadius → 接近目标
 *   CHARGING  → 距离 ≤ attackRadius → 蓄力冲锋（engageTime 递减）
 *   STRIKE    → engageTime == 0     → 停止蓄力，进入撤退
 *   RETREAT   → fleeingTime > 0     → 边撤退边攻击
 *   COOLDOWN  → 撤退完成             → 强制冷却 RECHARGE_COOLDOWN tick，冷却结束后 phase=APPROACH
 *
 * 保留旧文件不变。
 */
public class SpearUseGoal<T extends Monster> extends Goal {

    private static final int MAX_FLEEING_TIME = 100;
    private static final int RECHARGE_COOLDOWN = 40; // 撤退后 2 秒冷却

    private enum Phase {
        APPROACH, CHARGING, RETREAT, COOLDOWN
    }

    private final T mob;
    private final double speedModifierWhenCharging;
    private final double speedModifierWhenRepositioning;
    private final double attackRadiusSqr;

    private Phase phase;
    private int engageTime;               // 蓄力剩余 tick
    private int fleeingTime;              // 撤退已用 tick
    private Vec3 awayPos;                  // 撤退目标位置
    private int ticksUntilNextAttack;      // 攻击冷却
    private int cooldownRemaining;         // 循环冷却

    public SpearUseGoal(T mob, double chargeSpeed, double repositionSpeed, float attackRadius) {
        this.mob = mob;
        this.speedModifierWhenCharging = chargeSpeed;
        this.speedModifierWhenRepositioning = repositionSpeed;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // ==================== 条件检查 ====================

    @Override
    public boolean canUse() {
        return mob.getTarget() != null
                && mob.getMainHandItem().getItem() instanceof SpearItem;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.getTarget() != null && mob.getTarget().isAlive()
                && mob.getMainHandItem().getItem() instanceof SpearItem;
    }

    // ==================== 生命周期 ====================

    @Override
    public void start() {
        mob.setAggressive(true);
        phase = Phase.APPROACH;
        engageTime = -1;
        fleeingTime = 0;
        awayPos = null;
        ticksUntilNextAttack = 0;
        cooldownRemaining = 0;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        mob.stopUsingItem();
        mob.setAggressive(false);
    }

    // ==================== 核心 tick ====================

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        double distSq = mob.distanceToSqr(target);
        mob.lookAt(target, 30, 30);
        mob.getLookControl().setLookAt(target, 30, 30);

        switch (phase) {
            case APPROACH -> tickApproach(target, distSq);
            case CHARGING -> tickCharging(target, distSq);
            case RETREAT -> tickRetreat(target, distSq);
            case COOLDOWN -> tickCooldown(target, distSq);
        }
    }

    // ==================== 阶段: 接近 ====================

    private void tickApproach(LivingEntity target, double distSq) {
        if (distSq > attackRadiusSqr) {
            // 太远 → 接近
            mob.getNavigation().moveTo(target, speedModifierWhenRepositioning);
        } else {
            // 进入半径 → 开始蓄力
            if (mob.isUsingItem()) return;
            SpearItem spear = (SpearItem) mob.getMainHandItem().getItem();
            engageTime = spear.getDamageEndTick();
            mob.startUsingItem(InteractionHand.MAIN_HAND);
            phase = Phase.CHARGING;
        }
    }

    // ==================== 阶段: 蓄力冲锋 ====================

    private void tickCharging(LivingEntity target, double distSq) {
        // 向目标冲锋
        mob.getNavigation().moveTo(target, speedModifierWhenCharging);
        engageTime--;

        if (engageTime <= 0) {
            // 蓄力完成
            mob.stopUsingItem();

            // 计算撤退位置（至少是攻击半径的 1.5 倍）
            double dist = mob.distanceTo(target);
            double minAwayDist = Math.min(14.0, Math.sqrt(attackRadiusSqr) * 1.5 + 2.0);
            awayPos = LandRandomPos.getPosAway(mob,
                    Math.max(2, (int) (minAwayDist - dist)),
                    Math.max(3, (int) (minAwayDist + 2 - dist)),
                    target.position());

            // 找不到撤退位置 → 朝反方向
            if (awayPos == null) {
                Vec3 awayDir = mob.position().subtract(target.position()).normalize().scale(minAwayDist);
                awayPos = mob.position().add(awayDir);
            }

            fleeingTime = 0;
            ticksUntilNextAttack = 0;
            phase = Phase.RETREAT;
        }
    }

    // ==================== 阶段: 撤退+攻击 ====================

    private void tickRetreat(LivingEntity target, double distSq) {
        fleeingTime++;

        // 攻击冷却
        if (ticksUntilNextAttack > 0) {
            ticksUntilNextAttack--;
        }

        // 边撤退边攻击
        if (ticksUntilNextAttack <= 0
                && mob.getSensing().hasLineOfSight(target)
                && !mob.isUsingItem()) {
            performSpearAttack(target);
        }

        // 向撤退点移动
        if (awayPos != null) {
            mob.getNavigation().moveTo(awayPos.x, awayPos.y, awayPos.z, speedModifierWhenRepositioning);
        }

        // 撤退完成条件：超时 或 到达撤退点 或 已经远离目标到足够远
        boolean timeUp = fleeingTime > MAX_FLEEING_TIME;
        boolean arrived = awayPos != null && mob.getNavigation().isDone();
        boolean farEnough = distSq > attackRadiusSqr * 1.5;

        if (timeUp || arrived || farEnough) {
            awayPos = null;
            cooldownRemaining = RECHARGE_COOLDOWN;
            phase = Phase.COOLDOWN;
        }
    }

    // ==================== 阶段: 冷却 ====================

    private void tickCooldown(LivingEntity target, double distSq) {
        cooldownRemaining--;

        // 冷却期间不攻击，但可以移动
        if (distSq <= attackRadiusSqr) {
            // 还在攻击半径内 → 远离目标
            if (awayPos == null) {
                Vec3 awayDir = mob.position().subtract(target.position()).normalize().scale(10.0);
                awayPos = mob.position().add(awayDir);
            }
            mob.getNavigation().moveTo(awayPos.x, awayPos.y, awayPos.z, speedModifierWhenRepositioning);

            if (mob.getNavigation().isDone()) {
                awayPos = null;
            }
        } else {
            // 已在攻击半径外 → 可以重新接近
            awayPos = null;
        }

        // 冷却结束 → 回到 APPROACH
        if (cooldownRemaining <= 0) {
            phase = Phase.APPROACH;
        }
    }

    // ==================== 攻击执行 ====================

    private void performSpearAttack(LivingEntity target) {
        SpearItem spear = (SpearItem) mob.getMainHandItem().getItem();

        // 精确碰撞检测
        List<EntityHitResult> hits = SpearCollision.getHitEntitiesAlong(
                mob, spear, spear.getHitboxMargin(),
                entity -> entity == target
                        && entity.isAlive()
                        && entity != mob
        );

        if (hits.isEmpty()) {
            // 放宽判定
            hits = SpearCollision.getHitEntitiesAlong(
                    mob, spear, spear.getHitboxMargin2(),
                    entity -> entity instanceof LivingEntity
                            && entity.isAlive()
                            && entity != mob
            );
        }

        if (hits.isEmpty()) return;

        spear.attack(mob, EquipmentSlot.MAINHAND);
        ticksUntilNextAttack = (int) (spear.getSwingTimes() * 20.0F);
        if (ticksUntilNextAttack <= 0) ticksUntilNextAttack = 20;
    }
}
