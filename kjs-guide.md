# 用 KubeJS 给 Spear Core 加矛 / Adding Spears to Spear Core with KubeJS

> **中文版在下半部分，English version below.**  [中文版](#中文版) ｜ [English version](#english-version)
>
> 这份文档只讲 KubeJS 的用法，不需要写 Java、不需要编译。 / This guide only covers the KubeJS side: no Java and no compiling required.
>
> **版本提示**：这份教程**两个分支都适用**。1.20.1 + Forge 用 KubeJS **2001.6.5-build.26+forge**，1.21.1 + NeoForge 用 2101.x —— 脚本写法完全一致。 / **Version note**: this guide applies to **both branches**. 1.20.1 + Forge uses KubeJS **2001.6.5-build.26+forge**, 1.21.1 + NeoForge uses 2101.x — the script syntax is identical.

---

# 中文版

## 0. 前提

- 游戏版本 **1.21.1**，加载器 **NeoForge**（Spear Core 本身只支持这一档）。
- 装了 **KubeJS**（NeoForge 版，版本号形如 `2101.x`）。
- 装了 **Spear Core**。
- KubeJS 和 Spear Core **客户端、服务端都要装**。只装服务端的话物品能存在但没有模型。

Spear Core 没装 KubeJS 时一切照旧，不会报错，也不会加载任何相关代码。

---

## 1. 五行做一个矛

新建 `kubejs/startup_scripts/my_spears.js`：

```js
StartupEvents.registry('item', event => {
    event.create('mypack:bronze_spear', 'spearcore:spear')
        .displayName('青铜矛')
        .durability(400)
        .attackDamageBonus(3)
        .texture('mypack:item/bronze_spear')
})
```

重开游戏，然后 `/give @s mypack:bronze_spear`。

它是一把**真的矛**——蓄力突刺、冲锋、第一/第三人称动作、怪物拿在手里的 AI、附魔，全都是内建矛那一套，不需要你再配任何东西。

> 关键点：第二个参数 `'spearcore:spear'` 就是"物品类型"。写它，KubeJS 就会用 Spear Core 的矛类来创建这个物品。

---

## 2. 物品 ID 怎么起

```js
event.create('mypack:bronze_spear', 'spearcore:spear')
//            ^^^^^^^ 命名空间   ^^^^^^^^^^ 物品名
```

- 带上 `命名空间:` 前缀最稳妥，贴图会自动去同一个命名空间找。
- 不写前缀的话会落到 `kubejs:` 命名空间，脚本一多容易撞名。
- 同一局游戏里 ID 不能重复，重复了 KubeJS 会在日志里报 `Duplicate key`。

---

## 3. 贴图放哪

矛要**两张**贴图（这是它和普通物品最大的区别）：

| 用途 | 文件 |
|---|---|
| 背包 / GUI / 掉落物里的平面图标 | `kubejs/assets/<命名空间>/textures/item/<名字>.png` |
| 拿在手里、第三人称那张 | `kubejs/assets/<命名空间>/textures/item/<名字>_in_hand.png` |

以上面的例子：

```
kubejs/
├── startup_scripts/
│   └── my_spears.js
└── assets/
    └── mypack/
        └── textures/
            └── item/
                ├── bronze_spear.png           ← 平面图标
                └── bronze_spear_in_hand.png   ← 手持贴图
```

**约定**：你只写一个 `.texture('mypack:item/bronze_spear')`，Spear Core 会自动去找同名的 `_in_hand` 版本。手持那张建议画成斜向的矛身，因为游戏里就是那样握着的。

两张图不想用同一套命名，就单独指定：

```js
.texture('mypack:item/bronze_spear')            // 平面图标
.inHandTexture('mypack:item/bronze_spear_3d')   // 手持
```

> 模型文件（`models/item/*.json`）**不用你写**，Spear Core 会自动生成。而且必须由它生成——矛不能用普通的 `item/generated`，那样拿在手里会被压平、从中间劈开，你手写反而容易写坏。

---

## 4. 所有可用参数

不写就用**铁矛**的数值。想调再调，不用全写。

### 4.1 伤害与耐久

| 方法 | 作用 | 默认（铁矛） |
|---|---|---|
| `.durability(数字)` | 耐久度 | 250 |
| `.attackDamageBonus(数字)` | 固定攻击力加成 | 2 |
| `.damageMultiplier(数字)` | 冲锋伤害系数：伤害 = 基础伤害 + 相对速度 × 该值 | 0.95 |
| `.attackDuration(秒)` | 攻击冷却，**越小攻速越快**（攻击速度 = 1 ÷ 该值） | 0.95 |
| `.enchantmentValue(数字)` | 附魔能力，越大越好附魔 | 14 |
| `.repairItem('物品id', ...)` | 铁砧修复材料，直接写物品 id，可写多个 | 铁锭 |
| `.rarity('uncommon')` | 稀有度：`'common'` / `'uncommon'` / `'rare'` / `'epic'` | common |
| `.fireResistant()` | 防火（像下界合金那样烧不掉） | 关 |

### 4.2 手感与判定

| 方法 | 作用 | 默认 |
|---|---|---|
| `.phases(起手, 击落, 击退, 伤害)` | 四个蓄力阶段时长，单位**秒**；依次是"起手准备 / 能把骑手捅下来 / 能把人击退 / 能造成伤害"的有效窗口 | 0.6 / 2.5 / 6.75 / 11.25 |
| `.reach(最小, 最大, 创造最小, 创造最大)` | 攻击距离 | 2 / 4.5 / 2 / 6.5 |
| `.hitbox(半径1, 半径2)` | 判定箱外扩，越大越容易捅到 | 0.25 / 0.125 |
| `.swing(秒)` | 挥击间隔（怪物 AI 与动画使用） | 0.95 |
| `.contactCooldown(刻)` | 同一目标两次突刺之间的免疫时间，防止一次冲锋连续打同一个人 | 10 |
| `.forwardMovement(数字)` | 冲锋时向前推进的力度 | 0.38 |
| `.mobFactor(数字)` | 怪物（非玩家）的攻击距离倍率 | 0.5 |
| `.knockback(true/false)` | 会不会把目标击退 | true |
| `.dismount(true/false)` | 会不会把骑手从坐骑上捅下来 | false |

### 4.3 音效与杂项

| 方法 | 作用 |
|---|---|
| `.sounds(使用音, 命中音, 挥击音)` | 三个音效，可以用 KubeJS 自己注册的音效 |
| `.materialName('名字')` | 只是个标记，方便你自己记是哪把矛，不影响手感 |

---

## 5. 一把完整的自定义矛

```js
StartupEvents.registry('item', event => {
    event.create('mypack:bronze_spear', 'spearcore:spear')
        .displayName('青铜矛')
        // —— 数值 ——
        .durability(400)
        .attackDamageBonus(3)
        .damageMultiplier(1.1)
        .attackDuration(0.85)
        .enchantmentValue(18)
        .repairItem('minecraft:copper_ingot')
        .rarity('uncommon')
        // —— 手感 ——
        .phases(0.7, 3.0, 8.0, 13.0)
        .reach(2.5, 5.0, 2.5, 7.0)
        .mobFactor(0.6)
        .knockback(true)
        .dismount(true)
        // —— 外观 ——
        .texture('mypack:item/bronze_spear')
})
```

配套贴图：

```
kubejs/assets/mypack/textures/item/bronze_spear.png
kubejs/assets/mypack/textures/item/bronze_spear_in_hand.png
```

---

## 6. 和原版 / 内建矛的关系

- 新矛会**自动**被加进 `#spearcore:spears` 与 `#minecraft:spears` 两个标签。
  所以锋利、亡灵杀手、节肢杀手、耐久、火焰附加、抢夺这些附魔，它和内建矛享受同一套规则，不用你额外配。
- 内建那七把矛（木 / 石 / 铜 / 铁 / 金 / 钻石 / 下界合金）由 Spear Core 自己的配置项控制是否出现，和你加的矛互不影响。

---

## 7. 上架到创造模式物品栏

KubeJS 物品默认不进原版的"战斗"页，想放进去自己加一段：

```js
StartupEvents.modifyCreativeTab('minecraft:combat', event => {
    event.add('mypack:bronze_spear')
})
```

---

## 8. 几个必须知道的坑

**1）不能用"普通物品 + 加标签"糊弄过去。**
矛的行为是按"这个物品是不是矛类"判定的。用 `event.create('mypack:spear')` 造一个普通物品、再塞进 `#spearcore:spears`，它照样没有突刺、没有冲锋、没有动画。**必须**写成 `event.create(..., 'spearcore:spear')`。

**2）KubeJS 的通用物品回调对矛不生效。**
`.use(...)`、`.finishUsingItem(...)` 这类 KubeJS 自带回调，在矛身上不会触发——矛的"使用"行为由 Spear Core 自己实现。同理 `.attributes(...)` 也不生效，攻击力请用 `.attackDamageBonus()` 与 `.damageMultiplier()`。

**3）耐久和堆叠不是随便设的。**
矛永远**不可堆叠**（堆叠数 1）。调 `.durability()` 就够了，别试图让它堆叠。

**4）新增物品必须重启游戏。**
`startup_scripts` 里的注册只在启动时执行一次；`/reload` 或 KubeJS 的脚本重载**不会**把新物品注册进去。改了脚本请重启。

**5）服务器上也要有脚本和贴图。**
物品是注册表内容，联机时客户端必须也有同一份脚本与贴图，否则别人看到的是紫黑方块或者空手。

**6）别自己写 `models/item/*.json`。**
矛的模型由 Spear Core 生成（`neoforge:separate_transforms`）。自己覆盖它，多半会得到"GUI 里正常、拿手里被劈成两半"的效果。

---

## 9. 怎么确认成功了

启动日志里应该能看到：

```
[KubeJS/]: Found plugin source spearcore
[net.minecraft.spearcore.SpearcoreMod/]: [spearcore] KubeJS 联动已启用：物品类型 'spearcore:spear' 可用
[KubeJS Startup/]: Loaded 1/1 KubeJS startup scripts in ... with 0 errors and 0 warnings
```

以及标签那一步会带上你的矛：

```
[KubeJS Server/]: [minecraft:item] Found 395 tags, added 2 objects, removed 0 objects
```

脚本写错时错误只会出现在 KubeJS 的日志/控制台里，游戏本身照常启动——按报错里的行号去改就行。

**看不到 `Found plugin source spearcore` 或"联动已启用"那一行**：说明 KubeJS 没装、版本不对（需要 `2101.x`），或者你在 `kubejs/plugins.txt` 里把它禁掉了。

---

## 10. 参数速查（复制粘贴用）

```js
event.create('命名空间:名字', 'spearcore:spear')
    .displayName('显示名')
    .durability(250)                 // 耐久
    .attackDamageBonus(2)            // 固定攻击力
    .damageMultiplier(0.95)          // 冲锋伤害系数
    .attackDuration(0.95)            // 攻击冷却(秒)
    .enchantmentValue(14)            // 附魔能力
    .repairItem('minecraft:iron_ingot')          // 修复材料(可写多个)
    .rarity('common')                // common/uncommon/rare/epic
    .fireResistant()                 // 可选：防火
    .phases(0.6, 2.5, 6.75, 11.25)   // 起手/击落/击退/伤害(秒)
    .reach(2.0, 4.5, 2.0, 6.5)       // 生存min/max, 创造min/max
    .hitbox(0.25, 0.125)             // 判定箱外扩
    .swing(0.95)                     // 挥击间隔(秒)
    .contactCooldown(10)             // 同目标突刺免疫(刻)
    .forwardMovement(0.38)           // 冲锋前推力度
    .mobFactor(0.5)                  // 怪物攻击距离倍率
    .knockback(true)                 // 是否击退
    .dismount(false)                 // 是否击落骑手
    .materialName('custom')          // 标记名
    .texture('命名空间:item/名字')      // 图标贴图(手持自动找 _in_hand)
    .inHandTexture('命名空间:item/手持图') // 可选：单独指定手持贴图
```

---

# English version

## 0. Requirements

- Minecraft **1.21.1** with **NeoForge** (the only target Spear Core supports).
- **KubeJS** for NeoForge, version like `2101.x`.
- **Spear Core**.
- Both KubeJS and Spear Core must be installed on the **client and the server**. Server-only means the item exists but renders as nothing.

If KubeJS is absent, Spear Core behaves exactly as before: nothing errors, and none of the integration code is ever loaded.

---

## 1. A spear in five lines

Create `kubejs/startup_scripts/my_spears.js`:

```js
StartupEvents.registry('item', event => {
    event.create('mypack:bronze_spear', 'spearcore:spear')
        .displayName('Bronze Spear')
        .durability(400)
        .attackDamageBonus(3)
        .texture('mypack:item/bronze_spear')
})
```

Restart the game, then `/give @s mypack:bronze_spear`.

It is a **real spear**: charged stab, lunge, first- and third-person animations, mob AI when a monster holds it, enchanting — the entire built-in spear behaviour, with nothing else to configure.

> The second argument, `'spearcore:spear'`, is the **item type**. With it, KubeJS builds the item from Spear Core's spear class.

---

## 2. Choosing the item ID

```js
event.create('mypack:bronze_spear', 'spearcore:spear')
//            ^^^^^^^ namespace    ^^^^^^^^^^ item name
```

- Always include the `namespace:` prefix. Textures are then looked up in the same namespace automatically.
- Without a prefix the item lands in the `kubejs:` namespace, which gets crowded quickly.
- IDs must be unique. A duplicate makes KubeJS log `Duplicate key`.

---

## 3. Where the textures go

A spear needs **two** textures (this is what sets it apart from an ordinary item):

| Purpose | File |
|---|---|
| Flat icon in the inventory / GUI / dropped on the ground | `kubejs/assets/<namespace>/textures/item/<name>.png` |
| The one seen in hand and in third person | `kubejs/assets/<namespace>/textures/item/<name>_in_hand.png` |

For the example above:

```
kubejs/
├── startup_scripts/
│   └── my_spears.js
└── assets/
    └── mypack/
        └── textures/
            └── item/
                ├── bronze_spear.png           <- flat icon
                └── bronze_spear_in_hand.png   <- in-hand texture
```

**The convention:** you only write `.texture('mypack:item/bronze_spear')`, and Spear Core looks for the matching `_in_hand` variant on its own. Draw the in-hand texture as a diagonal spear shaft, because that is how the item is held in game.

If you do not want the two textures to share a name, set them separately:

```js
.texture('mypack:item/bronze_spear')            // flat icon
.inHandTexture('mypack:item/bronze_spear_3d')   // in hand
```

> You do **not** write the model JSON (`models/item/*.json`); Spear Core generates it. It has to be generated by Spear Core anyway: a spear cannot use a plain `item/generated` model, which would be flattened and split down the middle in hand. Hand-writing it usually makes things worse.

---

## 4. All available parameters

Anything you leave out uses the **iron spear** values. Tweak only what you care about.

### 4.1 Damage and durability

| Method | Effect | Default (iron spear) |
|---|---|---|
| `.durability(number)` | Durability | 250 |
| `.attackDamageBonus(number)` | Flat attack damage bonus | 2 |
| `.damageMultiplier(number)` | Lunge damage multiplier: damage = base damage + relative speed x this value | 0.95 |
| `.attackDuration(seconds)` | Attack cooldown; **smaller means faster** (attack speed = 1 / this value) | 0.95 |
| `.enchantmentValue(number)` | Enchantability; higher enchants better | 14 |
| `.repairItem('item id', ...)` | Anvil repair material, written as item ids; several allowed | iron ingot |
| `.rarity('uncommon')` | Rarity: `'common'` / `'uncommon'` / `'rare'` / `'epic'` | common |
| `.fireResistant()` | Fire resistant (like netherite) | off |

### 4.2 Handling and hit detection

| Method | Effect | Default |
|---|---|---|
| `.phases(delay, dismount, knockback, damage)` | The four charge-up phases in **seconds**: ready-up / can pull a rider off / can knock the target back / can deal damage | 0.6 / 2.5 / 6.75 / 11.25 |
| `.reach(min, max, creativeMin, creativeMax)` | Attack range | 2 / 4.5 / 2 / 6.5 |
| `.hitbox(margin1, margin2)` | Hitbox inflation; larger is easier to hit with | 0.25 / 0.125 |
| `.swing(seconds)` | Swing interval (used by mob AI and animations) | 0.95 |
| `.contactCooldown(ticks)` | Immunity between two stabs on the same target, so one lunge cannot hit the same entity repeatedly | 10 |
| `.forwardMovement(number)` | Lunge forward push | 0.38 |
| `.mobFactor(number)` | Attack range multiplier for monsters (non-players) | 0.5 |
| `.knockback(true/false)` | Whether it knocks the target back | true |
| `.dismount(true/false)` | Whether it pulls a rider off their mount | false |

### 4.3 Sound and misc

| Method | Effect |
|---|---|
| `.sounds(useSound, hitSound, attackSound)` | Three sound events; may be ones you registered with KubeJS |
| `.materialName('name')` | Just a label to remind you which spear this is; no gameplay effect |

---

## 5. A complete custom spear

```js
StartupEvents.registry('item', event => {
    event.create('mypack:bronze_spear', 'spearcore:spear')
        .displayName('Bronze Spear')
        // -- stats --
        .durability(400)
        .attackDamageBonus(3)
        .damageMultiplier(1.1)
        .attackDuration(0.85)
        .enchantmentValue(18)
        .repairItem('minecraft:copper_ingot')
        .rarity('uncommon')
        // -- handling --
        .phases(0.7, 3.0, 8.0, 13.0)
        .reach(2.5, 5.0, 2.5, 7.0)
        .mobFactor(0.6)
        .knockback(true)
        .dismount(true)
        // -- looks --
        .texture('mypack:item/bronze_spear')
})
```

Matching textures:

```
kubejs/assets/mypack/textures/item/bronze_spear.png
kubejs/assets/mypack/textures/item/bronze_spear_in_hand.png
```

---

## 6. How it relates to vanilla and the built-in spears

- New spears are **automatically** added to both `#spearcore:spears` and `#minecraft:spears`.
  Sharpness, Smite, Bane of Arthropods, Unbreaking, Fire Aspect and Looting therefore treat it exactly like a built-in spear, with no extra configuration.
- The seven built-in spears (wood / stone / copper / iron / golden / diamond / netherite) are toggled by Spear Core's own config and are independent of anything you add.

---

## 7. Adding it to a creative tab

KubeJS items do not land in vanilla's Combat tab by default. Add them yourself:

```js
StartupEvents.modifyCreativeTab('minecraft:combat', event => {
    event.add('mypack:bronze_spear')
})
```

---

## 8. Pitfalls you need to know

**1) "Ordinary item + tag" does not work.**
Spear behaviour is decided by whether the item's class is a spear. Creating a plain item with `event.create('mypack:spear')` and adding it to `#spearcore:spears` still gives you no stab, no lunge and no animation. It **must** be `event.create(..., 'spearcore:spear')`.

**2) KubeJS's generic item callbacks do not fire for spears.**
Callbacks such as `.use(...)` and `.finishUsingItem(...)` never run on a spear, because the "use" behaviour is implemented inside Spear Core. For the same reason `.attributes(...)` has no effect; use `.attackDamageBonus()` and `.damageMultiplier()` for damage.

**3) Durability and stacking are not free-form.**
A spear is always **unstackable** (stack size 1). Setting `.durability()` is all you need; do not try to make it stack.

**4) New items require a game restart.**
Registration in `startup_scripts` only runs once at startup. `/reload` and KubeJS's script reload do **not** register new items. Restart after editing.

**5) The server needs the scripts and textures too.**
Items are registry content, so in multiplayer the client must have the same scripts and textures, or players will see a purple-black square or an empty hand.

**6) Do not write `models/item/*.json` yourself.**
The spear model is generated by Spear Core (`neoforge:separate_transforms`). Overwriting it usually produces "fine in the GUI, split in half in hand".

---

## 9. How to tell it worked

The startup log should contain:

```
[KubeJS/]: Found plugin source spearcore
[net.minecraft.spearcore.SpearcoreMod/]: [spearcore] KubeJS 联动已启用：物品类型 'spearcore:spear' 可用
[KubeJS Startup/]: Loaded 1/1 KubeJS startup scripts in ... with 0 errors and 0 warnings
```

and the tag pass will include your spear:

```
[KubeJS Server/]: [minecraft:item] Found 395 tags, added 2 objects, removed 0 objects
```

If a script is wrong, the error only shows up in KubeJS's log/console and the game still starts; fix it by the line number in the report.

**If you do not see `Found plugin source spearcore` or the "联动已启用" line**, then KubeJS is missing, the version is wrong (you need `2101.x`), or you disabled the plugin in `kubejs/plugins.txt`.

---

## 10. Cheat sheet (copy and paste)

```js
event.create('namespace:name', 'spearcore:spear')
    .displayName('Display Name')
    .durability(250)                 // durability
    .attackDamageBonus(2)            // flat attack damage
    .damageMultiplier(0.95)          // lunge damage multiplier
    .attackDuration(0.95)            // attack cooldown (seconds)
    .enchantmentValue(14)            // enchantability
    .repairItem('minecraft:iron_ingot')          // repair material (several allowed)
    .rarity('common')                // common/uncommon/rare/epic
    .fireResistant()                 // optional: fire proof
    .phases(0.6, 2.5, 6.75, 11.25)   // delay/dismount/knockback/damage (seconds)
    .reach(2.0, 4.5, 2.0, 6.5)       // survival min/max, creative min/max
    .hitbox(0.25, 0.125)             // hitbox inflation
    .swing(0.95)                     // swing interval (seconds)
    .contactCooldown(10)             // same-target stab immunity (ticks)
    .forwardMovement(0.38)           // lunge forward push
    .mobFactor(0.5)                  // mob attack range multiplier
    .knockback(true)                 // knock targets back
    .dismount(false)                 // pull riders off mounts
    .materialName('custom')          // label
    .texture('namespace:item/name')      // icon texture (in-hand auto uses _in_hand)
    .inHandTexture('namespace:item/in_hand') // optional: set the in-hand texture explicitly
```
