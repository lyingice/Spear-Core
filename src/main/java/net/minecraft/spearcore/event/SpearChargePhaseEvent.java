package net.minecraft.spearcore.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * 长矛蓄力阶段变更时触发（只在切换瞬间发一次）。
 * 其他模组可用于：自定义动画、音效、粒子联动。
 */
public class SpearChargePhaseEvent extends Event {
    public enum Phase { DELAY, DISMOUNT, KNOCKBACK, DAMAGE }

    private final LivingEntity user;
    private final Phase newPhase;

    public SpearChargePhaseEvent(LivingEntity user, Phase newPhase) {
        this.user = user;
        this.newPhase = newPhase;
    }

    public LivingEntity getUser() { return user; }
    public Phase getNewPhase() { return newPhase; }
}
