package net.minecraft.spearcore.entity.ai.goal;

import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import java.util.EnumSet;

public class SpearApproachGoal<T extends Monster> extends Goal {

    private final T mob;
    private final double speed;
    private final float rangeSq;

    public SpearApproachGoal(T mob, double speed, float range) {
        this.mob = mob;
        this.speed = speed;
        this.rangeSq = range * range;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive()
                && mob.getMainHandItem().getItem() instanceof SpearItem
                && mob.distanceToSqr(target) > rangeSq;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;
        mob.lookAt(target, 30, 30);
        mob.getNavigation().moveTo(target, speed);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive()
                && mob.getMainHandItem().getItem() instanceof SpearItem
                && mob.distanceToSqr(target) > rangeSq;
    }
}