package net.minecraft.spearcore.config;

import net.minecraft.spearcore.util.SpearRules;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class SpearConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_VANILLA_SPEARS = BUILDER
            .comment("是否启用原版材质长矛（木、石、铁、金、钻石、下界合金）",
                    "默认开启：会出现在创造模式物品栏，怪物生成时也会携带。",
                    "关掉不影响合成与 /give——物品一直在，只是不在创造栏里、怪物也不带。")
            .define("enableVanillaSpears", true);

    public static final ForgeConfigSpec.BooleanValue ENABLE_COPPER_SPEAR = BUILDER
            .comment("是否启用铜长矛",
                    "默认开启：会出现在创造模式物品栏，怪物生成时也会携带。")
            .define("enableCopperSpear", true);

    /**
     * 安装 Punchy 时，让其矛蓄力动画按本模组各材质的阶段(tick)切换。
     * 关闭则用 Punchy 自带的固定时间轴。仅客户端读取。
     */
    public static final ForgeConfigSpec.BooleanValue PUNCHY_STAGE_BRIDGE = BUILDER
            .comment("安装 Punchy 时，让其矛蓄力动画按本模组各材质的阶段(tick)切换；关闭则用 Punchy 自带的固定时间轴")
            .define("punchyStageBridge", true);

    // ========== 行为开关 ==========
    // 每个细节一个独立枚举，默认 JAVA（= 原版 Java 版行为）。
    // 可选值只列出"行为确实不同"的版本：与 Java 一致的版本不会作为选项中重复出现。

    static {
        BUILDER.push("behavior");
    }

    public static final ForgeConfigSpec.EnumValue<SpearRules.JabDamage> JAB_DAMAGE = BUILDER
            .comment("戳刺伤害。",
                    "JAVA（默认）：与原版 Java 版一致，木/金 1、石/铜 2、铁 3、钻 4、下界合金 5 点。",
                    "BEDROCK：与原版基岩版一致，每级 +1 点伤害（木/金 2 … 下界合金 6）。",
                    "两种值都会同时体现在属性面板与伤害结算上；蓄力伤害用武器攻击力做基线，因此也会跟着 +1。")
            .defineEnum("jabDamage", SpearRules.JabDamage.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.JabSpeedBonus> JAB_SPEED_BONUS = BUILDER
            .comment("戳刺的速度加成。",
                    "JAVA（默认）：按原版 Java/基岩版，戳刺伤害只取决于武器攻击力。",
                    "MODDED：本模组原本的行为，额外加上 floor(攻速 × 倍率)，疾跑冲锋戳刺更疼。")
            .defineEnum("jabSpeedBonus", SpearRules.JabSpeedBonus.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.JabCooldown> JAB_COOLDOWN = BUILDER
            .comment("戳刺的冷却模型。",
                    "JAVA（默认）：必须等原版攻击蓄力条回满（≥95%）才能再戳。",
                    "BEDROCK：改用物的使用冷却（木 0.65s … 下界合金 1.15s），",
                    "          并且同一玩家背包里所有长矛共享这个冷却。")
            .defineEnum("jabCooldown", SpearRules.JabCooldown.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.ChargeHitboxMargin> CHARGE_HITBOX_MARGIN = BUILDER
            .comment("蓄力时判定箱的额外膨胀量。",
                    "JAVA（默认）：0.125，与原版一致，判定更紧。",
                    "MODDED：0.25，本模组原本的数值，更容易蹭到目标。")
            .defineEnum("chargeHitboxMargin", SpearRules.ChargeHitboxMargin.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.ChargeRepeatHit> CHARGE_REPEAT_HIT = BUILDER
            .comment("蓄力命中同一目标后的重复结算间隔。",
                    "JAVA（默认）：每个目标每次蓄力只结算一次。",
                    "BEDROCK：同一目标 10 tick（0.5s）内只结算一次，之后可再次命中；这也等于本模组原本的行为。")
            .defineEnum("chargeRepeatHit", SpearRules.ChargeRepeatHit.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.JabRepeatHit> JAB_REPEAT_HIT = BUILDER
            .comment("戳刺命中同一目标后的重复结算间隔。",
                    "JAVA（默认）：与原版一致，没有额外冷却。",
                    "MODDED：同一目标 10 tick 内跳过，防止连续左键多段伤害。")
            .defineEnum("jabRepeatHit", SpearRules.JabRepeatHit.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.Durability> DURABILITY = BUILDER
            .comment("长矛耐久。",
                    "JAVA（默认）：原版 Java 版数值，木 59 / 石 131 / 铜 190 / 铁 250 / 金 32 / 钻 1561 / 下界合金 2031。",
                    "BEDROCK：原版基岩版数值，每级比 Java 多 1 点。",
                    "MODDED：本模组原本的 1.20.1 工具材质数值，铜 195、金 59。")
            .defineEnum("durability", SpearRules.Durability.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.CreativeMaxRange> CREATIVE_MAX_RANGE = BUILDER
            .comment("创造模式下的长矛攻击距离上限。",
                    "JAVA（默认）：6.5。",
                    "BEDROCK：7.5（未经实测，按社区资料）。")
            .defineEnum("creativeMaxRange", SpearRules.CreativeMaxRange.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.OffhandCharging> OFFHAND_CHARGING = BUILDER
            .comment("能否用副手举矛蓄力。",
                    "JAVA（默认）：可以，与原版 Java 版一致。",
                    "BEDROCK：不行，与原版基岩版一致。")
            .defineEnum("offhandCharging", SpearRules.OffhandCharging.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.Lunge> LUNGE = BUILDER
            .comment("突进（戳刺时向前冲）的力度与饥饿门槛。",
                    "JAVA（默认）：每级 0.458 冲量，饥饿值 <6 时无法使用，与原版一致。",
                    "MODDED：每级 0.65 冲量，饥饿值 ≤7 时无法使用，本模组原本的行为。")
            .defineEnum("lunge", SpearRules.Lunge.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.HitSlowDown> HIT_SLOW_DOWN = BUILDER
            .comment("命中目标时给攻击者自己减速（水平速度 ×0.6）并取消奔跑。",
                    "JAVA（默认）：有，也就是原版那种撞完得重新起跑的手感。",
                    "MODDED：没有，撞完保持速度与奔跑状态，接着冲。")
            .defineEnum("hitSlowDown", SpearRules.HitSlowDown.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.MobSpearEquipTable> MOB_SPEAR_EQUIP_TABLE = BUILDER
            .comment("怪物携带长矛的名单与材质。",
                    "JAVA（默认）：与原版 Java 版一致——僵尸/尸壳/僵尸村民拿铁矛，猪灵/僵尸猪灵拿金矛，携带概率约 5%。",
                    "MODDED：本模组原本的概率表（卫道士 15%、凋灵骷髅 12% 等）并随机材质。")
            .defineEnum("mobSpearEquipTable", SpearRules.MobSpearEquipTable.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.MobSpearDropChance> MOB_SPEAR_DROP_CHANCE = BUILDER
            .comment("怪物手里那根矛的掉落概率。",
                    "JAVA（默认）：8.5%，抢夺每级 +1%（抢夺 III 时 11.5%），与原版一致。",
                    "MODDED：固定 15%，本模组原本的数值。")
            .defineEnum("mobSpearDropChance", SpearRules.MobSpearDropChance.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.MinAttackRange> MIN_ATTACK_RANGE = BUILDER
            .comment("长矛的最小攻击距离（判定射线的起点）。",
                    "JAVA（默认）：2 格，与原版一致——贴脸的目标反而打不到，得先退开一点。",
                    "MODDED：0 格，任意近的距离都能戳刺、也能起手蓄力。")
            .defineEnum("minAttackRange", SpearRules.MinAttackRange.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.ChargeStrength> CHARGE_STRENGTH = BUILDER
            .comment("蓄力伤害的基线是否吃攻击力修饰符（力量/虚弱药水、其他攻击力加成）。",
                    "JAVA（默认）：不吃，与原版 Java 版一致——蓄力伤害取武器的裸攻击力（MC-302778）。",
                    "BEDROCK：吃，与原版基岩版一致——力量药水等会加进蓄力伤害。")
            .defineEnum("chargeStrength", SpearRules.ChargeStrength.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.JabToChargeDelay> JAB_TO_CHARGE_DELAY = BUILDER
            .comment("戳刺命中后，要等多久才能重新举矛蓄力。",
                    "JAVA（默认）：0 tick，与原版 Java 版一致——戳完立刻能蓄力。",
                    "BEDROCK：等该长矛的使用冷却结束（木 0.65s … 下界合金 1.15s），与原版基岩版一致。",
                    "          原版只写了\"若干 tick\"（MCPE-233837），这里按该矛的使用冷却近似。")
            .defineEnum("jabToChargeDelay", SpearRules.JabToChargeDelay.JAVA);

    public static final ForgeConfigSpec.EnumValue<SpearRules.MovingTargetAssist> MOVING_TARGET_ASSIST = BUILDER
            .comment("打移动目标时的延迟补偿。",
                    "JAVA（默认）：不补偿，与原版一致——服务器按目标的真实位置判定，打移动靶要自己留提前量（MC-310857）。",
                    "MODDED：按玩家屏幕上看到的位置判定，把目标判定箱按最近 3 tick 的位移往回挪，打移动靶不用预判。")
            .defineEnum("movingTargetAssist", SpearRules.MovingTargetAssist.JAVA);

    static {
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
