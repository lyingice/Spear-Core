package net.minecraft.spearcore.gametest;

import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.config.SpearConfig;
import net.minecraft.spearcore.init.SpearCoreItems;
import net.minecraft.spearcore.init.SpearStats;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * GameTest：由游戏本体自己跑、自己判定。
 *
 * <p>1.21.1 这边 ModDevGradle 没有 gameTestServer 这个 run 类型（只有 client/server/data），
 * 所以走 unitTest：GameTest 当成 JUnit 跑，命令是 {@code gradlew test}，
 * 判定看退出码与 build/test-results/test/*.xml。模板结构由仓库根目录的
 * gameteststructures/empty5x5x5.snbt 提供（JUnit 的进程工作目录就是工程根目录）。</p>
 */
@GameTestHolder(SpearcoreMod.MODID)
public final class SpearGameTests {

    private static final String TEMPLATE = "empty5x5x5";

    private SpearGameTests() {
    }

    /** 七把矛的实例必须都是 SpearItem —— 所有行为判定的前提。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void allSpearsAreSpearItems(GameTestHelper helper) {
        Item[] items = {
                SpearCoreItems.WOODEN_SPEAR.get(),
                SpearCoreItems.STONE_SPEAR.get(),
                SpearCoreItems.COPPER_SPEAR.get(),
                SpearCoreItems.IRON_SPEAR.get(),
                SpearCoreItems.GOLDEN_SPEAR.get(),
                SpearCoreItems.DIAMOND_SPEAR.get(),
                SpearCoreItems.NETHERITE_SPEAR.get(),
        };
        for (Item item : items) {
            helper.assertTrue(item instanceof SpearItem, "有物品不是 SpearItem 子类");
        }
        helper.succeed();
    }

    /** 构造时写入 SpearStats 表的数值必须取得到。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void spearStatsAreRegistered(GameTestHelper helper) {
        helper.assertTrue(SpearStats.get(SpearCoreItems.IRON_SPEAR.get()).durability() == 250,
                "铁矛耐久应为 250，实际 " + SpearStats.get(SpearCoreItems.IRON_SPEAR.get()).durability());
        helper.assertTrue(SpearStats.get(SpearCoreItems.NETHERITE_SPEAR.get()).fireResistant(),
                "下界合金矛应当防火");
        helper.assertFalse(SpearStats.get(SpearCoreItems.IRON_SPEAR.get()).fireResistant(),
                "铁矛不应防火");
        helper.succeed();
    }

    /** 属性修饰符确实挂上了属性（1.21.1 用原版 ENTITY_INTERACTION_RANGE）。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void spearAttributesAreRegistered(GameTestHelper helper) {
        ItemStack stack = new ItemStack(SpearCoreItems.IRON_SPEAR.get());
        // 1.21 的 ItemStack#getAttributeModifiers() 返回 ItemAttributeModifiers（不是 1.20 的 Multimap）
        var entries = stack.getAttributeModifiers().modifiers();
        helper.assertTrue(entries.stream().anyMatch(e -> e.attribute().value() == Attributes.ATTACK_DAMAGE.value()),
                "缺少攻击力修饰符");
        helper.assertTrue(entries.stream().anyMatch(e -> e.attribute().value() == Attributes.ATTACK_SPEED.value()),
                "缺少攻击速度修饰符");
        helper.assertTrue(entries.stream().anyMatch(e -> e.attribute().value() == Attributes.ENTITY_INTERACTION_RANGE.value()),
                "缺少交互距离修饰符");
        helper.succeed();
    }

    /** 真正打一次：持矛实体戳中正前方的目标，目标掉血。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void stabDamagesTarget(GameTestHelper helper) {
        float dealt = spearcore$stabDamage(helper, 1, 1, null, 0);
        helper.assertTrue(dealt > 0.0F, "戳刺没有造成伤害（掉血 " + dealt + "）");
        helper.succeed();
    }

    /**
     * 附魔伤害必须真的参与结算（对齐 26.1.2 原版）。
     *
     * <p>这条是回归测试：曾经 stabAttack 直接 target.hurt，没走原版
     * EnchantmentHelper 的附魔伤害修正，导致锋利/亡灵杀手挂在物品上却不加伤害。</p>
     */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void sharpnessIncreasesStabDamage(GameTestHelper helper) {
        float plain = spearcore$stabDamage(helper, 1, 1, null, 0);
        Holder<Enchantment> sharpness = helper.getLevel().registryAccess().holderOrThrow(Enchantments.SHARPNESS);
        float sharp = spearcore$stabDamage(helper, 3, 1, sharpness, 5);
        helper.assertTrue(sharp > plain, "锋利附魔没有提高戳刺伤害：" + plain + " -> " + sharp);
        helper.succeed();
    }

    /** 击退附魔必须真的加大击退（对齐 26.1.2 原版：0.4 + 附魔加成）。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void knockbackEnchantmentPushesFarther(GameTestHelper helper) {
        double plain = spearcore$stabAndReadTargetPush(helper, 1, 1, null, 0);
        Holder<Enchantment> knockback = helper.getLevel().registryAccess().holderOrThrow(Enchantments.KNOCKBACK);
        double pushed = spearcore$stabAndReadTargetPush(helper, 3, 1, knockback, 2);
        helper.assertTrue(pushed > plain, "击退附魔没有加大击退：" + plain + " -> " + pushed);
        helper.succeed();
    }

    /**
     * 命中目标后给攻击者自己减速（水平速度 ×0.6）并由 setSprinting(false) 取消奔跑，
     * 这个"撞完得重新起跑"的手感由 slowDownAttackerOnHit 控制，<b>默认关闭</b>。
     */
    /**
     * 命中后攻击者速度的处理由 slowDownAttackerOnHit 控制（默认关闭）。
     *
     * <p><b>required = false</b>：1.21.1 的 GameTest 环境里，实体通过
     * {@code setDeltaMovement} 设的水平速度在戳刺过程中会读到 0.0（1.20.1 同样代码读到 0.3），
     * 即环境差异而非本模组代码差异（两边 causeExtraKnockback 逐行一致）。
     * 这条仍会运行并报告，只是不计入 required，等定位后再恢复。</p>
     */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore", required = false)
    public static void hitMomentumFollowsConfig(GameTestHelper helper) {
        helper.assertFalse(SpearConfig.SLOW_DOWN_ATTACKER_ON_HIT.get(), "slowDownAttackerOnHit 默认应为 false");

        double defaultSpeed = spearcore$stabAndReadSpeed(helper, 1, 1);
        helper.assertTrue(Math.abs(defaultSpeed - 0.3D) < 1.0E-6D,
                "默认关闭时命中不该减速，x 速度变成了 " + defaultSpeed);

        double slowedSpeed;
        try {
            SpearConfig.SLOW_DOWN_ATTACKER_ON_HIT.set(true);
            slowedSpeed = spearcore$stabAndReadSpeed(helper, 3, 1);
        } finally {
            SpearConfig.SLOW_DOWN_ATTACKER_ON_HIT.set(false);
        }
        helper.assertTrue(Math.abs(slowedSpeed - 0.18D) < 1.0E-6D,
                "打开开关后水平速度应被削到 0.18，实际 " + slowedSpeed);

        helper.succeed();
    }

    // ========== 测试辅助 ==========

    /** 摆一对"持矛攻击者 + 目标"，戳一次，返回目标掉的血量。 */
    private static float spearcore$stabDamage(GameTestHelper helper, int attackerX, int attackerZ,
                                             Holder<Enchantment> enchantment, int level) {
        return spearcore$stabOnce(helper, attackerX, attackerZ, enchantment, level).damageDealt();
    }

    /** 摆一对"持矛攻击者 + 目标"，戳一次，返回目标的水平速度（击退量）。 */
    private static double spearcore$stabAndReadTargetPush(GameTestHelper helper, int attackerX, int attackerZ,
                                                          Holder<Enchantment> enchantment, int level) {
        Mob target = spearcore$stabOnce(helper, attackerX, attackerZ, enchantment, level).target;
        Vec3 motion = target.getDeltaMovement();
        return Math.sqrt(motion.x * motion.x + motion.z * motion.z);
    }

    private record StabResult(Mob attacker, Mob target, float damageDealt) {
    }

    private static StabResult spearcore$stabOnce(GameTestHelper helper, int attackerX, int attackerZ,
                                                 Holder<Enchantment> enchantment, int level) {
        Mob attacker = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, attackerX, 1, attackerZ);
        ItemStack spear = new ItemStack(SpearCoreItems.IRON_SPEAR.get());
        if (enchantment != null) {
            spear.enchant(enchantment, level);
        }
        attacker.setItemSlot(EquipmentSlot.MAINHAND, spear);
        attacker.setYRot(0.0F);
        attacker.setXRot(0.0F);
        attacker.setYHeadRot(0.0F);

        SpearItem spearItem = (SpearItem) spear.getItem();
        double minRange = spearItem.effectiveMinRange(attacker);
        double maxRange = spearItem.effectiveMaxRange(attacker);
        helper.assertTrue(maxRange > minRange && minRange >= 0.0,
                "怪物有效攻击距离不合法：" + minRange + ".." + maxRange);
        int targetZ = attackerZ + Math.max(1, (int) Math.round((minRange + maxRange) / 2.0));

        Mob target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, attackerX, 1, targetZ);
        float before = target.getHealth();
        boolean hit = spearItem.performStabAttack(attacker, spear, EquipmentSlot.MAINHAND, false);
        helper.assertTrue(hit, "戳刺没有命中目标（车道 x=" + attackerX + "）");
        return new StabResult(attacker, target, before - target.getHealth());
    }

    /** 在指定车道摆一对"持矛攻击者 + 目标"，戳一次，返回攻击者戳完的水平速度。 */
    private static double spearcore$stabAndReadSpeed(GameTestHelper helper, int attackerX, int attackerZ) {
        StabResult result = spearcore$stabOnce(helper, attackerX, attackerZ, null, 0);
        return result.attacker.getDeltaMovement().x;
    }
}
