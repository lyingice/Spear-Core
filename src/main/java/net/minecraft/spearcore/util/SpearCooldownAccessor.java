package net.minecraft.spearcore.util;

import net.minecraft.world.entity.Entity;

public interface SpearCooldownAccessor {
    boolean WasRecentlyStabbed(Entity target, int cooldownTicks);
    void RememberStabbedEntity(Entity target);
}