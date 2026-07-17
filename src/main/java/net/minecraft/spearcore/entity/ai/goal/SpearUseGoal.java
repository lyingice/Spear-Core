package net.minecraft.spearcore.entity.ai.goal;

import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.spearcore.util.SpearCollision;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.EnumSet;
import java.util.List;

/**
 * 复刻 JerotesSpearUseGoal 的长矛生物 AI：
 * 1. 进入攻击半径前接近目标；
 * 2. 进入半径后举矛蓄力并继续向目标冲锋；
 * 3. 蓄力结束后停止使用物品并向远离目标的位置撤退；
 * 4. 撤退阶段可以穿插普通戳刺；
 * 5. 撤退完成/超时后结束本次 Goal，让选择器重新评估下一轮攻击。
 */
public class SpearUseGoal<T extends PathfinderMob> extends Goal {

    private static final int MAX_FLEEING_TIME = 100;

    private final T mob;
    private final double speedModifierWhenCharging;
    private final double speedModifierWhenRepositioning;
    private final double attackRadiusSqr;
    private final double targetInRangeSqr;
    private final boolean canNormalAttack;
    private final boolean canChargeAttack;

    private int engageTime = -1;
    private int fleeingTime = -1;
    private Vec3 awayPos;
    private boolean done;
    public int ticksUntilNextAttack;

    public SpearUseGoal(T mob, double chargeSpeed, double repositionSpeed, float attackRadius) {
        this(mob, chargeSpeed, repositionSpeed, attackRadius, 2.0F, true, true);
    }

    public SpearUseGoal(T mob, double chargeSpeed, double repositionSpeed, float attackRadius, float targetInRange) {
        this(mob, chargeSpeed, repositionSpeed, attackRadius, targetInRange, true, true);
    }

    public SpearUseGoal(T mob, double chargeSpeed, double repositionSpeed, float attackRadius, float targetInRange, boolean canNormalAttack) {
        this(mob, chargeSpeed, repositionSpeed, attackRadius, targetInRange, canNormalAttack, true);
    }

    public SpearUseGoal(T mob, double chargeSpeed, double repositionSpeed, float attackRadius, float targetInRange,
                        boolean canNormalAttack, boolean canChargeAttack) {
        this.mob = mob;
        this.speedModifierWhenCharging = chargeSpeed;
        this.speedModifierWhenRepositioning = repositionSpeed;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.targetInRangeSqr = targetInRange * targetInRange;
        this.canNormalAttack = canNormalAttack;
        this.canChargeAttack = canChargeAttack;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return ableToAttack() && (!mob.isUsingItem() || mob.getUseItem().getItem() instanceof ShieldItem);
    }

    private boolean ableToAttack() {
        return mob.getTarget() != null && mob.getMainHandItem().getItem() instanceof SpearItem;
    }

    @Override
    public boolean canContinueToUse() {
        return !done && ableToAttack();
    }

    @Override
    public void start() {
        super.start();
        mob.setAggressive(true);
        ticksUntilNextAttack = 0;
    }

    @Override
    public void stop() {
        super.stop();
        mob.getNavigation().stop();
        mob.setAggressive(false);
        engageTime = -1;
        fleeingTime = -1;
        awayPos = null;
        done = false;
        mob.stopUsingItem();
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        double distSq = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        double movementFactor = currentMovementFactor();
        int ridingBonus = mob.isPassenger() ? 2 : 0;

        mob.lookAt(target, 30.0F, 30.0F);
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (canNormalAttack && !canChargeAttack) {
            tickAttackCooldown();
            checkAndPerformAttack(target, mob.getMainHandItem().getItem() instanceof SpearItem spear ? spear : null);
        }

        if (engageTime < 0) {
            if (distSq > attackRadiusSqr) {
                mob.getNavigation().moveTo(target, movementFactor * speedModifierWhenRepositioning);
                return;
            }

            engageTime = getSpearUseDuration();
            if (mob.getUseItem().getItem() instanceof ShieldItem) {
                mob.stopUsingItem();
            }
            if (canChargeAttack) {
                mob.startUsingItem(InteractionHand.MAIN_HAND);
                playUseSound();
            }
        }

        if (engageTime > 0) {
            engageTime--;
            if (engageTime == 0) {
                mob.stopUsingItem();
                double dist = Math.sqrt(distSq);
                awayPos = getPosAway(mob,
                        Math.max(0.0D, 9.0D + ridingBonus - dist),
                        Math.max(1.0D, 11.0D + ridingBonus - dist),
                        7,
                        target.position());
                fleeingTime = 1;
            }
        }

        if (fleeingTime > 0) {
            fleeingTime++;
            if (canNormalAttack && canChargeAttack) {
                tickAttackCooldown();
                checkAndPerformAttack(target, mob.getMainHandItem().getItem() instanceof SpearItem spear ? spear : null);
            }
            if (fleeingTime > getMaxFleeingTime()) {
                done = true;
                return;
            }
        }

        if (awayPos != null) {
            mob.getNavigation().moveTo(awayPos.x, awayPos.y, awayPos.z, movementFactor * speedModifierWhenRepositioning);
            if (mob.getNavigation().isDone()) {
                if (fleeingTime > 0) {
                    done = true;
                    return;
                }
                awayPos = null;
            }
        } else {
            mob.getNavigation().moveTo(target, movementFactor * speedModifierWhenCharging);
            if (distSq < targetInRangeSqr || mob.getNavigation().isDone()) {
                double dist = Math.sqrt(distSq);
                awayPos = getPosAway(mob,
                        6.0D + ridingBonus - dist,
                        7.0D + ridingBonus - dist,
                        7,
                        target.position());
            }
        }
    }

    protected void checkAndPerformAttack(LivingEntity target, SpearItem spear) {
        if (spear == null || !canPerformAttack(target, spear)) return;

        resetAttackCooldown(spear);
        if (canNormalAttack && !canChargeAttack) {
            mob.lookAt(target, 120.0F, 120.0F);
            mob.getLookControl().setLookAt(target, 120.0F, 120.0F);
        }
        mob.stopUsingItem();
        spear.attack(mob, EquipmentSlot.MAINHAND);
    }

    protected boolean canPerformAttack(LivingEntity target, SpearItem spear) {
        List<EntityHitResult> hits = SpearCollision.getHitEntitiesAlong(
                mob,
                spear,
                spear.getHitboxMargin(),
                entity -> mob == entity ? false : entity == target
                        && entity.isAlive()
                        && entity != mob.getVehicle()
                        && !mob.isPassengerOfSameVehicle(entity)
        );
        return !hits.isEmpty()
                && isTimeToAttack()
                && mob.getSensing().hasLineOfSight(target)
                && !mob.isUsingItem();
    }

    protected void resetAttackCooldown(SpearItem spear) {
        int multiplier = canChargeAttack ? 2 : 1;
        ticksUntilNextAttack = adjustedTickDelay(Math.max(1, Math.round(spear.getSwingTimes() * 20.0F)) * multiplier);
    }

    protected boolean isTimeToAttack() {
        return ticksUntilNextAttack <= 0;
    }

    private void tickAttackCooldown() {
        ticksUntilNextAttack = Math.max(ticksUntilNextAttack - 1, 0);
    }

    @Override
    protected int adjustedTickDelay(int ticks) {
        return this.requiresUpdateEveryTick() ? ticks : reducedTickDelay(ticks);
    }

    private int getMaxFleeingTime() {
        return adjustedTickDelay(MAX_FLEEING_TIME);
    }

    private int getSpearUseDuration() {
        if (mob.getMainHandItem().getItem() instanceof SpearItem spear) {
            return adjustedTickDelay(Math.max(1, spear.getDamageEndTick()));
        }
        return adjustedTickDelay(200);
    }

    private void playUseSound() {
        if (mob.getMainHandItem().getItem() instanceof SpearItem spear) {
            mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), spear.getUseSound(), mob.getSoundSource(), 1.0F, 1.0F);
        }
    }

    private double currentMovementFactor() {
        return 1.0D;
    }

    // ========== getPosAway ==========

    public static Vec3 getPosAway(PathfinderMob mob, int horizontalRange, int verticalRange, Vec3 awayFrom) {
        return getPosAway(mob, 0.0D, horizontalRange, verticalRange, awayFrom);
    }

    public static Vec3 getPosAway(PathfinderMob mob, double minHorizontalRange, double maxHorizontalRange, int verticalRange, Vec3 awayFrom) {
        Vec3 directAway = mob.position().subtract(awayFrom);
        Vec3 pos = LandRandomPos.getPosAway(mob,
                Math.max(1, (int) Math.ceil(maxHorizontalRange)),
                Math.max(1, verticalRange),
                awayFrom);
        if (pos == null && directAway.lengthSqr() > 1.0E-4D) {
            pos = DefaultRandomPos.getPosTowards(mob,
                    Math.max(1, (int) Math.ceil(maxHorizontalRange)),
                    Math.max(1, verticalRange),
                    mob.position().add(directAway.normalize().scale(Math.max(minHorizontalRange, maxHorizontalRange))),
                    Math.PI / 2.0D);
        }
        if (pos == null && directAway.lengthSqr() > 1.0E-4D) {
            pos = mob.position().add(directAway.normalize().scale(Math.max(1.0D, maxHorizontalRange)));
        }
        return pos;
    }
}
