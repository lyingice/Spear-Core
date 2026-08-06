package net.minecraft.spearcore.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/**
 * 长矛命中目标后触发（伤害已结算）。
 * 其他模组可用于：吸血、点燃、粒子、计数、成就等。
 */
public class SpearHitEvent extends Event {
    private final LivingEntity attacker;
    private final Entity target;
    private final float damageDealt;
    private final SpearDamageEvent.AttackType attackType;

    public SpearHitEvent(LivingEntity attacker, Entity target, float damageDealt,
                         SpearDamageEvent.AttackType attackType) {
        this.attacker = attacker;
        this.target = target;
        this.damageDealt = damageDealt;
        this.attackType = attackType;
    }

    public LivingEntity getAttacker() { return attacker; }
    public Entity getTarget() { return target; }
    public float getDamageDealt() { return damageDealt; }
    public SpearDamageEvent.AttackType getAttackType() { return attackType; }
}