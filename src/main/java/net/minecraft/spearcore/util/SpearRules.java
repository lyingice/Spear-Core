package net.minecraft.spearcore.util;

import net.minecraft.spearcore.config.SpearConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Map;

/**
 * 行为开关的解析中心。
 *
 * <p>每个细节一个独立枚举配置项，值只包含"行为确实不同"的版本：
 * <ul>
 *   <li>Java 与基岩版一致 → 只有 {@code JAVA} / {@code MODDED}；</li>
 *   <li>Java 与本模组一致 → 只有 {@code JAVA} / {@code BEDROCK}；</li>
 *   <li>基岩版与本模组一致、且都不同于 Java → 只有 {@code JAVA} / {@code BEDROCK}
 *       （{@code BEDROCK} 同时也等于本模组原先的行为）；</li>
 *   <li>三者全同 → 不提供配置项。</li>
 * </ul>
 *
 * <p>所有读取都带兜底：配置还没加载（物品构造、早期初始化）时一律按 {@code JAVA} 处理，
 * 不让 {@link ForgeConfigSpec.ConfigValue#get()} 的 IllegalStateException 冒出来。</p>
 */
public final class SpearRules {

    // ========== 各细节的可选值（只列行为不同的版本） ==========

    /** 戳刺伤害：JAVA = 原版数值；BEDROCK = 每级 +1 点。 */
    public enum JabDamage { JAVA, BEDROCK }

    /** 戳刺速度加成：JAVA/BEDROCK 无；MODDED = 本模组原本的 floor(速度 × 倍率)。 */
    public enum JabSpeedBonus { JAVA, MODDED }

    /** 戳刺冷却模型：JAVA = 攻击蓄力条；BEDROCK = 使用冷却，且全背包长矛共享。 */
    public enum JabCooldown { JAVA, BEDROCK }

    /** 蓄力判定箱：JAVA = 0.125；MODDED = 本模组原本的 0.25。 */
    public enum ChargeHitboxMargin { JAVA, MODDED }

    /** 蓄力重复命中：JAVA = 每个目标每次蓄力只结算一次；BEDROCK/MODDED = 同目标 10 tick 内只结算一次。 */
    public enum ChargeRepeatHit { JAVA, BEDROCK }

    /** 戳刺重复命中：JAVA/BEDROCK 无；MODDED = 同目标 10 tick 内跳过。 */
    public enum JabRepeatHit { JAVA, MODDED }

    /** 耐久表：JAVA 原版 / BEDROCK 每级 +1 / MODDED 本模组原表。 */
    public enum Durability { JAVA, BEDROCK, MODDED }

    /** 创造模式攻击距离：JAVA 6.5；BEDROCK 7.5。 */
    public enum CreativeMaxRange { JAVA, BEDROCK }

    /** 副手蓄力：JAVA 允许；BEDROCK 禁止。 */
    public enum OffhandCharging { JAVA, BEDROCK }

    /** 突进：JAVA 原版 0.458×等级、饥饿 <6 禁用；MODDED 0.65×等级、饥饿 ≤7 禁用。 */
    public enum Lunge { JAVA, MODDED }

    /** 命中自减速：JAVA/BEDROCK 有；MODDED 无。 */
    public enum HitSlowDown { JAVA, MODDED }

    /** 怪物携带表：JAVA 原版名单；MODDED 本模组概率表 + 随机材质。 */
    public enum MobSpearEquipTable { JAVA, MODDED }

    /** 怪物掉落率：JAVA 8.5%（抢夺每级 +1%）；MODDED 固定 15%。 */
    public enum MobSpearDropChance { JAVA, MODDED }

    /** 最小攻击距离：JAVA 原版 2 格起手；MODDED 0 格，贴脸也能戳/蓄力。 */
    public enum MinAttackRange { JAVA, MODDED }

    /** 蓄力伤害基线是否吃攻击力修饰符：JAVA 不吃（裸武器攻击力）；BEDROCK 吃（力量药水等）。 */
    public enum ChargeStrength { JAVA, BEDROCK }

    /** 戳刺命中后到能重新蓄力的间隔：JAVA 0；BEDROCK 该矛的使用冷却。 */
    public enum JabToChargeDelay { JAVA, BEDROCK }

    /** 移动靶延迟补偿：JAVA 原版需预判；MODDED 按玩家看到的位置判定。 */
    public enum MovingTargetAssist { JAVA, MODDED }

    // ========== 数值表 ==========

    /** Java 原版耐久：木 / 石 / 铜 / 铁 / 金 / 钻 / 下界合金。 */
    private static final Map<String, Integer> JE_DURABILITY = Map.of(
            "wood", 59,
            "stone", 131,
            "copper", 190,
            "iron", 250,
            "gold", 32,
            "diamond", 1561,
            "netherite", 2031
    );

    /** 本模组原本的耐久表（= 1.20.1 工具材质数值）。 */
    private static final Map<String, Integer> MODDED_DURABILITY = Map.of(
            "wood", 59,
            "stone", 131,
            "copper", 195,
            "iron", 250,
            "gold", 59,
            "diamond", 1561,
            "netherite", 2031
    );

    private SpearRules() {
    }

    // ========== 读取包装 ==========

    private static <T extends Enum<T>> T read(ForgeConfigSpec.EnumValue<T> value, T fallback) {
        try {
            return value.get();
        } catch (IllegalStateException e) {
            return fallback;
        }
    }

    // ========== 戳刺 ==========

    public static JabDamage jabDamage() {
        return read(SpearConfig.JAB_DAMAGE, JabDamage.JAVA);
    }

    /** 戳刺伤害要额外加多少点（基岩版每级 +1）。 */
    public static float jabDamageBonus() {
        return jabDamage() == JabDamage.BEDROCK ? 1.0F : 0.0F;
    }

    public static JabSpeedBonus jabSpeedBonus() {
        return read(SpearConfig.JAB_SPEED_BONUS, JabSpeedBonus.JAVA);
    }

    public static boolean hasJabSpeedBonus() {
        return jabSpeedBonus() == JabSpeedBonus.MODDED;
    }

    public static JabCooldown jabCooldown() {
        return read(SpearConfig.JAB_COOLDOWN, JabCooldown.JAVA);
    }

    /** 是否走基岩版的使用冷却（并且全背包长矛共享）。 */
    public static boolean usesUseCooldown() {
        return jabCooldown() == JabCooldown.BEDROCK;
    }

    public static JabRepeatHit jabRepeatHit() {
        return read(SpearConfig.JAB_REPEAT_HIT, JabRepeatHit.JAVA);
    }

    public static boolean hasJabRepeatCooldown() {
        return jabRepeatHit() == JabRepeatHit.MODDED;
    }

    // ========== 蓄力 ==========

    public static ChargeHitboxMargin chargeHitboxMargin() {
        return read(SpearConfig.CHARGE_HITBOX_MARGIN, ChargeHitboxMargin.JAVA);
    }

    public static float hitboxMargin(float moddedMargin) {
        return chargeHitboxMargin() == ChargeHitboxMargin.MODDED ? moddedMargin : 0.125F;
    }

    public static ChargeRepeatHit chargeRepeatHit() {
        return read(SpearConfig.CHARGE_REPEAT_HIT, ChargeRepeatHit.JAVA);
    }

    /** 蓄力时"同目标只结算一次"的窗口：JAVA 用整次蓄力，BEDROCK 用 10 tick 接触冷却。 */
    public static int chargeRepeatCooldownTicks(int contactCooldownTicks) {
        return chargeRepeatHit() == ChargeRepeatHit.JAVA ? Integer.MAX_VALUE : contactCooldownTicks;
    }

    // ========== 耐久 / 距离 ==========

    public static Durability durabilityMode() {
        return read(SpearConfig.DURABILITY, Durability.JAVA);
    }

    /**
     * 按开关解析某材质的耐久。
     *
     * @param materialName 材质名（wood / stone / copper / iron / gold / diamond / netherite）
     * @param moddedValue  本模组原表里的数值，未知材质时直接回落到它
     */
    public static int durability(String materialName, int moddedValue) {
        return switch (durabilityMode()) {
            case JAVA -> JE_DURABILITY.getOrDefault(materialName, moddedValue);
            case BEDROCK -> JE_DURABILITY.getOrDefault(materialName, moddedValue - 1) + 1;
            case MODDED -> MODDED_DURABILITY.getOrDefault(materialName, moddedValue);
        };
    }

    public static CreativeMaxRange creativeMaxRange() {
        return read(SpearConfig.CREATIVE_MAX_RANGE, CreativeMaxRange.JAVA);
    }

    public static float creativeMaxRange(float moddedRange) {
        return creativeMaxRange() == CreativeMaxRange.BEDROCK ? 7.5F : moddedRange;
    }

    // ========== 移动 / 副手 / 突进 / 减速 ==========

    public static OffhandCharging offhandCharging() {
        return read(SpearConfig.OFFHAND_CHARGING, OffhandCharging.JAVA);
    }

    public static boolean blocksOffhandCharging() {
        return offhandCharging() == OffhandCharging.BEDROCK;
    }

    public static Lunge lunge() {
        return read(SpearConfig.LUNGE, Lunge.JAVA);
    }

    /** 突进每级给的水平冲量。 */
    public static float lungeStrength() {
        return lunge() == Lunge.MODDED ? 0.65F : 0.458F;
    }

    /** 饥饿门槛：JAVA 原版 <6 就禁用；MODDED ≤7 禁用。 */
    public static boolean lungeBlockedByHunger(int foodLevel) {
        return lunge() == Lunge.MODDED ? foodLevel <= 7 : foodLevel < 6;
    }

    public static HitSlowDown hitSlowDown() {
        return read(SpearConfig.HIT_SLOW_DOWN, HitSlowDown.JAVA);
    }

    public static boolean slowsDownAttackerOnHit() {
        return hitSlowDown() == HitSlowDown.JAVA;
    }

    // ========== 怪物 ==========

    public static MobSpearEquipTable mobSpearEquipTable() {
        return read(SpearConfig.MOB_SPEAR_EQUIP_TABLE, MobSpearEquipTable.JAVA);
    }

    public static boolean usesVanillaMobTable() {
        return mobSpearEquipTable() == MobSpearEquipTable.JAVA;
    }

    public static MobSpearDropChance mobSpearDropChance() {
        return read(SpearConfig.MOB_SPEAR_DROP_CHANCE, MobSpearDropChance.JAVA);
    }

    public static boolean usesVanillaDropChance() {
        return mobSpearDropChance() == MobSpearDropChance.JAVA;
    }

    // ========== 距离 / 蓄力基线 / 戳刺后摇 / 移动靶 ==========

    public static MinAttackRange minAttackRange() {
        return read(SpearConfig.MIN_ATTACK_RANGE, MinAttackRange.JAVA);
    }

    /** 最小攻击距离是否归零（贴脸也能戳刺、也能起手蓄力）。 */
    public static boolean ignoresMinRange() {
        return minAttackRange() == MinAttackRange.MODDED;
    }

    public static ChargeStrength chargeStrength() {
        return read(SpearConfig.CHARGE_STRENGTH, ChargeStrength.JAVA);
    }

    /** 蓄力伤害的基线要不要算上力量/虚弱等攻击力修饰符。 */
    public static boolean chargeGainsAttributeModifiers() {
        return chargeStrength() == ChargeStrength.BEDROCK;
    }

    public static JabToChargeDelay jabToChargeDelay() {
        return read(SpearConfig.JAB_TO_CHARGE_DELAY, JabToChargeDelay.JAVA);
    }

    /** 戳刺命中后要挂多久的"禁止蓄力"冷却；JAVA 为 0（无间隔）。 */
    public static int jabToChargeDelayTicks(int useCooldownTicks) {
        return jabToChargeDelay() == JabToChargeDelay.BEDROCK ? useCooldownTicks : 0;
    }

    public static MovingTargetAssist movingTargetAssist() {
        return read(SpearConfig.MOVING_TARGET_ASSIST, MovingTargetAssist.JAVA);
    }

    /** 是否做移动靶延迟补偿（按玩家看到的位置判定）。 */
    public static boolean compensatesMovingTargets() {
        return movingTargetAssist() == MovingTargetAssist.MODDED;
    }
}
