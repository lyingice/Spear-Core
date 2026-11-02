# Spear Core · 矛核心

> **中文版在下半部分，English version below.**  [中文版](#中文版) ｜ [English version](#english-version)
>
> 给 Minecraft 加了一套长矛：能蓄力冲锋、能戳刺、跑得越快伤害越高，怪物也会拿着它追着你捅。
> A set of **spears** for Minecraft: charge, stab, hit harder the faster you run — and mobs will chase you with one too.

> 适用版本：**Minecraft 1.20.1 · Forge**（客户端与服务端都要装） / **Minecraft 1.20.1 · Forge**, client and server both.
>
> 本分支 = 1.20.1 + Forge；1.21.1 + NeoForge 是 `main` 分支，两边功能一致。

---

# 中文版


## 这个 mod 加了什么

- **七把矛**：木、石、铜、铁、金、钻石、下界合金，数值各不相同。
- **两种打法**：左键**戳刺**，右键按住**蓄力冲锋**。
- **速度就是伤害**：朝目标冲得越快，打出来越高。
- **母语附魔**：突进、锋利、亡灵杀手等都能正常上。
- **怪物会用矛**：僵尸、骷髅、卫道士等有一定概率拿着矛生成，而且真的会冲锋。
- **自带动作与音效**：第一/第三人称持矛姿势、蓄力动作、命中反馈。

---

## 安装

1. 游戏版本要 **1.20.1**，加载器 **Forge**（47.3.0 及以上）。
2. 把 jar 放进 `mods` 文件夹。
3. **客户端和服务端都要放**。只装服务端的话，联机时别人看到的是空手。
4. 单人游戏不用管，直接开。

---

## 七把矛

下表是**空手拿着它时的显示数值**（附魔和属性加成另算）：

| 矛 | 攻击力 | 攻击速度 | 耐久 | 附魔能力 | 备注 |
|---|---|---|---|---|---|
| 木矛 | 1 | 1.5 | 59 | 15 | 起手最快 |
| 石矛 | 2 | 1.3 | 131 | 5 | |
| 铜矛 | 2 | 1.2 | 195 | 13 | |
| 铁矛 | 3 | 1.1 | 250 | 14 | 均衡，也是默认手感基准 |
| 金矛 | 1 | 1.1 | 59 | 22 | 好附魔，不耐用 |
| 钻石矛 | 4 | 1.0 | 1561 | 10 | |
| 下界合金矛 | 5 | 0.9 | 2031 | 15 | 防火、不掉岩浆 |

**攻击速度越小越慢**：木矛戳得最勤，下界合金矛一下最疼但间隔最长。

---

## 合成

六把普通矛都在**工作台**里，形状是"斜着的一根材料 + 两根木棍"：

```
木矛      = 任意木板 x1 + 木棍 x2
石矛      = 圆石   x1 + 木棍 x2
铜矛      = 铜锭   x1 + 木棍 x2
铁矛      = 铁锭   x1 + 木棍 x2
金矛      = 金锭   x1 + 木棍 x2
钻石矛    = 钻石   x1 + 木棍 x2
```

摆法（`a` 是材料，`b` 是木棍，空格不摆东西）：

```
[ ][ ][a]
[ ][b][ ]
[b][ ][ ]
```

**下界合金矛**走原版那套：锻造台里放 `下界合金升级模板 + 钻石矛 + 下界合金锭`。

装了 JEI 的话，游戏里直接搜"矛"就能看到全部配方。

---

## 怎么用

### 左键：戳刺

拿着矛**左键**（就是平时攻击那个键）会捅一下。有两个条件：

- **攻击冷却条要满**——和斧头一样，挥完要等一会儿，没满的时候戳不动。
- 戳中之后，同一只怪在很短时间内不会被你连续戳到，防止一次冲锋打出一串伤害。

戳刺伤害 = **武器攻击力 + 你朝前冲的速度加成**，所以站着不动戳是最低的。

> 注：MC 26.1.2 原版也加入了长矛。原版**穿刺是纯属性伤害、只有蓄力吃速度加成**，
> 所以"穿刺也吃速度"是本模组与原版的一处设计差异；**蓄力那一整套数值两边完全一致**。

### 右键按住：蓄力冲锋

按住**右键**开始蓄力，矛会依次进入几个阶段：

| 阶段 | 能做什么 |
|---|---|
| 起手 | 抬矛准备，还没效果 |
| 击落窗口 | 可以**把骑在坐骑上的敌人捅下来** |
| 击退窗口 | 可以**把撞到的目标击飞** |
| 伤害窗口 | 可以**造成伤害**（这是主要输出窗口） |

蓄到最长时间（不同材质约 6～12 秒）会**自动收手**，不用一直按着。

**关键：蓄力时不会像吃东西那样被减速**，你可以正常走动、还能按住疾跑键全力冲刺——这正是它的玩法。冲进人堆里按住右键，边跑边撞。

### 为什么"跑得越快伤害越高"

系统看的是**你朝面向方向的速度**，以及**你和目标之间的相对速度**。所以：

- 站着不动 → 伤害很低，甚至触发不了击退/伤害窗口。
- 疾跑冲过去 → 伤害明显更高。
- 迎面撞上正在朝你走来的怪 → 相对速度最大，伤害最高。
- 追着同方向逃跑的怪捅 → 相对速度小，伤害低。

### 击退与击落骑手

- **击退**：把目标打飞一段距离，需要你冲得够快。
- **击落骑手**：把马上/船上的敌人直接捅下来，是最早就能触发的效果，适合对付劫掠时的掠夺者骑兵。

---

## 突进附魔（Lunge）

新的**突进**附魔，最高 3 级，只能附在矛上：

- **戳刺时把自己向前推一段**，级别越高推得越远。
- 代价：每戳一次扣 **1 点耐久**，并按级别**消耗饱食度**（3 级很费）。
- 饥饿值低于 6 时**不会触发**（默认 Java 版规则；`behavior.lunge = MODDED` 时饥饿值 ≤7 就不触发）。
- 骑乘时、鞘翅飞行时、在水里时**不会触发**。

配合"移动越快伤害越高"的机制，突进能让你自己把速度加上去，是矛的核心附魔。

---

## 怪物也会用矛

怪物携带长矛的名单、概率与掉落率都由配置项控制，**默认 `JAVA`**，也就是和原版一致：

| 生物 | 携带 | 概率 |
|---|---|---|
| 僵尸 / 尸壳 / 僵尸村民 | 铁矛 | 约 5% |
| 猪灵 / 僵尸猪灵 | 金矛 | 约 5% |

原版没公布精确概率，这里取社区常用的 5% 近似值；僵尸马与骆驼尸壳的骑手本身就是僵尸，所以骑手也一并覆盖。

切到 `behavior.mobSpearEquipTable = MODDED` 则恢复本模组原本的概率表：

| 生物 | 概率 |
|---|---|
| 卫道士 | 15% |
| 凋灵骷髅 | 12% |
| 尸壳 | 10% |
| 僵尸 / 僵尸村民 / 掠夺者 | 8% |
| 溺尸 | 6% |
| 骷髅 / 流浪者 | 5% |
| 猪灵 / 猪灵蛮兵 / 僵尸猪灵 | 固定拿金矛 |

手里那根矛的掉落率由 `behavior.mobSpearDropChance` 决定：`JAVA`（默认）= **8.5%**，抢夺每级 +1%（抢夺 III 时 11.5%）；`MODDED` = 固定 15%。

所以别以为只有你会冲锋——**卫道士拿着矛冲过来是真的疼**（MODDED 模式下）。

---

## 配置

配置文件在 `config/spearcore-common.toml`。

### 总开关

```toml
# 是否启用原版材质长矛（木、石、铁、金、钻石、下界合金）
enableVanillaSpears = true

# 是否启用铜矛
enableCopperSpear = true

# 安装 Punchy 时，让其矛蓄力动画按本模组各材质的阶段切换
punchyStageBridge = true
```

前两个开关控制的是（**默认都开着**）：

- 这些矛**会不会出现在创造模式物品栏**；
- 怪物**会不会携带**这些矛。

**关掉也不影响**合成、`/give` 和其他正常获取途径——物品一直在，只是不在创造栏里、怪物也不带。

### 行为开关（`[behavior]`）

每个细节一个独立配置项，**默认一律是 `JAVA`**（= 原版 Java 版行为）。可选值只列出"行为确实不同"的版本，与 Java 一致的版本不会重复出现。

```toml
[behavior]
jabDamage = "JAVA"
jabSpeedBonus = "JAVA"
jabCooldown = "JAVA"
chargeHitboxMargin = "JAVA"
chargeRepeatHit = "JAVA"
jabRepeatHit = "JAVA"
durability = "JAVA"
creativeMaxRange = "JAVA"
offhandCharging = "JAVA"
lunge = "JAVA"
hitSlowDown = "JAVA"
mobSpearEquipTable = "JAVA"
mobSpearDropChance = "JAVA"
minAttackRange = "JAVA"
chargeStrength = "JAVA"
jabToChargeDelay = "JAVA"
movingTargetAssist = "JAVA"
```

| 配置项 | 可选值（第一个是默认） | 说明 |
|---|---|---|
| `jabDamage` | `JAVA` / `BEDROCK` | 戳刺伤害。JAVA：木/金 1、石/铜 2、铁 3、钻 4、下界合金 5；BEDROCK：每级 +1 点。 |
| `jabSpeedBonus` | `JAVA` / `MODDED` | 戳刺是否额外加 `floor(攻速 × 倍率)`。JAVA/基岩版没有；MODDED 才有（疾跑冲锋更能打）。 |
| `jabCooldown` | `JAVA` / `BEDROCK` | JAVA：等原版攻击蓄力条回满；BEDROCK：改用物的使用冷却，且**背包里所有长矛共享**。 |
| `chargeHitboxMargin` | `JAVA` / `MODDED` | 蓄力判定箱额外膨胀：JAVA 0.125，MODDED 0.25。 |
| `chargeRepeatHit` | `JAVA` / `BEDROCK` | 蓄力重复命中：JAVA 每个目标每次蓄力只结算一次；BEDROCK 同目标 10 tick（0.5s）一次。 |
| `jabRepeatHit` | `JAVA` / `MODDED` | 戳刺重复命中：JAVA 没有额外冷却；MODDED 同目标 10 tick 内跳过。 |
| `durability` | `JAVA` / `BEDROCK` / `MODDED` | 耐久。JAVA：59/131/190/250/32/1561/2031；BEDROCK：每级 +1；MODDED：本模组原表（铜 195、金 59）。 |
| `creativeMaxRange` | `JAVA` / `BEDROCK` | 创造模式攻击距离上限：JAVA 6.5，BEDROCK 7.5。 |
| `offhandCharging` | `JAVA` / `BEDROCK` | 能否用副手举矛蓄力。JAVA 可以；BEDROCK 不行。 |
| `lunge` | `JAVA` / `MODDED` | 突进力度与饥饿门槛。JAVA：0.458 / 饥饿 <6 禁用；MODDED：0.65 / 饥饿 ≤7 禁用。 |
| `hitSlowDown` | `JAVA` / `MODDED` | 命中后攻击者自己水平速度 ×0.6 并取消奔跑。JAVA 有（原版手感）；MODDED 没有。 |
| `mobSpearEquipTable` | `JAVA` / `MODDED` | 怪物携带名单：JAVA 原版名单（铁/金矛），MODDED 本模组概率表 + 随机材质。 |
| `mobSpearDropChance` | `JAVA` / `MODDED` | 怪物矛掉落率：JAVA 8.5%（抢夺每级 +1%），MODDED 固定 15%。 |
| `minAttackRange` | `JAVA` / `MODDED` | 最小攻击距离：JAVA 2 格（怪物按 mobFactor 减半）；MODDED 归零，贴脸也能出手。 |
| `chargeStrength` | `JAVA` / `BEDROCK` | 蓄力基线是否吃攻击力修饰符（力量/虚弱药水）。JAVA 不吃（原版 [MC-302778](https://bugs.mojang.com/browse/MC-302778)）；BEDROCK 吃。 |
| `jabToChargeDelay` | `JAVA` / `BEDROCK` | 戳刺命中后重新举矛蓄力的间隔：JAVA 0 tick；BEDROCK 该矛的使用冷却（木 13、铁 19、下界合金 23 tick）。 |
| `movingTargetAssist` | `JAVA` / `MODDED` | 移动靶补偿（[MC-310857](https://bugs.mojang.com/browse/MC-310857)）：JAVA 按服务器真实位置判定；MODDED 按客户端看到的位置回退 3 tick。 |

另有两条**不提供配置项**的固定规则：

- **击落骑手所需的速度阈值写死为 Java 版数据**（木 14 / 金 13 / 石 13 / 铜 12 / 铁 11 / 钻 10 / 下界合金 9 格每秒）——这是原版 Java 版唯一可信的一套数值，基岩版对齐后与此相同。
- 判定层差异凡能复刻的都做成了开关（例如 `chargeRepeatHit` 对应基岩版的 0.5 秒重连间隔）；**引擎/渲染层**差异不在复刻范围（基岩版火焰附加点燃蜡烛/营火/TNT、蓄力暴击粒子）。

`jabDamage = "BEDROCK"` 时，多出来的 1 点伤害加在攻击力属性上，因此**属性面板与戳刺会一起 +1**；蓄力是否也 +1 取决于 `chargeStrength`（`BEDROCK` 时蓄力基线也吃攻击力修饰符）。

## 与原版的差异

以下几处是本模组有意与 Java 原版不同（能做成开关的都已在上一节列出），其余行为默认按原版还原：

- **蓄力伤害基线**：原版是固定 1 点（空手伤害）加相对速度倍数；本模组始终以武器攻击力为基线，`chargeStrength` 只决定力量/虚弱这类修饰符算不算。
- **戳刺速度加成**：原版戳刺只看武器攻击力，不随移动速度增加；本模组默认不加（`jabSpeedBonus = "MODDED"` 才启用）。
- **同目标接触冷却**：原版一次蓄力对同一目标只结算一次，戳刺也没有额外冷却；本模组默认就是原版行为，`chargeRepeatHit` / `jabRepeatHit` 的 `MODDED` 档才加 10 tick 冷却。
- **最小攻击距离归零**：原版 2 格内打不到；`minAttackRange = "MODDED"` 时归零，可以贴脸捅。
- **戳刺后摇**：原版 Java 版戳完能立刻再举矛；`jabToChargeDelay = "BEDROCK"` 时按该矛的使用冷却补上这段后摇。
- **移动靶补偿**：原版按服务器位置判定（打移动靶要留提前量）；`movingTargetAssist = "MODDED"` 时按客户端看到的位置回退 3 tick。
- **怪物携带长矛**：原版只有僵尸/尸壳/僵尸村民/僵尸马骑手带铁矛、猪灵/僵尸猪灵带金矛；本模组默认走原版名单，概率表与随机材质属于 `MODDED` 档（`mobSpearEquipTable`、`mobSpearDropChance`）。

---

## 常见问题

**Q：为什么矛的攻击力比剑低？**
因为它的伤害主要来自**速度加成**。站着砍确实不如剑，但疾跑冲锋戳一下可以远超剑。

**Q：左键没反应 / 戳不动？**
攻击冷却条没满（看准星下方那个条），或者目标刚被你戳过还在免疫时间内。

**Q：创造模式物品栏里搜不到矛？**
默认配置是关闭的，见上面"配置"一节。或者在创造搜索栏里直接搜物品 ID，`give` 也能拿到。

**Q：能附锋利吗？**
能，矛按普通武器规则吃附魔。突进是它专属的。

**Q：支持别的版本吗？**
这个分支是 **1.20.1 + Forge**；**1.21.1 + NeoForge** 在 `main` 分支上，两者玩法一致。

**Q：和别的 mod 冲突吗？**
**JEI**（查配方）、**KubeJS**（用脚本加自己的矛）与 **Punchy**（第一人称视角动画）都已支持。

---

## 许可证

MIT License

---

# 给模组 / 数据包作者

想在别的 mod、数据包或 KubeJS 脚本里接入长矛，下面是全部公开接口。

## 物品与标签

| 内容 | ID |
|---|---|
| 内建矛 | `spearcore:wooden_spear`、`stone_spear`、`copper_spear`、`iron_spear`、`golden_spear`、`diamond_spear`、`netherite_spear` |
| 六把原版材质矛 | `#spearcore:spears` |
| 全部七把矛 | `#spearcore:spear` |
| 原版矛标签 | `#minecraft:spears` |
| 近战武器归类 | `#minecraft:swords`（自动连带进入 `#minecraft:tools` 与 `#forge:tools`） |
| 猪灵喜爱 | `#minecraft:piglin_loved`（仅金矛） |
| 伤害类型 | `spearcore:spear` |
| 突进附魔 | `spearcore:lunge`（`supported_items` 即 `#spearcore:spear`，最高 3 级） |
| 玩家属性 | `spearcore:spear_stab_multiplier`（戳刺伤害倍率，默认 1.0）、`spearcore:spear_charge_multiplier`（冲锋伤害倍率，默认 1.0） |

## 类继承

```
net.minecraft.world.item.Item
└── SpearItem                 (abstract) 定义全部矛参数（攻击距离、阶段、判定箱、音效……）
    └── BaseSpearItem         (abstract) Stats 驱动，内建七把矛都是它的内部子类
        └── ConfiguredSpearItem          通用具体实现，构造即注册（KubeJS 与数据驱动路径使用）
```

- `SpearItem`：抽象方法全是"这个矛长什么样、怎么打"，客户端 mixin、网络包、怪物 AI 都按 `instanceof SpearItem` 判定。
- `BaseSpearItem`：两个构造 —— `(SpearStats.Stats)` 用 Stats 自动生成物品属性；`(SpearStats.Stats, Item.Properties)` 由调用方完全接管物品属性。
- `ConfiguredSpearItem`：不绑定任何材质的成品类，`new ConfiguredSpearItem(stats)` 即可；**不引用任何第三方 mod 的类**，可安全用于可选联动。

### 定义一把自己的矛（Java）

```java
public class MySpearItem extends ConfiguredSpearItem {
    public MySpearItem() {
        super(SpearStats.Stats.of(
            /* durability */ 300, /* attackDuration(秒) */ 0.9f, /* damageMultiplier */ 1.0f,
            /* attackDamageBonus */ 3.0f, /* enchantmentValue */ 15,
            /* rarity */ Rarity.UNCOMMON, /* repairIngredient */ Ingredient.of(Items.COPPER_INGOT),
            /* fireResistant */ false, /* materialName */ "my_spear",
            /* use/hit/attack 音效 */ SpearSounds.ITEM_SPEAR_USE.get(), SpearSounds.ITEM_SPEAR_HIT.get(), SpearSounds.ITEM_SPEAR_ATTACK.get(),
            /* swingTimes */ 0.9f, /* hitboxMargin */ 0.25f, /* contactCooldownTicks */ 10,
            /* delayTicks */ 12,
            /* 三个阶段条件 */ SpearCondition.ofAttackerSpeed(50, 0.3F),
                                  SpearCondition.ofAttackerSpeed(135, 5.1F),
                                  SpearCondition.ofRelativeSpeed(225, 4.6F),
            /* forwardMovement */ 0.38f, /* minRange */ 2.0f, /* maxRange */ 4.5f,
            /* 创造模式距离 */ 2.0f, 6.5f,
            /* hitboxMargin2 */ 0.25f, /* mobFactor */ 0.5f,
            /* dealsKnockback */ true, /* dismounts */ true));
    }
}
```

数值懒得填可以用 `SpearStats.spearOf(...)` 走简化工厂，它会按 `attackDuration` 自动算好各阶段参数；内建预设直接读 `SpearStats.WOOD / STONE / COPPER / IRON / GOLD / DIAMOND / NETHERITE`。

> 注意：注册必须发生在**物品注册事件**期间（构造时会把 `Item → Stats` 记进 `SpearStats` 的表）。要用延迟注册器的话，确保 `SpearStats` 初始化时音效注册表已就绪。

## 事件

全部在 Forge 的 `MinecraftForge.EVENT_BUS`（游戏总线）上：

| 事件 | 时机 | 能改什么 |
|---|---|---|
| `SpearDamageEvent` | 每次结算伤害前（戳刺与冲锋各一次） | `setMultiplier(`…`)` / `addMultiplier(`…`)` 调整最终伤害；`getAttackType()` 区分 `STAB` / `CHARGE` |
| `SpearHitEvent` | 命中且伤害已经打完 | 只读：吸血、点燃、粒子、计数、成就 |
| `SpearChargePhaseEvent` | 蓄力阶段切换的瞬间（只发一次） | 只读：`DELAY / DISMOUNT / KNOCKBACK / DAMAGE`，用于自定义动画/音效/粒子 |
| `SpearMobEquipEvent` | 怪物生成准备发矛时 | **可取消**；也能换矛、改掉落率 |

## 扩展点

- `UseSpearSpecialEntity`：让**非玩家实体**（自定义生物）接入矛的速度判定，可覆盖 `getJerotesSpearNeedSpeed()`、`getJerotesSpearDamageMultiple()`、`isJerotesSpearGetMotionLikePlayer()` 等。
- `KnownMovementAccessor` / `SpearCooldownAccessor`：通过 mixin 注入到实体上的接口，用于读写"已知移动向量"与"刚被谁戳过"的免疫记录。
- 自定义音效可以复用 `SpearSounds` 里的条目，也可以自己注册 `SoundEvent` 传给 Stats。

## KubeJS（已支持）

脚本里可以直接注册真正的矛，写法与 1.21.1 分支完全一致：

```js
StartupEvents.registry('item', event => {
    event.create('mypack:bronze_spear', 'spearcore:spear')
        .displayName('青铜矛')
        .durability(400)
        .attackDamageBonus(3)
        .texture('mypack:item/bronze_spear')
})
```

完整教程（参数表、贴图规范、常见坑，中英双语）：**[kjs-guide.md](kjs-guide.md)**。

**类加载隔离**：整个项目只有 `compat/kubejs` 包引用 `dev.latvian.mods.*`，入口是 `kubejs.plugins.txt` 里的类名字符串 + `Class.forName`。
没装 KubeJS 时那个包永远不会被加载（已实测：无 KubeJS 启动零 ClassNotFound、零 Exception）。

**对应版本**：KubeJS **2001.6.5-build.26+forge**（1.20.1 Forge）。

## Punchy（已支持）

装了 Punchy 时，矛的蓄力动画会**跟着本模组各材质的时间轴走** ——
举矛段(delay+dismount) → 击退段 → 仅伤害段，而不是用 Punchy 自带的固定时间轴。
不喜欢可以用配置项 `punchyStageBridge = false` 关掉，回到 Punchy 自己的节奏。

对应版本：Punchy **2.8d**（1.20.1 Forge）。没装 Punchy 时整条链路自动跳过：
桥接的 mixin 配置是 `required=false`，并额外由 `PunchyMixinPlugin` 在启动早期判定是否应用
（实测：无 Punchy 时零 ClassNotFound、零异常）。

## 1.20.1 移植说明

从 1.21.1 / NeoForge 移植到 1.20.1 / Forge 时，下面这些 API 是对不上的（都已按 1.20.1 改好）：

| 1.21.1 / NeoForge | 1.20.1 / Forge |
|---|---|
| `CustomPacketPayload` + `StreamCodec` | `SimpleImpl` 的 `SimpleChannel` |
| `mods.toml` 里的 `[[mixins]]` | `build.gradle` 的 `mixin { config ... }` → MANIFEST 的 `MixinConfigs` |
| `Attributes.ENTITY_INTERACTION_RANGE` | `ForgeMod.ENTITY_REACH` |
| `getDefaultAttributeModifiers(ItemStack)` + `ItemAttributeModifiers` | `getDefaultAttributeModifiers(EquipmentSlot)` + `Multimap` |
| `Operation.ADD_VALUE`，修饰符 ID 用 `ResourceLocation` | `Operation.ADDITION`，ID 用 `UUID` |
| 附魔是数据包 JSON | 附魔是注册表对象：`LungeEnchantment` + `EnchantmentCategory.create` |
| `Item#postHurtEnemy` | `Item#hurtEnemy`（1.20.1 的耐久扣减在这里） |
| `ICancellableEvent` | `@Cancelable` |
| `ResourceLocation.fromNamespaceAndPath(ns, path)` | `new ResourceLocation(ns, path)` |

### 自己构建与验证

```bash
./gradlew build              # 产物：build/libs/spearcore-1.20.1-forge-3.2.0.jar
./gradlew runServer          # 无头启动；启动自检会打印"已注册 7 把长矛"
./gradlew runGameTestServer  # 由游戏本体判定的自动化测试
```

GameTest 的判定看日志：`All N required tests passed :)` 或 `N required tests failed :(`，
退出码等于失败的必需测试数。测试结构模板在 `gameteststructures/`，Gradle 会拷进 `run/`。

---

# English version

## What this mod adds

- **Seven spears**: wood, stone, copper, iron, golden, diamond and netherite, each with its own stats.
- **Two ways to fight**: left-click to **stab**, hold right-click to **charge**.
- **Speed is damage**: the faster you run at a target, the harder it hits.
- **Enchantments work normally**: Lunge, Sharpness, Smite and the rest.
- **Mobs use spears too**: zombies, skeletons and vindicators may spawn holding one — and they really do charge.
- **Animations and sounds included**: first- and third-person spear poses, a charge-up animation, hit feedback.

---

## Installation

1. **Minecraft 1.20.1** with **Forge** (47.3.0 or newer).
2. Drop the jar into your `mods` folder.
3. **Install it on both the client and the server.** Server-only means other players see an empty hand.
4. Single-player needs nothing else.

---

## The seven spears

These are the values shown for a bare item (enchantments and attribute modifiers not included):

| Spear | Attack damage | Attack speed | Durability | Enchantability | Notes |
|---|---|---|---|---|---|
| Wooden spear | 1 | 1.5 | 59 | 15 | fastest to raise |
| Stone spear | 2 | 1.3 | 131 | 5 | |
| Copper spear | 2 | 1.2 | 195 | 13 | |
| Iron spear | 3 | 1.1 | 250 | 14 | balanced; the baseline feel |
| Golden spear | 1 | 1.1 | 59 | 22 | enchants well, fragile |
| Diamond spear | 4 | 1.0 | 1561 | 10 | |
| Netherite spear | 5 | 0.9 | 2031 | 15 | fireproof, survives lava |

**A lower attack speed number means slower attacks**: the wooden spear stabs most often, the netherite spear hits hardest but with the longest gap between hits.

---

## Crafting

All six ordinary spears are made at a **crafting table**, shaped as one diagonal line of material plus two sticks:

```
Wooden spear   = any planks x1   + stick x2
Stone spear    = cobblestone x1  + stick x2
Copper spear   = copper ingot x1 + stick x2
Iron spear     = iron ingot x1   + stick x2
Golden spear   = gold ingot x1   + stick x2
Diamond spear  = diamond x1      + stick x2
```

Layout (`a` is the material, `b` is a stick, leave the rest empty):

```
[ ][ ][a]
[ ][b][ ]
[b][ ][ ]
```

The **netherite spear** uses the vanilla route: a smithing table with a `netherite upgrade template + diamond spear + netherite ingot`.

With JEI installed, just search for "spear" in game to see every recipe.

---

## How to use it

### Left click: stab

**Left click** with a spear in hand (your normal attack key) performs a stab. Two conditions apply:

- **The attack cooldown bar must be full** — like an axe, you have to wait after each hit, and a partial bar will not stab.
- After a hit, the same mob cannot be stabbed again for a short while, so a single charge cannot deal a burst of damage.

Stab damage = **weapon attack damage + a bonus from your forward speed**, so standing still and poking is the weakest option.

> Note: MC 26.1.2 added spears to vanilla. Vanilla's stab is pure attribute damage and only the charge
> scales with speed, so "stab also scales with speed" is a deliberate difference from vanilla. The charge
> math itself is identical in both.

### Hold right click: charge

Holding **right click** starts charging, and the spear moves through several stages:

| Stage | What it enables |
|---|---|
| Ready | spear raised; no effect yet |
| Dismount window | **knock a rider off their mount** |
| Knockback window | **send the target flying** |
| Damage window | **deal damage** (the main damage window) |

Charging to its maximum (roughly 6–12 seconds depending on the material) **ends automatically**; you do not have to hold the button the whole time.

**Crucially, charging does not slow you down** the way eating does. You can walk normally and even hold sprint at full speed — that is the whole point. Charge into a crowd with right click held and keep running.

### Why "the faster you run, the harder you hit"

The system looks at **your speed along the direction you are facing**, and at the **relative speed between you and the target**. So:

- Standing still → very low damage, and the knockback/damage windows may not even trigger.
- Sprinting into the target → noticeably more damage.
- Meeting a mob head-on that is walking towards you → the highest relative speed, so the most damage.
- Chasing a mob that is running away in the same direction → low relative speed, low damage.

### Knockback and dismounting

- **Knockback**: sends the target flying; you need to be moving fast enough.
- **Dismount**: pulls an enemy straight off a horse or boat. It is the earliest effect you can trigger, which makes it ideal against pillager cavalry during a raid.

---

## The Lunge enchantment

A new **Lunge** enchantment, up to level 3, applicable only to spears:

- **A stab pushes you forward**, further at higher levels.
- Cost: **1 durability** per stab, plus **hunger** that scales with the level (level 3 is expensive).
- It does **not** trigger when your hunger is below 6 (the JAVA rule; with `behavior.lunge = MODDED` it is blocked at hunger ≤7).
- It does **not** trigger while riding, elytra flying, or in water.

Combined with the "faster means more damage" rule, Lunge lets you build up your own speed — it is the spear's signature enchantment.

---

## Mobs use spears too

Who carries a spear, how often, and how often it drops are all controlled by config options. The default is `JAVA`, i.e. vanilla behaviour:

| Mob | Carries | Chance |
|---|---|---|
| Zombie / husk / zombie villager | iron spear | ~5% |
| Piglin / zombified piglin | golden spear | ~5% |

Vanilla never published the exact chance, so 5% is the commonly cited approximation. Zombie horsemen and camel husk jockeys are ordinary zombies riding a mount, so their riders are covered too.

With `behavior.mobSpearEquipTable = MODDED` you get this mod's original table instead:

| Mob | Chance |
|---|---|
| Vindicator | 15% |
| Wither skeleton | 12% |
| Husk | 10% |
| Zombie / zombie villager / pillager | 8% |
| Drowned | 6% |
| Skeleton / stray | 5% |
| Piglin / piglin brute / zombified piglin | always, with a golden spear |

The drop chance of that spear is controlled by `behavior.mobSpearDropChance`: `JAVA` (default) = **8.5%**, plus 1% per Looting level (11.5% at Looting III); `MODDED` = fixed 15%.

So do not assume you are the only one who can charge — **in MODDED mode, a vindicator charging with a spear genuinely hurts**.

---

## Configuration

The config file is `config/spearcore-common.toml`.

### Master switches

```toml
# Whether the vanilla-material spears are enabled (wood, stone, iron, gold, diamond, netherite)
enableVanillaSpears = true

# Whether the copper spear is enabled
enableCopperSpear = true

# When Punchy is installed, drive its spear charge animation from this mod's per-material stages
punchyStageBridge = true
```

The first two switches (both **on by default**) control:

- whether these spears **appear in the creative inventory**;
- whether mobs **spawn carrying** them.

**Turning them off changes nothing else**: crafting, `/give` and every normal way of obtaining them still work. The items always exist — they are simply hidden from the creative tabs and never handed to mobs.

### Behaviour switches (`[behavior]`)

One option per detail, **all defaulting to `JAVA`** (= vanilla Java Edition behaviour). Each option only lists the versions whose behaviour actually differs; a version that matches Java is never offered as a duplicate.

```toml
[behavior]
jabDamage = "JAVA"
jabSpeedBonus = "JAVA"
jabCooldown = "JAVA"
chargeHitboxMargin = "JAVA"
chargeRepeatHit = "JAVA"
jabRepeatHit = "JAVA"
durability = "JAVA"
creativeMaxRange = "JAVA"
offhandCharging = "JAVA"
lunge = "JAVA"
hitSlowDown = "JAVA"
mobSpearEquipTable = "JAVA"
mobSpearDropChance = "JAVA"
minAttackRange = "JAVA"
chargeStrength = "JAVA"
jabToChargeDelay = "JAVA"
movingTargetAssist = "JAVA"
```

| Option | Values (first is default) | Meaning |
|---|---|---|
| `jabDamage` | `JAVA` / `BEDROCK` | Stab damage. JAVA: wood/gold 1, stone/copper 2, iron 3, diamond 4, netherite 5; BEDROCK: +1 per tier. |
| `jabSpeedBonus` | `JAVA` / `MODDED` | Whether a stab adds `floor(speed × multiplier)`. Vanilla (both editions) does not; MODDED does. |
| `jabCooldown` | `JAVA` / `BEDROCK` | JAVA: wait for the vanilla attack cooldown bar; BEDROCK: use the item's use-cooldown, **shared by every spear in the inventory**. |
| `chargeHitboxMargin` | `JAVA` / `MODDED` | Extra hitbox inflation while charging: JAVA 0.125, MODDED 0.25. |
| `chargeRepeatHit` | `JAVA` / `BEDROCK` | Re-hitting during a charge: JAVA once per target per charge; BEDROCK once per 10 ticks (0.5s) per target. |
| `jabRepeatHit` | `JAVA` / `MODDED` | Re-stabbing: JAVA no extra cooldown; MODDED skips a target for 10 ticks. |
| `durability` | `JAVA` / `BEDROCK` / `MODDED` | Durability. JAVA: 59/131/190/250/32/1561/2031; BEDROCK: +1 per tier; MODDED: this mod's old table (copper 195, gold 59). |
| `creativeMaxRange` | `JAVA` / `BEDROCK` | Creative attack range cap: JAVA 6.5, BEDROCK 7.5. |
| `offhandCharging` | `JAVA` / `BEDROCK` | Charging from the off hand. JAVA allows it; BEDROCK does not. |
| `lunge` | `JAVA` / `MODDED` | Lunge strength and hunger gate. JAVA: 0.458 / blocked below hunger 6; MODDED: 0.65 / blocked at hunger ≤7. |
| `hitSlowDown` | `JAVA` / `MODDED` | On hit, cut the attacker's own horizontal speed to 60% and cancel sprinting. JAVA has it; MODDED does not. |
| `mobSpearEquipTable` | `JAVA` / `MODDED` | Mob loadout: JAVA vanilla list (iron/golden spears), MODDED this mod's chances + random materials. |
| `mobSpearDropChance` | `JAVA` / `MODDED` | Mob spear drop chance: JAVA 8.5% (+1% per Looting level), MODDED fixed 15%. |
| `minAttackRange` | `JAVA` / `MODDED` | Minimum attack range: JAVA 2 blocks (halved for mobs); MODDED 0 — you can poke at point-blank range. |
| `chargeStrength` | `JAVA` / `BEDROCK` | Whether the charge baseline takes attack-damage modifiers (Strength/Weakness). JAVA does not ([MC-302778](https://bugs.mojang.com/browse/MC-302778)); BEDROCK does. |
| `jabToChargeDelay` | `JAVA` / `BEDROCK` | Delay before you can start another charge after a jab: JAVA 0 ticks; BEDROCK the spear's use-cooldown (wood 13, iron 19, netherite 23 ticks). |
| `movingTargetAssist` | `JAVA` / `MODDED` | Moving-target compensation ([MC-310857](https://bugs.mojang.com/browse/MC-310857)): JAVA judges at the server-side position; MODDED rewinds the hitbox 3 ticks to what the client sees. |

Two rules are **deliberately not configurable**:

- **The rider-dismount speed thresholds are hard-wired to the Java Edition data** (wood 14 / gold 13 / stone 13 / copper 12 / iron 11 / diamond 10 / netherite 9 blocks per second) — that is the only trustworthy vanilla set, and Bedrock was aligned to it.
- Every judgable version difference was turned into a switch where possible (e.g. `chargeRepeatHit` mirrors Bedrock's 0.5 s re-hit interval); **engine / rendering** differences are out of scope (Bedrock's Fire Aspect igniting candles/campfires/TNT, charge crit particles).

With `jabDamage = "BEDROCK"` the extra point goes onto the attack-damage attribute, so the **tooltip and the stab gain +1 together**; whether the charge gains it too depends on `chargeStrength` (with `BEDROCK` the charge baseline also takes modifiers).

## Deviations from vanilla

These are the places where this mod intentionally differs from Java Edition vanilla (each has a switch in the previous section); everything else defaults to vanilla behaviour:

- **Charge damage baseline**: vanilla uses a fixed baseline of 1 (bare-hand damage) plus the relative-speed multiplier; this mod always uses the weapon's attack damage as the baseline, and `chargeStrength` only decides whether modifiers such as Strength/Weakness count.
- **Stab speed bonus**: vanilla stab damage only uses the weapon's attack damage and never scales with movement speed; this mod does not add it by default (`jabSpeedBonus = "MODDED"` enables it).
- **Per-target contact cooldown**: vanilla resolves a charge against each target once and has no extra stab cooldown; this mod defaults to that, and the `MODDED` settings of `chargeRepeatHit` / `jabRepeatHit` add a 10-tick cooldown.
- **Zero minimum range**: vanilla cannot hit anything closer than 2 blocks; `minAttackRange = "MODDED"` removes that limit.
- **Jab recovery**: vanilla Java lets you charge again immediately after a jab; `jabToChargeDelay = "BEDROCK"` adds the spear's use-cooldown as recovery.
- **Moving-target assist**: vanilla judges at the server-side position (you must lead moving targets); `movingTargetAssist = "MODDED"` rewinds 3 ticks to the position the client sees.
- **Mob spears**: vanilla only has zombies/husks/zombie villagers/zombie horsemen with iron spears and piglins/zombified piglins with golden spears; this mod defaults to that list, and the chance table + random materials are the `MODDED` setting (`mobSpearEquipTable`, `mobSpearDropChance`).

---

## FAQ

**Q: Why is a spear weaker than a sword?**
Because most of its damage comes from the **speed bonus**. Standing still and swinging is indeed worse than a sword, but a sprinting charge can hit far harder than one.

**Q: Left click does nothing / it will not stab.**
The attack cooldown bar is not full (the bar under your crosshair), or the target was stabbed moments ago and is still immune.

**Q: I cannot find the spears in the creative inventory.**
They are disabled by default — see the Configuration section above. You can also search the item ID directly in the creative search box, or use `/give`.

**Q: Can I put Sharpness on it?**
Yes. Spears take enchantments like any other weapon. Lunge is the spear-specific one.

**Q: Does it support other versions?**
This branch is **1.20.1 + Forge**; **1.21.1 + NeoForge** lives on the `main` branch. Gameplay is identical.

**Q: Does it conflict with other mods?**
**JEI** (recipe viewing), **KubeJS** (add your own spears from scripts) and **Punchy** (first-person view animation) are all supported.

---

## License

MIT License

---

# For mod and datapack authors

Everything public you can hook into from another mod, a datapack, or a KubeJS script.

## Items and tags

| Thing | ID |
|---|---|
| Built-in spears | `spearcore:wooden_spear`, `stone_spear`, `copper_spear`, `iron_spear`, `golden_spear`, `diamond_spear`, `netherite_spear` |
| The six vanilla-material spears | `#spearcore:spears` |
| All seven spears | `#spearcore:spear` |
| Vanilla spear tag | `#minecraft:spears` |
| Melee weapon tag | `#minecraft:swords` (which also pulls them into `#minecraft:tools` and `#forge:tools`) |
| Piglin loved | `#minecraft:piglin_loved` (golden spear only) |
| Damage type | `spearcore:spear` |
| Lunge enchantment | `spearcore:lunge` (`supported_items` is `#spearcore:spear`, max level 3) |
| Player attributes | `spearcore:spear_stab_multiplier` (stab damage multiplier, default 1.0), `spearcore:spear_charge_multiplier` (charge damage multiplier, default 1.0) |

## Class hierarchy

```
net.minecraft.world.item.Item
└── SpearItem                 (abstract) declares every spear parameter (reach, phases, hitbox, sounds, ...)
    └── BaseSpearItem         (abstract) Stats-driven; the seven built-in spears are its inner subclasses
        └── ConfiguredSpearItem          generic concrete implementation; registers on construction
                                         (used by the KubeJS and data-driven paths)
```

- `SpearItem`: the abstract methods describe what the spear looks like and how it fights. Client mixins, network packets and mob AI all test `instanceof SpearItem`.
- `BaseSpearItem`: two constructors — `(SpearStats.Stats)` derives the item properties from the stats, while `(SpearStats.Stats, Item.Properties)` hands full control of the item properties to the caller.
- `ConfiguredSpearItem`: a finished class bound to no material; `new ConfiguredSpearItem(stats)` is enough. It references **no third-party mod classes**, so it is safe for optional integrations.

### Defining your own spear (Java)

```java
public class MySpearItem extends ConfiguredSpearItem {
    public MySpearItem() {
        super(SpearStats.Stats.of(
            /* durability */ 300, /* attackDuration (seconds) */ 0.9f, /* damageMultiplier */ 1.0f,
            /* attackDamageBonus */ 3.0f, /* enchantmentValue */ 15,
            /* rarity */ Rarity.UNCOMMON, /* repairIngredient */ Ingredient.of(Items.COPPER_INGOT),
            /* fireResistant */ false, /* materialName */ "my_spear",
            /* use/hit/attack sounds */ SpearSounds.ITEM_SPEAR_USE.get(), SpearSounds.ITEM_SPEAR_HIT.get(), SpearSounds.ITEM_SPEAR_ATTACK.get(),
            /* swingTimes */ 0.9f, /* hitboxMargin */ 0.25f, /* contactCooldownTicks */ 10,
            /* delayTicks */ 12,
            /* three phase conditions */ SpearCondition.ofAttackerSpeed(50, 0.3F),
                                         SpearCondition.ofAttackerSpeed(135, 5.1F),
                                         SpearCondition.ofRelativeSpeed(225, 4.6F),
            /* forwardMovement */ 0.38f, /* minRange */ 2.0f, /* maxRange */ 4.5f,
            /* creative ranges */ 2.0f, 6.5f,
            /* hitboxMargin2 */ 0.25f, /* mobFactor */ 0.5f,
            /* dealsKnockback */ true, /* dismounts */ true));
    }
}
```

If you cannot be bothered with the numbers, `SpearStats.spearOf(...)` is a simplified factory that derives the phase parameters from `attackDuration`, and the built-in presets are available as `SpearStats.WOOD / STONE / COPPER / IRON / GOLD / DIAMOND / NETHERITE`.

> Note: registration has to happen during the **item registry event**, because the constructor records the `Item → Stats` mapping into `SpearStats`. If you register through a deferred register, make sure the sound registry is populated before `SpearStats` initialises.

## Events

All of them are on Forge's `MinecraftForge.EVENT_BUS` (the game bus):

| Event | When | What you can change |
|---|---|---|
| `SpearDamageEvent` | Before each damage calculation (once for a stab, once for a charge) | `setMultiplier(`...`)` / `addMultiplier(`...`)` to adjust the final damage; `getAttackType()` tells `STAB` from `CHARGE` |
| `SpearHitEvent` | On a hit, after damage is applied | Read-only: lifesteal, ignite, particles, counters, advancements |
| `SpearChargePhaseEvent` | The instant a charge phase changes (fires once per change) | Read-only: `DELAY / DISMOUNT / KNOCKBACK / DAMAGE`, for custom animations, sounds or particles |
| `SpearMobEquipEvent` | When a mob is about to be given a spear on spawn | **Cancellable**; you can also swap the spear or change the drop chance |

## Extension points

- `UseSpearSpecialEntity`: lets **non-player entities** (your own mobs) hook into the spear speed checks by overriding `getJerotesSpearNeedSpeed()`, `getJerotesSpearDamageMultiple()`, `isJerotesSpearGetMotionLikePlayer()` and friends.
- `KnownMovementAccessor` / `SpearCooldownAccessor`: interfaces injected into entities via mixin, for reading and writing the "known movement" vector and the recently-stabbed immunity records.
- Custom sounds may reuse the entries in `SpearSounds`, or you can register your own `SoundEvent` and pass it to the stats.

## KubeJS (supported)

A script can register a real spear, with exactly the same syntax as on the 1.21.1 branch:

```js
StartupEvents.registry('item', event => {
    event.create('mypack:bronze_spear', 'spearcore:spear')
        .displayName('Bronze Spear')
        .durability(400)
        .attackDamageBonus(3)
        .texture('mypack:item/bronze_spear')
})
```

The full walkthrough (parameter tables, texture conventions, pitfalls; Chinese and English) is in **[kjs-guide.md](kjs-guide.md)**.

**Class-loading isolation**: `compat/kubejs` is the only package in the project that references `dev.latvian.mods.*`; the entry point is a class-name string in `kubejs.plugins.txt` plus `Class.forName`. Without KubeJS installed that package is never loaded (measured: a KubeJS-less boot shows zero ClassNotFound and zero exceptions).

**Matching version**: KubeJS **2001.6.5-build.26+forge** (1.20.1 Forge).

## Punchy (supported)

With Punchy installed, the spear charge animation **follows this mod's per-material timeline** —
raise (delay + dismount) → knockback → damage-only — instead of Punchy's fixed timeline.
Set `punchyStageBridge = false` to hand control back to Punchy.

Matching version: Punchy **2.8d** (1.20.1 Forge). Without Punchy the whole chain is skipped:
the bridge mixin config is `required=false` and `PunchyMixinPlugin` decides at startup whether to
apply it (measured: zero ClassNotFound and zero exceptions without Punchy).

## Porting notes (1.21.1 → 1.20.1)

These APIs do not line up between 1.21.1/NeoForge and 1.20.1/Forge (all already handled):

| 1.21.1 / NeoForge | 1.20.1 / Forge |
|---|---|
| `CustomPacketPayload` + `StreamCodec` | `SimpleImpl`'s `SimpleChannel` |
| `[[mixins]]` in `mods.toml` | `mixin { config ... }` in `build.gradle` → `MixinConfigs` in the MANIFEST |
| `Attributes.ENTITY_INTERACTION_RANGE` | `ForgeMod.ENTITY_REACH` |
| `getDefaultAttributeModifiers(ItemStack)` + `ItemAttributeModifiers` | `getDefaultAttributeModifiers(EquipmentSlot)` + `Multimap` |
| `Operation.ADD_VALUE`, modifier IDs as `ResourceLocation` | `Operation.ADDITION`, IDs as `UUID` |
| Enchantments are datapack JSON | Enchantments are registry objects: `LungeEnchantment` + `EnchantmentCategory.create` |
| `Item#postHurtEnemy` | `Item#hurtEnemy` (durability is deducted here on 1.20.1) |
| `ICancellableEvent` | `@Cancelable` |
| `ResourceLocation.fromNamespaceAndPath(ns, path)` | `new ResourceLocation(ns, path)` |

### Building and verifying

```bash
./gradlew build              # output: build/libs/spearcore-1.20.1-forge-3.2.0.jar
./gradlew runServer          # headless boot; the startup check prints "已注册 7 把长矛"
./gradlew runGameTestServer  # automated tests judged by the game itself
```

Read the verdict from the log: `All N required tests passed :)` or `N required tests failed :(`;
the exit code equals the number of failed required tests. The structure template lives in
`gameteststructures/` and Gradle stages it into `run/`.
