package net.minecraft.spearcore.gametest;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.config.SpearConfig;
import net.minecraft.spearcore.init.SpearCoreItems;
import net.minecraft.spearcore.init.SpearStats;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.spearcore.util.SpearRules;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

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

    /** 属性修饰符只该有攻击力/攻击速度：射程由 stats 的 2/4.5 加上持有者 reach 超出 3.0 的部分换算，物品上不能再挂一份。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void spearAttributesAreRegistered(GameTestHelper helper) {
        ItemStack stack = new ItemStack(SpearCoreItems.IRON_SPEAR.get());
        var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        helper.assertTrue(modifiers.containsKey(Attributes.ATTACK_DAMAGE), "缺少攻击力修饰符");
        helper.assertTrue(modifiers.containsKey(Attributes.ATTACK_SPEED), "缺少攻击速度修饰符");
        helper.assertFalse(modifiers.containsKey(ForgeMod.ENTITY_REACH.get()),
                "长矛不应再挂 ENTITY_REACH 修饰符（射程已由 stats + 持有者 reach 换算，重复计入会变成 6 格）");
        helper.succeed();
    }

    /** 攻击距离必须等于原版：玩家 2~4.5、创造 6.5、怪物 2.25。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void attackRangeFollowsVanilla(GameTestHelper helper) {
        for (Item item : spearcore$allSpears()) {
            SpearItem spear = (SpearItem) item;
            helper.assertTrue(spear.getMinRange() == 2.0F, "最小攻击距离应为 2，实际 " + spear.getMinRange());
            helper.assertTrue(spear.getMaxRange() == 4.5F, "最大攻击距离应为 4.5，实际 " + spear.getMaxRange());
            helper.assertTrue(spear.getMaxCreativeRange() == 6.5F,
                    "创造模式最大攻击距离应为 6.5，实际 " + spear.getMaxCreativeRange());
            helper.assertTrue(spear.getMobFactor() == 0.5F, "怪物射程系数应为 0.5，实际 " + spear.getMobFactor());
        }
        Mob mob = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        SpearItem iron = (SpearItem) SpearCoreItems.IRON_SPEAR.get();
        helper.assertTrue(Math.abs(iron.effectiveMaxRange(mob) - 2.25F) < 1.0e-4F,
                "怪物最大攻击距离应为 2.25，实际 " + iron.effectiveMaxRange(mob));
        helper.succeed();
    }

    /** 整合包给持有者加 reach 时，长矛射程必须跟着变长（不能被上面的修复一刀切掉）。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void reachBonusStillApplies(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        SpearItem iron = (SpearItem) SpearCoreItems.IRON_SPEAR.get();
        helper.assertTrue(Math.abs(iron.effectiveMaxRange(player) - 4.5F) < 1.0e-4F,
                "无加成时玩家最大攻击距离应为 4.5，实际 " + iron.effectiveMaxRange(player));
        AttributeInstance reach = player.getAttribute(ForgeMod.ENTITY_REACH.get());
        helper.assertTrue(reach != null, "测试玩家没有 ENTITY_REACH 属性");
        AttributeModifier bonus = new AttributeModifier(UUID.randomUUID(), "game test reach",
                2.0D, AttributeModifier.Operation.ADDITION);
        reach.addTransientModifier(bonus);
        try {
            helper.assertTrue(Math.abs(iron.effectiveMaxRange(player) - 6.5F) < 1.0e-4F,
                    "reach +2 没有计入长矛射程，实际 " + iron.effectiveMaxRange(player));
        } finally {
            reach.removeModifier(bonus);
        }
        helper.succeed();
    }

    /** 判定箱对齐原版：蓄力 0.125、戳刺 0.25。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void jabHitboxMatchesVanilla(GameTestHelper helper) {
        for (Item item : spearcore$allSpears()) {
            SpearItem spear = (SpearItem) item;
            helper.assertTrue(spear.getHitboxMargin() == 0.125F,
                    "蓄力判定箱应为 0.125，实际 " + spear.getHitboxMargin());
            helper.assertTrue(spear.getHitboxMargin2() == 0.25F,
                    "戳刺判定箱应为 0.25，实际 " + spear.getHitboxMargin2());
        }
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
     * 这个"撞完得重新起跑"的手感由 {@code behavior.hitSlowDown} 控制：
     * JAVA（默认）有、MODDED 没有。
     *
     * <p>两段都在同一个测试里跑，避免翻配置影响到并行/同批次的其他测试。</p>
     */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void hitMomentumFollowsConfig(GameTestHelper helper) {
        // 测试不依赖本地配置文件：先强制到默认值，跑完再恢复原值
        SpearRules.HitSlowDown original = SpearConfig.HIT_SLOW_DOWN.get();
        SpearConfig.HIT_SLOW_DOWN.set(SpearRules.HitSlowDown.JAVA);
        helper.assertTrue(SpearRules.slowsDownAttackerOnHit(),
                "hitSlowDown 默认应为 JAVA（测试内已强制复位）");

        // 第一段：JAVA —— 命中后自己也被削到 0.3 × 0.6 = 0.18
        double vanillaSpeed = spearcore$stabAndReadSpeed(helper, 1, 1);
        helper.assertTrue(Math.abs(vanillaSpeed - 0.18D) < 1.0E-6D,
                "JAVA 模式命中后自己应减速到 0.18，实际 x 速度 " + vanillaSpeed);

        // 第二段：MODDED —— 命中后速度不变，不用重新起跑
        double moddedSpeed;
        try {
            SpearConfig.HIT_SLOW_DOWN.set(SpearRules.HitSlowDown.MODDED);
            moddedSpeed = spearcore$stabAndReadSpeed(helper, 3, 1);
        } finally {
            SpearConfig.HIT_SLOW_DOWN.set(original);
        }
        helper.assertTrue(Math.abs(moddedSpeed - 0.3D) < 1.0E-6D,
                "MODDED 模式命中不该减速，x 速度变成了 " + moddedSpeed);

        helper.succeed();
    }

    /** 耐久按 behavior.durability 切换：JAVA 金 32 / BEDROCK 33 / MODDED 59。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void durabilityFollowsConfig(GameTestHelper helper) {
        SpearRules.Durability original = SpearConfig.DURABILITY.get();
        try {
            SpearConfig.DURABILITY.set(SpearRules.Durability.JAVA);
            helper.assertTrue(spearcore$maxDamage(SpearCoreItems.GOLDEN_SPEAR.get()) == 32,
                    "JAVA 金矛耐久应为 32，实际 " + spearcore$maxDamage(SpearCoreItems.GOLDEN_SPEAR.get()));
            helper.assertTrue(spearcore$maxDamage(SpearCoreItems.IRON_SPEAR.get()) == 250,
                    "JAVA 铁矛耐久应为 250，实际 " + spearcore$maxDamage(SpearCoreItems.IRON_SPEAR.get()));

            SpearConfig.DURABILITY.set(SpearRules.Durability.BEDROCK);
            helper.assertTrue(spearcore$maxDamage(SpearCoreItems.GOLDEN_SPEAR.get()) == 33,
                    "BEDROCK 金矛耐久应为 33，实际 " + spearcore$maxDamage(SpearCoreItems.GOLDEN_SPEAR.get()));
            helper.assertTrue(spearcore$maxDamage(SpearCoreItems.IRON_SPEAR.get()) == 251,
                    "BEDROCK 铁矛耐久应为 251，实际 " + spearcore$maxDamage(SpearCoreItems.IRON_SPEAR.get()));

            SpearConfig.DURABILITY.set(SpearRules.Durability.MODDED);
            helper.assertTrue(spearcore$maxDamage(SpearCoreItems.GOLDEN_SPEAR.get()) == 59,
                    "MODDED 金矛耐久应为 59，实际 " + spearcore$maxDamage(SpearCoreItems.GOLDEN_SPEAR.get()));
        } finally {
            SpearConfig.DURABILITY.set(original);
        }
        helper.succeed();
    }

    /** 蓄力判定箱：JAVA 0.125 / MODDED 0.25。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void hitboxMarginFollowsConfig(GameTestHelper helper) {
        SpearRules.ChargeHitboxMargin original = SpearConfig.CHARGE_HITBOX_MARGIN.get();
        SpearItem iron = (SpearItem) SpearCoreItems.IRON_SPEAR.get();
        try {
            SpearConfig.CHARGE_HITBOX_MARGIN.set(SpearRules.ChargeHitboxMargin.JAVA);
            helper.assertTrue(Math.abs(iron.getHitboxMargin() - 0.125F) < 1.0E-6F,
                    "JAVA 判定箱膨胀应为 0.125，实际 " + iron.getHitboxMargin());
            SpearConfig.CHARGE_HITBOX_MARGIN.set(SpearRules.ChargeHitboxMargin.MODDED);
            helper.assertTrue(Math.abs(iron.getHitboxMargin() - 0.25F) < 1.0E-6F,
                    "MODDED 判定箱膨胀应为 0.25，实际 " + iron.getHitboxMargin());
        } finally {
            SpearConfig.CHARGE_HITBOX_MARGIN.set(original);
        }
        helper.succeed();
    }

    /** 创造模式攻击距离：JAVA 6.5 / BEDROCK 7.5。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void creativeRangeFollowsConfig(GameTestHelper helper) {
        SpearRules.CreativeMaxRange original = SpearConfig.CREATIVE_MAX_RANGE.get();
        SpearItem iron = (SpearItem) SpearCoreItems.IRON_SPEAR.get();
        try {
            SpearConfig.CREATIVE_MAX_RANGE.set(SpearRules.CreativeMaxRange.JAVA);
            helper.assertTrue(Math.abs(iron.getMaxCreativeRange() - 6.5F) < 1.0E-6F,
                    "JAVA 创造距离上限应为 6.5，实际 " + iron.getMaxCreativeRange());
            SpearConfig.CREATIVE_MAX_RANGE.set(SpearRules.CreativeMaxRange.BEDROCK);
            helper.assertTrue(Math.abs(iron.getMaxCreativeRange() - 7.5F) < 1.0E-6F,
                    "BEDROCK 创造距离上限应为 7.5，实际 " + iron.getMaxCreativeRange());
        } finally {
            SpearConfig.CREATIVE_MAX_RANGE.set(original);
        }
        helper.succeed();
    }

    /** 戳刺伤害：JAVA 铁矛 +2 攻击力 / BEDROCK +3（每级 +1 点）。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void jabDamageFollowsConfig(GameTestHelper helper) {
        SpearRules.JabDamage original = SpearConfig.JAB_DAMAGE.get();
        try {
            SpearConfig.JAB_DAMAGE.set(SpearRules.JabDamage.JAVA);
            helper.assertTrue(Math.abs(spearcore$attackDamageBonus(SpearCoreItems.IRON_SPEAR.get()) - 2.0F) < 1.0E-6F,
                    "JAVA 铁矛攻击力加成应为 2，实际 " + spearcore$attackDamageBonus(SpearCoreItems.IRON_SPEAR.get()));
            SpearConfig.JAB_DAMAGE.set(SpearRules.JabDamage.BEDROCK);
            helper.assertTrue(Math.abs(spearcore$attackDamageBonus(SpearCoreItems.IRON_SPEAR.get()) - 3.0F) < 1.0E-6F,
                    "BEDROCK 铁矛攻击力加成应为 3，实际 " + spearcore$attackDamageBonus(SpearCoreItems.IRON_SPEAR.get()));
        } finally {
            SpearConfig.JAB_DAMAGE.set(original);
        }
        helper.succeed();
    }

    /** 最小攻击距离：JAVA 有下限（怪物按 mobFactor 减半）/ MODDED 归零，贴脸也能出手。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void minAttackRangeFollowsConfig(GameTestHelper helper) {
        SpearItem iron = (SpearItem) SpearCoreItems.IRON_SPEAR.get();
        Mob attacker = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        SpearRules.MinAttackRange original = SpearConfig.MIN_ATTACK_RANGE.get();
        try {
            SpearConfig.MIN_ATTACK_RANGE.set(SpearRules.MinAttackRange.JAVA);
            helper.assertTrue(Math.abs(iron.effectiveMinRange(attacker) - 1.0F) < 1.0E-6F,
                    "JAVA 怪物最小攻击距离应为 2×0.5=1.0，实际 " + iron.effectiveMinRange(attacker));
            SpearConfig.MIN_ATTACK_RANGE.set(SpearRules.MinAttackRange.MODDED);
            helper.assertTrue(iron.effectiveMinRange(attacker) == 0.0F,
                    "MODDED 最小攻击距离应为 0，实际 " + iron.effectiveMinRange(attacker));
        } finally {
            SpearConfig.MIN_ATTACK_RANGE.set(original);
        }
        helper.succeed();
    }

    /** 蓄力基线是否吃攻击力修饰符：JAVA 只取裸值（力量药水无效，原版 MC-302778）/ BEDROCK 吃修饰符。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void chargeStrengthFollowsConfig(GameTestHelper helper) {
        SpearItem iron = (SpearItem) SpearCoreItems.IRON_SPEAR.get();
        Mob user = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
        user.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
                new AttributeModifier("spearcore_test_strength", 5.0D, AttributeModifier.Operation.ADDITION));
        SpearRules.ChargeStrength original = SpearConfig.CHARGE_STRENGTH.get();
        try {
            SpearConfig.CHARGE_STRENGTH.set(SpearRules.ChargeStrength.JAVA);
            float javaBase = iron.chargeBaseDamage(user);
            helper.assertTrue(
                    Math.abs(javaBase - (float) user.getAttributeBaseValue(Attributes.ATTACK_DAMAGE)) < 1.0E-6F,
                    "JAVA 蓄力基线应忽略修饰符，实际 " + javaBase);
            SpearConfig.CHARGE_STRENGTH.set(SpearRules.ChargeStrength.BEDROCK);
            float bedrockBase = iron.chargeBaseDamage(user);
            helper.assertTrue(Math.abs(bedrockBase - (javaBase + 5.0F)) < 1.0E-6F,
                    "BEDROCK 蓄力基线应包含 +5 力量，实际 " + bedrockBase + "（JAVA " + javaBase + "）");
        } finally {
            SpearConfig.CHARGE_STRENGTH.set(original);
        }
        helper.succeed();
    }

    /** 戳刺后重新蓄力的间隔：JAVA 0 tick / BEDROCK 该矛的使用冷却（铁 19t、木 13t）。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void jabToChargeDelayFollowsConfig(GameTestHelper helper) {
        SpearItem iron = (SpearItem) SpearCoreItems.IRON_SPEAR.get();
        SpearItem wood = (SpearItem) SpearCoreItems.WOODEN_SPEAR.get();
        helper.assertTrue(iron.getUseCooldownTicks() == 19,
                "铁矛使用冷却应为 19t，实际 " + iron.getUseCooldownTicks());
        helper.assertTrue(wood.getUseCooldownTicks() == 13,
                "木矛使用冷却应为 13t，实际 " + wood.getUseCooldownTicks());
        SpearRules.JabToChargeDelay original = SpearConfig.JAB_TO_CHARGE_DELAY.get();
        try {
            SpearConfig.JAB_TO_CHARGE_DELAY.set(SpearRules.JabToChargeDelay.JAVA);
            helper.assertTrue(SpearRules.jabToChargeDelayTicks(19) == 0, "JAVA 戳刺后不应有间隔");
            SpearConfig.JAB_TO_CHARGE_DELAY.set(SpearRules.JabToChargeDelay.BEDROCK);
            helper.assertTrue(SpearRules.jabToChargeDelayTicks(19) == 19, "BEDROCK 铁矛戳刺后应冷却 19t");
            helper.assertTrue(SpearRules.jabToChargeDelayTicks(13) == 13, "BEDROCK 木矛戳刺后应冷却 13t");
        } finally {
            SpearConfig.JAB_TO_CHARGE_DELAY.set(original);
        }
        helper.succeed();
    }

    /** 移动靶补偿（MC-310857）：JAVA 按服务器真实位置判定 / MODDED 按客户端看到的位置回退 3 tick。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void movingTargetAssistFollowsConfig(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        Mob moving = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 3);
        moving.setDeltaMovement(0.28D, 0.0D, 0.0D);
        SpearRules.MovingTargetAssist original = SpearConfig.MOVING_TARGET_ASSIST.get();
        try {
            SpearConfig.MOVING_TARGET_ASSIST.set(SpearRules.MovingTargetAssist.JAVA);
            helper.assertTrue(SpearItem.movingTargetCompensation(player) == 0.0D, "JAVA 不应有移动靶补偿");
            double javaShift = SpearItem.compensatedTargetBox(player, moving, 0.0F).minX
                    - moving.getBoundingBox().minX;
            helper.assertTrue(Math.abs(javaShift) < 1.0E-6D, "JAVA 判定箱不应被移动，实际偏移 " + javaShift);

            SpearConfig.MOVING_TARGET_ASSIST.set(SpearRules.MovingTargetAssist.MODDED);
            helper.assertTrue(Math.abs(SpearItem.movingTargetCompensation(player) - 3.0D) < 1.0E-6D,
                    "MODDED 应回退 3 tick，实际 " + SpearItem.movingTargetCompensation(player));
            double moddedShift = SpearItem.compensatedTargetBox(player, moving, 0.0F).minX
                    - moving.getBoundingBox().minX;
            helper.assertTrue(Math.abs(moddedShift - (-0.28D * 3.0D)) < 1.0E-6D,
                    "MODDED 判定箱应按 0.28 格/tick × 3 回退，实际偏移 " + moddedShift);
        } finally {
            SpearConfig.MOVING_TARGET_ASSIST.set(original);
        }
        helper.succeed();
    }

    /** 击落骑手的速度阈值写死 Java 版数据，与任何配置项无关。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void dismountThresholdIsVanillaData(GameTestHelper helper) {
        helper.assertTrue(Math.abs(SpearStats.dismountSpeedFor("wood") - 14.0F) < 1.0E-6F, "木矛击落阈值应为 14");
        helper.assertTrue(Math.abs(SpearStats.dismountSpeedFor("gold") - 13.0F) < 1.0E-6F, "金矛击落阈值应为 13");
        helper.assertTrue(Math.abs(SpearStats.dismountSpeedFor("stone") - 13.0F) < 1.0E-6F, "石矛击落阈值应为 13");
        helper.assertTrue(Math.abs(SpearStats.dismountSpeedFor("copper") - 12.0F) < 1.0E-6F, "铜矛击落阈值应为 12");
        helper.assertTrue(Math.abs(SpearStats.dismountSpeedFor("iron") - 11.0F) < 1.0E-6F, "铁矛击落阈值应为 11");
        helper.assertTrue(Math.abs(SpearStats.dismountSpeedFor("diamond") - 10.0F) < 1.0E-6F, "钻矛击落阈值应为 10");
        helper.assertTrue(Math.abs(SpearStats.dismountSpeedFor("netherite") - 9.0F) < 1.0E-6F, "下界合金矛击落阈值应为 9");

        var ironStats = SpearStats.get(SpearCoreItems.IRON_SPEAR.get());
        helper.assertTrue(ironStats.dismounts(), "铁矛应当能击落骑手");
        helper.assertTrue(ironStats.dismountConditions().isPresent(), "铁矛应当带击落条件");
        helper.assertTrue(Math.abs(ironStats.dismountConditions().get().minSpeed() - 11.0F) < 1.0E-6F,
                "铁矛击落条件里的速度阈值应为 11，实际 "
                        + ironStats.dismountConditions().get().minSpeed());
        helper.succeed();
    }

    /** 突进力度：JAVA 0.458 / MODDED 0.65。 */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void lungeStrengthFollowsConfig(GameTestHelper helper) {
        SpearRules.Lunge original = SpearConfig.LUNGE.get();
        try {
            SpearConfig.LUNGE.set(SpearRules.Lunge.JAVA);
            helper.assertTrue(Math.abs(SpearRules.lungeStrength() - 0.458F) < 1.0E-6F,
                    "JAVA 突进力度应为 0.458，实际 " + SpearRules.lungeStrength());
            SpearConfig.LUNGE.set(SpearRules.Lunge.MODDED);
            helper.assertTrue(Math.abs(SpearRules.lungeStrength() - 0.65F) < 1.0E-6F,
                    "MODDED 突进力度应为 0.65，实际 " + SpearRules.lungeStrength());
        } finally {
            SpearConfig.LUNGE.set(original);
        }
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

    /** 读一件长矛当前配置下的最大耐久。 */
    private static int spearcore$maxDamage(Item item) {
        return new ItemStack(item).getMaxDamage();
    }

    /** 读一件长矛主手时的攻击力修饰符数值（ADDITION 的 amount）。 */
    private static float spearcore$attackDamageBonus(Item item) {
        var modifiers = new ItemStack(item).getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE);
        if (modifiers == null || modifiers.isEmpty()) {
            return 0.0F;
        }
        return (float) modifiers.iterator().next().getAmount();
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

    /**
     * 长矛必须挂进合适的物品 tag —— 这是和其他模组/数据包互通的前提。
     *
     * <p>1.20.1 的 tag 目录是 tags/items（复数），写成 1.21 的 tags/item 会被静默忽略，
     * 原仓库就是踩了这个坑（所有 tag 实际都没生效）。这条测试同时验证本模组 tag
     * 与原版 / Forge 的归类 tag，防止再犯。</p>
     */
    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = SpearcoreMod.MODID, template = TEMPLATE, batch = "spearcore")
    public static void spearsCarryItemTags(GameTestHelper helper) {
        TagKey<Item> spearsTag = TagKey.create(Registries.ITEM, new ResourceLocation(SpearcoreMod.MODID, "spears"));
        TagKey<Item> spearTag = TagKey.create(Registries.ITEM, new ResourceLocation(SpearcoreMod.MODID, "spear"));
        TagKey<Item> swordsTag = TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "swords"));
        TagKey<Item> toolsTag = TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "tools"));
        TagKey<Item> minecraftSpearsTag = TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "spears"));
        TagKey<Item> forgeToolsTag = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "tools"));
        TagKey<Item> piglinLovedTag = TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "piglin_loved"));

        // #spearcore:spears 按上游约定只含「原版材质」的六把（铜是模组自加的，单独一个 tag）
        Item[] vanillaMaterialSpears = {
                SpearCoreItems.WOODEN_SPEAR.get(),
                SpearCoreItems.STONE_SPEAR.get(),
                SpearCoreItems.IRON_SPEAR.get(),
                SpearCoreItems.GOLDEN_SPEAR.get(),
                SpearCoreItems.DIAMOND_SPEAR.get(),
                SpearCoreItems.NETHERITE_SPEAR.get(),
        };
        Item[] allSpears = {
                SpearCoreItems.WOODEN_SPEAR.get(),
                SpearCoreItems.STONE_SPEAR.get(),
                SpearCoreItems.COPPER_SPEAR.get(),
                SpearCoreItems.IRON_SPEAR.get(),
                SpearCoreItems.GOLDEN_SPEAR.get(),
                SpearCoreItems.DIAMOND_SPEAR.get(),
                SpearCoreItems.NETHERITE_SPEAR.get(),
        };
        for (Item item : vanillaMaterialSpears) {
            helper.assertTrue(new ItemStack(item).is(spearsTag), "长矛没有进入 #spearcore:spears：" + item);
        }
        for (Item item : allSpears) {
            ItemStack stack = new ItemStack(item);
            helper.assertTrue(stack.is(spearTag), "长矛没有进入 #spearcore:spear：" + item);
            helper.assertTrue(stack.is(minecraftSpearsTag), "长矛没有进入 #minecraft:spears：" + item);
            helper.assertTrue(stack.is(swordsTag), "长矛没有进入 #minecraft:swords：" + item);
            helper.assertTrue(stack.is(toolsTag), "长矛没有进入 #minecraft:tools：" + item);
            helper.assertTrue(stack.is(forgeToolsTag), "长矛没有进入 #forge:tools：" + item);
        }
        helper.assertTrue(new ItemStack(SpearCoreItems.GOLDEN_SPEAR.get()).is(piglinLovedTag),
                "金矛没有进入 #minecraft:piglin_loved");
        helper.succeed();
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

    /** 七把长矛的实例，顺序固定（木/石/铜/铁/金/钻/下界）。 */
    private static Item[] spearcore$allSpears() {
        return new Item[]{
                SpearCoreItems.WOODEN_SPEAR.get(),
                SpearCoreItems.STONE_SPEAR.get(),
                SpearCoreItems.COPPER_SPEAR.get(),
                SpearCoreItems.IRON_SPEAR.get(),
                SpearCoreItems.GOLDEN_SPEAR.get(),
                SpearCoreItems.DIAMOND_SPEAR.get(),
                SpearCoreItems.NETHERITE_SPEAR.get(),
        };
    }
}