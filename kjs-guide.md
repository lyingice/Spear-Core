# 用 KubeJS 给 Spear Core 加矛

这篇只讲 KubeJS 的用法。你不需要写 Java、不需要会编译，只要会改脚本和放贴图。

---

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
