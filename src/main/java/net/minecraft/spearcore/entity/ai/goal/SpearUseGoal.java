package net.minecraft.spearcore.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SpearUseGoal<T extends Monster> extends Goal {

    static final int MIN_REPOSITION_DISTANCE = 6;
    static final int MAX_REPOSITION_DISTANCE = 7;
    static final int MIN_COOLDOWN_DISTANCE = 9;
    static final int MAX_COOLDOWN_DISTANCE = 11;
    private static final double MAX_FLEEING_TIME = reducedTickDelay(100);

    private final T mob;
    private final double speedModifierWhenCharging;
    private final double speedModifierWhenRepositioning;
    private final float approachDistanceSq;
    private final float targetInRangeRadiusSq;

    @Nullable
    private SpearUseState state;

    public SpearUseGoal(
            final T mob,
            final double speedModifierWhenCharging,
            final double speedModifierWhenRepositioning,
            final float approachDistance,
            final float targetInRangeRadius
    ) {
        this.mob = mob;
        this.speedModifierWhenCharging = speedModifierWhenCharging;
        this.speedModifierWhenRepositioning = speedModifierWhenRepositioning;
        this.approachDistanceSq = approachDistance * approachDistance;
        this.targetInRangeRadiusSq = targetInRangeRadius * targetInRangeRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return ableToAttack() && !this.mob.isUsingItem();
    }

    private boolean ableToAttack() {
        LivingEntity target = this.mob.getTarget();
        return target != null
                && target.isAlive()
                && this.mob.getMainHandItem().getItem() instanceof SpearItem;
    }

    private int getUseDuration() {
        SpearItem spear = (SpearItem) this.mob.getMainHandItem().getItem();
        return reducedTickDelay(spear.getDamageEndTick());
    }

    @Override
    public boolean canContinueToUse() {
        return this.state != null && !this.state.done && ableToAttack();
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
        this.state = new SpearUseState();
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.getNavigation().stop();
        this.mob.setAggressive(false);
        this.state = null;
        this.mob.stopUsingItem();
    }

    @Override
    public void tick() {
        if (this.state == null) return;

        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        double targetDistSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        Entity mount = this.mob.getRootVehicle();
        float speedModifier = 1.0F;
        if (this.mob.isPassenger() && this.mob.getVehicle() instanceof LivingEntity vehicle) {
            var attr = vehicle.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            if (attr != null) {
                speedModifier = (float) attr.getValue() * 10.0F; // MOVEMENT_SPEED ≈ 0.1~0.3, 放大到 1~3
            }
        }

        int mountDistance = this.mob.isPassenger() ? 2 : 0;
        this.mob.lookAt(target, 30.0F, 30.0F);
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // ---- Phase 1: Not yet engaged - approach or start charging ----
        if (this.state.notEngagedYet()) {
            if (targetDistSqr > (double) this.approachDistanceSq) {
                // Too far: approach the target
                this.mob.getNavigation().moveTo(
                        target,
                        (double) speedModifier * this.speedModifierWhenRepositioning
                );
                return;
            }

            // Close enough: start charging (using spear)
            this.state.startEngagement(this.getUseDuration());
            this.mob.startUsingItem(InteractionHand.MAIN_HAND);
        }

        // ---- Phase 2: Engagement (charging) ----
        if (this.state.tickAndCheckEngagement()) {
            // Charge finished
            this.mob.stopUsingItem();
            double distance = Math.sqrt(targetDistSqr);
            this.state.awayPos = LandRandomPos.getPosAway(
                    this.mob,
                    Math.max(1, (int)(MIN_COOLDOWN_DISTANCE + mountDistance - distance)),
                    Math.max(2, (int)(MAX_COOLDOWN_DISTANCE + mountDistance - distance)),
                    target.position()
            );
            this.state.fleeingTime = 1;
        }

        // ---- Phase 3: Fleeing / Repositioning ----
        if (!this.state.tickAndCheckFleeing()) {
            if (this.state.awayPos != null) {
                // Moving to reposition / retreat position
                this.mob.getNavigation().moveTo(
                        this.state.awayPos.x,
                        this.state.awayPos.y,
                        this.state.awayPos.z,
                        (double) speedModifier * this.speedModifierWhenRepositioning
                );
                if (this.mob.getNavigation().isDone()) {
                    if (this.state.fleeingTime > 0) {
                        // Full cycle done
                        this.state.done = true;
                    } else {
                        this.state.awayPos = null;
                    }
                }
            } else {
                // Charging toward target
                this.mob.getNavigation().moveTo(
                        target,
                        (double) speedModifier * this.speedModifierWhenCharging
                );
                if (targetDistSqr < (double) this.targetInRangeRadiusSq || this.mob.getNavigation().isDone()) {
                    double distance = Math.sqrt(targetDistSqr);
                    this.state.awayPos = LandRandomPos.getPosAway(
                            this.mob,
                            Math.max(1, (int)(MIN_REPOSITION_DISTANCE + mountDistance - distance)),
                            Math.max(2, (int)(MAX_REPOSITION_DISTANCE + mountDistance - distance)),
                            target.position()
                    );
                }
            }
        }
    }

    // ======================== Inner State ========================

    static class SpearUseState {

        private static final int NOT_ENGAGED = 0;
        private static final int ENGAGED = 1;
        private static final int FLEEING = 2;

        private int stage = NOT_ENGAGED;
        private int engagementCounter = 0;
        boolean done = false;
        int fleeingTime = 0;
        @Nullable
        Vec3 awayPos;

        boolean notEngagedYet() {
            return this.stage == NOT_ENGAGED;
        }

        void startEngagement(final int ticks) {
            this.stage = ENGAGED;
            this.engagementCounter = ticks;
        }

        /**
         * @return true if engagement just finished (transition to FLEEING)
         */
        boolean tickAndCheckEngagement() {
            if (this.stage == ENGAGED) {
                this.engagementCounter--;
                if (this.engagementCounter <= 0) {
                    this.stage = FLEEING;
                    return true;
                }
            }
            return false;
        }

        /**
         * @return true if currently in FLEEING stage
         */
        boolean tickAndCheckFleeing() {
            if (this.stage == FLEEING) {
                this.fleeingTime++;
                return true;
            }
            return false;
        }
    }
}
