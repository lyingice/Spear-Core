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
- 饥饿值低于 8（半格饥饿条以下）时**不会触发**。
- 骑乘时、鞘翅飞行时、在水里时**不会触发**。

配合"移动越快伤害越高"的机制，突进能让你自己把速度加上去，是矛的核心附魔。

---

## 怪物也会用矛

部分敌对生物生成时有概率直接拿着矛，并且会用矛的 AI（靠近、蓄力、冲锋、拉开距离）：

| 生物 | 概率 |
|---|---|
| 卫道士 | 15% |
| 凋灵骷髅 | 12% |
| 尸壳 | 10% |
| 僵尸 / 僵尸村民 / 掠夺者 | 8% |
| 溺尸 | 6% |
| 骷髅 / 流浪者 | 5% |
| 猪灵蛮兵 | 固定拿金矛 |

所以别以为只有你会冲锋——**卫道士拿着矛冲过来是真的疼**。

---

## 配置

配置文件在 `config/spearcore-common.toml`：

```toml
# 是否启用原版材质长矛（木、石、铁、金、钻石、下界合金）
enableVanillaSpears = true

# 是否启用铜矛
enableCopperSpear = true

# 命中目标时给攻击者自己减速（水平速度 ×0.6）并取消奔跑
# 默认关闭：撞完保持速度和奔跑状态，接着冲
slowDownAttackerOnHit = false
```

前两个开关控制的是（**默认都开着**）：

- 这些矛**会不会出现在创造模式物品栏**；
- 怪物**会不会携带**这些矛。

**关掉也不影响**合成、`/give` 和其他正常获取途径——物品一直在，只是不在创造栏里、怪物也不带。

第三个开关 `slowDownAttackerOnHit` 管的是**撞完之后你自己**：

- `false`（默认）：命中后保持水平速度与奔跑状态，可以接着冲、连续撞；
- `true`：命中瞬间把自己水平速度削到 60% 并取消奔跑，也就是原版那股"撞完得重新起跑"的手感。

它**只影响攻击者自己的动量**，不影响被打飞那些目标的击退力度。

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
**JEI**（查配方）直接可用，**KubeJS**（用脚本加自己的矛）也已支持（KubeJS 2001.6.5-build.26+forge）。**Punchy**（第一人称视角动画接管）尚未接入，见下文。

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
            /* hitboxMargin2 */ 0.125f, /* mobFactor */ 0.5f,
            /* dealsKnockback */ true, /* dismounts */ false));
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

## Punchy（尚未接入）

`compat/punchy/PunchyCompat` 目前恒返回 false，行为等价于"没装 Punchy"，即不接管第一人称视角动画。
Punchy 在 1.20.1 Forge 上的最新版是 **2.8d**，接入属下一轮工作。

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
./gradlew build              # 产物：build/libs/spearcore-1.20.1-forge-3.0.0.jar
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
- It does **not** trigger when your hunger is below 8 (under half a drumstick).
- It does **not** trigger while riding, elytra flying, or in water.

Combined with the "faster means more damage" rule, Lunge lets you build up your own speed — it is the spear's signature enchantment.

---

## Mobs use spears too

Some hostile mobs spawn already holding a spear, and they use spear AI (approach, charge, retreat):

| Mob | Chance |
|---|---|
| Vindicator | 15% |
| Wither skeleton | 12% |
| Husk | 10% |
| Zombie / zombie villager / pillager | 8% |
| Drowned | 6% |
| Skeleton / stray | 5% |
| Piglin brute | always, with a golden spear |

So do not assume you are the only one who can charge — **a vindicator charging with a spear genuinely hurts**.

---

## Configuration

The config file is `config/spearcore-common.toml`:

```toml
# Whether the vanilla-material spears are enabled (wood, stone, iron, gold, diamond, netherite)
enableVanillaSpears = true

# Whether the copper spear is enabled
enableCopperSpear = true

# Slow the attacker down on hit (horizontal speed x0.6) and cancel sprinting
# Default off: keep your speed and your sprint through the hit
slowDownAttackerOnHit = false
```

The first two switches (both **on by default**) control:

- whether these spears **appear in the creative inventory**;
- whether mobs **spawn carrying** them.

**Turning them off changes nothing else**: crafting, `/give` and every normal way of obtaining them still work. The items always exist — they are simply hidden from the creative tabs and never handed to mobs.

The third switch, `slowDownAttackerOnHit`, is about **you, right after a hit**:

- `false` (default): you keep your horizontal speed and your sprint, so you can chain charges;
- `true`: on impact your own horizontal speed is cut to 60% and sprinting is cancelled — the vanilla "you have to build up speed again" feel.

It affects **only the attacker's own momentum**; the knockback dealt to the target is unchanged.

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
**JEI** (recipe viewing) works today, and **KubeJS** (add your own spears from scripts) is supported too (KubeJS 2001.6.5-build.26+forge). **Punchy** (taking over the first-person view animation) is not wired up yet — see below.

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
            /* hitboxMargin2 */ 0.125f, /* mobFactor */ 0.5f,
            /* dealsKnockback */ true, /* dismounts */ false));
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

## Punchy (not wired up yet)

`compat/punchy/PunchyCompat` currently always returns false, i.e. exactly the "Punchy is not installed" behaviour — the first-person view animation is not taken over. The newest Punchy for 1.20.1 Forge is **2.8d**; hooking it up is the next round of work.

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
./gradlew build              # output: build/libs/spearcore-1.20.1-forge-3.0.0.jar
./gradlew runServer          # headless boot; the startup check prints "已注册 7 把长矛"
./gradlew runGameTestServer  # automated tests judged by the game itself
```

Read the verdict from the log: `All N required tests passed :)` or `N required tests failed :(`;
the exit code equals the number of failed required tests. The structure template lives in
`gameteststructures/` and Gradle stages it into `run/`.
