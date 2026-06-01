package net.minecraft.spearcore.util;


import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SpearCollision {

    public static List<EntityHitResult> getHitEntitiesAlong(
            LivingEntity attacker, SpearItem spear, float hitboxMargin, Predicate<Entity> predicate) {
        Vec3 eyePos = attacker.getEyePosition();
        if (attacker.isPassenger()) {
            eyePos = eyePos.add(0.0, -0.5, 0.0);
        }
        Vec3 look = getHeadLookAngle(attacker);

        boolean creative = attacker instanceof Player player && player.getAbilities().instabuild;
        double minRange = creative ? 0.0 : spear.getMinRange();
        double maxRange = creative ? spear.getMaxRange() : spear.getMaxRange();
        double speedBonus = Math.max(0.0, look.dot(SpearItem.getMotion(attacker)));
        speedBonus = Math.min(speedBonus, 4.0); // 最多加 4 格
        maxRange += speedBonus;

        Vec3 start = eyePos.add(look.scale(minRange));
        Vec3 end = eyePos.add(look.scale(maxRange));

        // 调试日志
        //System.out.println("[SpearCollision] start=" + start + " end=" + end);
        //System.out.println("[SpearCollision] minRange=" + minRange + " maxRange=" + maxRange + " speedBonus=" + speedBonus);

        return getHitEntitiesAlong(start, attacker, predicate, end, hitboxMargin);
    }

    private static Vec3 getHeadLookAngle(Entity entity) {
        return calculateViewVector(entity.getXRot(), entity.getYHeadRot());
    }

    private static Vec3 calculateViewVector(float xRot, float yRot) {
        float xRad = xRot * ((float) Math.PI / 180F);
        float yRad = -yRot * ((float) Math.PI / 180F);
        float cosY = Mth.cos(yRad);
        float sinY = Mth.sin(yRad);
        float cosX = Mth.cos(xRad);
        float sinX = Mth.sin(xRad);
        return new Vec3(sinY * cosX, -sinX, cosY * cosX);
    }

    public static List<EntityHitResult> getHitEntitiesAlong(

            Vec3 start, Entity attacker, Predicate<Entity> predicate, Vec3 end, float hitboxMargin) {
        Level level = attacker.level();

        BlockHitResult blockHit = level.clip(
                new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, attacker));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        AABB searchBox = new AABB(start.x, start.y, start.z, end.x, end.y, end.z).inflate(hitboxMargin);

        List<EntityHitResult> results = new ArrayList<>();
        for (Entity target : level.getEntities(attacker, searchBox, predicate)) {
            AABB targetBox = target.getBoundingBox().inflate(hitboxMargin);
            Optional<Vec3> hit = targetBox.clip(start, end);
            if (hit.isPresent()) {
                results.add(new EntityHitResult(target, hit.get()));
            } else if (targetBox.contains(start)) {
                results.add(new EntityHitResult(target, start));
            }
        }

        results.sort((a, b) -> Double.compare(
                a.getLocation().distanceToSqr(start),
                b.getLocation().distanceToSqr(start)));
        return results;
    }

    public static boolean hasLineOfSight(Entity from, Entity to) {
        if (to.level() != from.level()) return false;

        Vec3 fromEye = from.getEyePosition();
        Vec3 toCenter = to.position().add(0, to.getBbHeight() / 2, 0);

        BlockHitResult hit = from.level().clip(
                new ClipContext(fromEye, toCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, from)
        );

        return hit.getType() == HitResult.Type.MISS;
    }

}