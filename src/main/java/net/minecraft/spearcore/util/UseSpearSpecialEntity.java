package net.minecraft.spearcore.util;

/**
 * J库 UseSpearSpecialEntity 接口，让实体在持矛时有特殊速度/移动判定逻辑。
 * 原包: com.jerotes.jerotes.entity.Interface.UseSpearSpecialEntity
 * 注意: 由于 Windows 文件系统不区分大小写，entity/Interface 与 entity/interface 冲突，
 * 因此放在 util 包中。
 */
public interface UseSpearSpecialEntity {

    default float getJerotesSpearNeedReach() {
        return 0.5F;
    }

    default float getJerotesSpearNeedSpeed() {
        return 0.2F;
    }

    default float getJerotesSpearDamageMultiple() {
        return 1.0F;
    }

    /**
     * 是否像玩家一样获取移动向量（通过 KnownMovementAccessor）
     */
    default boolean isJerotesSpearGetMotionLikePlayer() {
        return false;
    }

    default boolean isJerotesSpearCauseExtraKnockbackPlayer() {
        return false;
    }
}
