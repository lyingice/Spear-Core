package net.minecraft.spearcore.entity.ai.goal;

import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.spearcore.util.SpearCollision;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SpearRetreatGoal<T extends Monster> extends Goal {

    private final T mob;
    private final double speed;
    private Vec3 retreatPos;
    private int ticksUntilAttack;
    private static final int MAX_RETREAT_TIME = 100;
    private int retreatTime;

    public SpearRetreatGoal(T mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 蓄力刚结束，开始脱离
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (!(mob.getMainHandItem().getItem() instanceof SpearItem)) return false;
        if (mob.isUsingItem()) return false;

        // 计算撤退位置
        double dist = mob.distanceTo(target);
        retreatPos = LandRandomPos.getPosAway(mob,
                Math.max(1, (int)(9 - dist)),
                Math.max(2, (int)(11 - dist)),
                target.position());
        return retreatPos != null;
    }

    @Override
    public void start() {
        retreatTime = 0;
        ticksUntilAttack = 0;
    }

    @Override
    public void tick() {
        retreatTime++;
        ticksUntilAttack--;

        LivingEntity target = mob.getTarget();
        if (target == null) return;
        mob.lookAt(target, 30, 30);

        // 向撤退点移动
        if (retreatPos != null) {
            mob.getNavigation().moveTo(retreatPos.x, retreatPos.y, retreatPos.z, speed);
        }

        // 左键戳刺
        if (ticksUntilAttack <= 0 && mob.getSensing().hasLineOfSight(target)) {
            if (mob.getMainHandItem().getItem() instanceof SpearItem spear) {
                spear.attack(mob, EquipmentSlot.MAINHAND);
                ticksUntilAttack = (int)(spear.getSwingTimes() * 20);
            } else {
                mob.swing(InteractionHand.MAIN_HAND);
                mob.doHurtTarget(target);
                ticksUntilAttack = 20;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return retreatTime < MAX_RETREAT_TIME
                && mob.getTarget() != null && mob.getTarget().isAlive()
                && !mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        mob.setAggressive(false);
    }
}