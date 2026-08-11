package net.minecraft.spearcore.compat.punchy;

import net.neoforged.fml.ModList;

public final class PunchyCompat {

    private static boolean checked;
    private static boolean active;

    private PunchyCompat() {
    }

    public static boolean isActive() {
        if (!checked) {
            checked = true;
            active = ModList.get().isLoaded("punchy") && isPunchyEnabled();
        }
        return active;
    }

    private static boolean isPunchyEnabled() {
        try {
            Class<?> config = Class.forName("punchy.config.PunchyConfig");
            return Boolean.TRUE.equals(config.getMethod("isModEnabled").invoke(null));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }
}
