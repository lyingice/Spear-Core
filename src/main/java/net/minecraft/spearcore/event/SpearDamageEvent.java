package net.minecraft.spearcore.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * 在矛计算最终伤害前触发。
 *   - multiplier 初始为 1.0，监听者可修改
 *   - 最终伤害 = 基础计算伤害 × 对应属性值 × multiplier
 */
public class SpearDamageEvent extends Event {
    public enum AttackType { STAB, CHARGE }

    private final LivingEntity attacker;
    private final Entity target;
    private final float baseDamage;
    private final AttackType attackType;
    private float multiplier = 1.0F;

    public SpearDamageEvent(LivingEntity attacker, Entity target, float baseDamage, AttackType attackType) {
        this.attacker = attacker;
        this.target = target;
        this.baseDamage = baseDamage;
        this.attackType = attackType;
    }

    public LivingEntity getAttacker() { return attacker; }
    public Entity getTarget() { return target; }
    public float getBaseDamage() { return baseDamage; }
    public AttackType getAttackType() { return attackType; }
    public float getMultiplier() { return multiplier; }
    public void setMultiplier(float multiplier) { this.multiplier = multiplier; }
    public void addMultiplier(float extra) { this.multiplier *= extra; }
}
