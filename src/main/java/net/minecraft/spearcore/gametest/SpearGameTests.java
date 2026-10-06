package net.minecraft.spearcore.gametest;

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
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * GameTest：由游戏本体自己跑、自己判定、自己报告失败数。
 *
 * <p>判定就是退出码与日志里的 "All N required tests passed :)" /
 * "N required tests failed :("；模板结构由 gameteststructures/ 提供，
 * Gradle 的 stageGameTestStructures 任务负责拷进 run/。</p>
 */
@Mod.EventBusSubscriber(modid = SpearcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SpearGameTests {

    private static final String TEMPLATE = "empty5x5x5";

    private SpearGameTests() {
    }

    @SubscribeEvent
    public static void onRegisterGameTests(RegisterGameTestsEvent event) {
        event.register(SpearGameTests.class);
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

    /** 移植后的属性修饰符代码（EquipmentSlot + Multimap + ENTITY_REACH）确实挂上了属性。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void spearAttributesAreRegistered(GameTestHelper helper) {
        ItemStack stack = new ItemStack(SpearCoreItems.IRON_SPEAR.get());
        var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        helper.assertTrue(modifiers.containsKey(Attributes.ATTACK_DAMAGE), "缺少攻击力修饰符");
        helper.assertTrue(modifiers.containsKey(Attributes.ATTACK_SPEED), "缺少攻击速度修饰符");
        helper.assertTrue(modifiers.containsKey(ForgeMod.ENTITY_REACH.get()), "缺少交互距离修饰符（ENTITY_REACH）");
        helper.succeed();
    }

    /**
     * 真正打一次：持矛实体戳中正前方的目标，目标掉血。
     *
     * <p>这条同时是"世界刚开时戳刺会被静默跳过"那个 bug 的回归测试——
     * GameTest 跑在全新世界里，gameTime 很小，正好命中该边界。</p>
     */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void stabDamagesTarget(GameTestHelper helper) {
        Mob attacker = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        ItemStack spear = new ItemStack(SpearCoreItems.IRON_SPEAR.get());
        attacker.setItemSlot(EquipmentSlot.MAINHAND, spear);
        attacker.setYRot(0.0F);
        attacker.setXRot(0.0F);
        attacker.setYHeadRot(0.0F);

        // 关键：怪物有 mobFactor（默认 0.5）缩减，有效射程只有玩家的一半，
        // 不能按玩家的 minRange/maxRange 摆目标，否则永远打不中。
        SpearItem probe = (SpearItem) spear.getItem();
        double minRange = probe.effectiveMinRange(attacker);
        double maxRange = probe.effectiveMaxRange(attacker);
        helper.assertTrue(maxRange > minRange && minRange >= 0.0,
                "怪物有效攻击距离不合法：" + minRange + ".." + maxRange);
        int targetZ = 1 + Math.max(1, (int) Math.round((minRange + maxRange) / 2.0));

        Mob target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, targetZ);
        float before = target.getHealth();

        SpearItem spearItem = (SpearItem) spear.getItem();
        boolean hit = spearItem.performStabAttack(attacker, spear, EquipmentSlot.MAINHAND, false);

        helper.assertTrue(hit, "戳刺没有命中目标（怪物射程 " + spearItem.effectiveMinRange(attacker)
                + ".." + spearItem.effectiveMaxRange(attacker) + "，目标在 " + (targetZ - 1) + " 格外）");
        helper.assertTrue(target.getHealth() < before,
                "戳刺没有造成伤害（" + before + " -> " + target.getHealth() + "）");
        helper.succeed();
    }

    /**
     * 命中目标后给攻击者自己减速（水平速度 ×0.6）并由 setSprinting(false) 取消奔跑，
     * 这个"撞完得重新起跑"的手感由 slowDownAttackerOnHit 控制，<b>默认关闭</b>。
     *
     * <p>两段都在同一个测试里跑，避免翻配置影响到并行/同批次的其他测试。</p>
     */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void hitMomentumFollowsConfig(GameTestHelper helper) {
        helper.assertFalse(SpearConfig.SLOW_DOWN_ATTACKER_ON_HIT.get(), "slowDownAttackerOnHit 默认应为 false");

        // 第一段：默认（关闭）—— 命中后水平速度不变，不用重新起跑
        double defaultSpeed = spearcore$stabAndReadSpeed(helper, 1, 1);
        helper.assertTrue(Math.abs(defaultSpeed - 0.3D) < 1.0E-6D,
                "默认关闭时命中不该减速，x 速度变成了 " + defaultSpeed);

        // 第二段：打开开关 —— 应当被削到 0.3 × 0.6 = 0.18
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
        float sharp = spearcore$stabDamage(helper, 3, 1, Enchantments.SHARPNESS, 5);
        helper.assertTrue(sharp > plain, "锋利附魔没有提高戳刺伤害：" + plain + " -> " + sharp);
        helper.succeed();
    }

    /** 击退附魔必须真的加大击退（对齐 26.1.2 原版：0.4 + 附魔加成）。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void knockbackEnchantmentPushesFarther(GameTestHelper helper) {
        double plain = spearcore$stabAndReadTargetPush(helper, 1, 1, null, 0);
        double pushed = spearcore$stabAndReadTargetPush(helper, 3, 1, Enchantments.KNOCKBACK, 2);
        helper.assertTrue(pushed > plain, "击退附魔没有加大击退：" + plain + " -> " + pushed);
        helper.succeed();
    }

    /** 摆一对"持矛攻击者 + 目标"，戳一次，返回目标掉的血量。 */
    private static float spearcore$stabDamage(GameTestHelper helper, int attackerX, int attackerZ,
                                             Enchantment enchantment, int level) {
        return spearcore$stabOnce(helper, attackerX, attackerZ, enchantment, level).damageDealt();
    }

    /** 摆一对"持矛攻击者 + 目标"，戳一次，返回目标的水平速度（击退量）。 */
    private static double spearcore$stabAndReadTargetPush(GameTestHelper helper, int attackerX, int attackerZ,
                                                          Enchantment enchantment, int level) {
        Mob target = spearcore$stabOnce(helper, attackerX, attackerZ, enchantment, level).target();
        net.minecraft.world.phys.Vec3 motion = target.getDeltaMovement();
        return Math.sqrt(motion.x * motion.x + motion.z * motion.z);
    }

    private record StabResult(Mob attacker, Mob target, float damageDealt) {
    }

    /** 摆一对"持矛攻击者 + 目标"并戳一次；矛按需附魔。 */
    private static StabResult spearcore$stabOnce(GameTestHelper helper, int attackerX, int attackerZ,
                                                 Enchantment enchantment, int level) {
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

    /** 在指定 x 车道摆一对"持矛攻击者 + 目标"，戳一次，返回攻击者戳完的水平速度。 */
    private static double spearcore$stabAndReadSpeed(GameTestHelper helper, int attackerX, int attackerZ) {
        Mob attacker = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, attackerX, 1, attackerZ);
        ItemStack spear = new ItemStack(SpearCoreItems.IRON_SPEAR.get());
        attacker.setItemSlot(EquipmentSlot.MAINHAND, spear);
        attacker.setYRot(0.0F);
        attacker.setXRot(0.0F);
        attacker.setYHeadRot(0.0F);

        SpearItem spearItem = (SpearItem) spear.getItem();
        // 怪物有 mobFactor（默认 0.5）缩减，目标要按怪物自己的有效射程摆
        double minRange = spearItem.effectiveMinRange(attacker);
        double maxRange = spearItem.effectiveMaxRange(attacker);
        int targetZ = attackerZ + Math.max(1, (int) Math.round((minRange + maxRange) / 2.0));
        helper.spawnWithNoFreeWill(EntityType.ZOMBIE, attackerX, 1, targetZ);

        attacker.setDeltaMovement(0.3D, 0.0D, 0.0D);
        boolean hit = spearItem.performStabAttack(attacker, spear, EquipmentSlot.MAINHAND, false);
        helper.assertTrue(hit, "戳刺没有命中目标（车道 x=" + attackerX + "）");
        return attacker.getDeltaMovement().x;
    }
}
