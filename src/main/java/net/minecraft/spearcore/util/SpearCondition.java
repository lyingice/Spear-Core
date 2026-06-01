package net.minecraft.spearcore.util;

import java.util.Optional;

public record SpearCondition(int maxDurationTicks, float minSpeed, float minRelativeSpeed) {

    public boolean test(int usedTicks, double attackerSpeed, double relativeSpeed, double needSpeed) {
        return usedTicks <= this.maxDurationTicks
                && attackerSpeed >= (double) this.minSpeed * needSpeed
                && relativeSpeed >= (double) this.minRelativeSpeed * needSpeed;
    }

    public static Optional<SpearCondition> ofAttackerSpeed(int maxDurationTicks, float minSpeed) {
        return Optional.of(new SpearCondition(maxDurationTicks, minSpeed, 0.0F));
    }

    public static Optional<SpearCondition> ofRelativeSpeed(int maxDurationTicks, float minRelativeSpeed) {
        return Optional.of(new SpearCondition(maxDurationTicks, 0.0F, minRelativeSpeed));
    }
}