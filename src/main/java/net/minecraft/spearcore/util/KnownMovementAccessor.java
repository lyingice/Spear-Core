package net.minecraft.spearcore.util;

import net.minecraft.world.phys.Vec3;

public interface KnownMovementAccessor {
    Vec3 GetKnownMovement();
    void SetKnownMovement(Vec3 vec3);
}