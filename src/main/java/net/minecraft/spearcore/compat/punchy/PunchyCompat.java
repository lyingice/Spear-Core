package net.minecraft.spearcore.compat.punchy;

import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.config.SpearConfig;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraftforge.fml.ModList;

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

    private static boolean stageBridgeLogged;

    /** 是否启用"让 Punchy 的阶段时间轴跟随本模组矛参数"的桥接；配置未就绪时默认开启。 */
    public static boolean isStageBridgeEnabled() {
        try {
            return SpearConfig.PUNCHY_STAGE_BRIDGE.get();
        } catch (RuntimeException e) {
            return true;
        }
    }

    /**
     * 把本模组的蓄力阶段映射到 Punchy 的三段姿势：
     * 举矛段(delay + dismount) → 1(Engaged)、击退段 → 2(Tired)、仅伤害段 → 3(Disengaged)。
     */
    public static int chargeStage(SpearItem spear, int usedTicks) {
        if (usedTicks < spear.getDismountEndTick()) return 1;
        if (usedTicks < spear.getKnockbackEndTick()) return 2;
        return 3;
    }

    /** 本模组停止用矛的 tick（与 SpearItem#getDamageEndTick 一致）。 */
    public static int chargeEndTick(SpearItem spear) {
        return Math.max(1, spear.getDamageEndTick());
    }

    /** 首次生效时打印一次，便于确认桥接是否真的接管。 */
    public static void logStageBridgeOnce() {
        if (stageBridgeLogged) return;
        stageBridgeLogged = true;
        SpearcoreMod.LOGGER.info("[spearcore] Punchy 矛阶段桥接已启用（按各材质 delay/dismount/knockback/damage 阶段切换）");
    }
}
