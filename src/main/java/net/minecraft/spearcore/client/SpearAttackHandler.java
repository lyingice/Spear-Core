package net.minecraft.spearcore.client;

/**
 * @deprecated Server-side spear attack handling moved to
 * {@link net.minecraft.spearcore.event.SpearAttackHandler} to avoid dedicated-server
 * class loading of client-only animation code.
 */
@Deprecated(forRemoval = false)
public final class SpearAttackHandler {
    private SpearAttackHandler() {
    }
}
