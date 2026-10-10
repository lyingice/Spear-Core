# Asset ledger — video-kjs

All assets are **copied into this project**; the composition references project-local
relative paths only (`assets/...`). Nothing is drawn by hand — every pixel sprite is a
real game texture.

| File | Source | Use in the film |
|---|---|---|
| `vendor/gsap.min.js` | `D:\0CreaterMods\UltraEnchantment\video\assets\vendor\gsap.min.js` (3.x, local; jsDelivr unreachable on this machine) | timeline engine |
| `fonts/NotoSansSC-VF.ttf` | `...\video\assets\fonts\NotoSansSC-VF.ttf` | all Chinese + Latin display text (family `SC Sans`) |
| `fonts/JetBrainsMono-400.woff2` | `...\video\assets\fonts\JetBrainsMono-400.woff2` | code block (family `Mono Code`) |
| `fonts/JetBrainsMono-700.woff2` | `...\video\assets\fonts\JetBrainsMono-700.woff2` | reserved weight |
| `items/kjs_spear.png` (16×16) | `C:\Users\12501\MCreatorWorkspaces\spearcore\run\kubejs\assets\kubejs\textures\item\kjs_spear.png` | hero icon, scene C — 20× nearest-neighbour |
| `items/kjs_spear_in_hand.png` (32×32) | `...\run\kubejs\assets\kubejs\textures\item\kjs_spear_in_hand.png` | scene D backdrop silhouette — 24× nearest-neighbour |
| `items/mod/{wooden,stone,copper,iron,golden,diamond,netherite}_spear.png` (16×16 each) | `...\spearcore\src\main\resources\assets\spearcore\textures\item\` | dim grayscale row behind the title (the seven built-in spears) — 4× nearest-neighbour |

## Pixel-integrity rules honoured

* every sprite carries `image-rendering: pixelated`
* every sprite is scaled by an **integer** factor (4×, 20×, 24×)
* no rotation, no blur, no resampling filter; the mod row is only `grayscale(1) brightness(2)`
  (a per-pixel colour map, not a resample), the outro backdrop is `brightness(0) invert(1)`
  (a silhouette of the same alpha shape)

## Not used

`*_spear_in_hand.png` for the seven built-in spears (not needed by the shot design).
