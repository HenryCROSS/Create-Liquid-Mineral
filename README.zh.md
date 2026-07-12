# Create: Liquid Mineral（熔融矿物）

<p align="center">
  <img src="src/main/resources/createliquidmineral_logo.png" alt="Create: Liquid Mineral 图标" width="128">
</p>

**[English →](README.md)**

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-3B8526) ![NeoForge](https://img.shields.io/badge/NeoForge-21.1.234%2B-1F6FEB) ![Create](https://img.shields.io/badge/Create-6.0.0%2B-F2A93B) ![License](https://img.shields.io/badge/License-GPLv3-blue)

给游戏添加 8 种"熔融金属"流体——铁、金、铜、钻石（幻想设定）、下界合金、锌、黄铜、琥珀金（电气石/electrum）。它们都是真正可用桶装、可放置、可以被机械动力（Create）的水泵/管道系统运输的流体，每种都有独立的物理特性、发光度和动画贴图。

- **Minecraft 版本**：1.21.1
- **模组加载器**：NeoForge 21.1.234 及以上
- **必需前置**：机械动力（Create）6.0.0 及以上
- **可选前置**：Create: Additions & Synthetics 1.6.0 及以上（只有琥珀金/电气石这一种流体需要，没装这个前置的话这一种会被自动跳过，不影响其他流体）
- **作者**：CROSS
- **协议**：[GPLv3](LICENSE)
- **AI 使用说明**：本 mod 的代码、贴图生成工具和文档都是在 AI 结对编程（Claude Code）协助下完成的。

---

## 目录

1. [这个 mod 加了什么](#这个-mod-加了什么)
2. [安装](#安装)
3. [流体列表](#流体列表)
4. [配置文件：fluidsjson](#配置文件fluidsjson)
5. [如何新增一个自定义流体](#如何新增一个自定义流体)
6. [使用你自己的贴图](#使用你自己的贴图)
7. [贴图生成工具](#贴图生成工具)
8. [已知局限](#已知局限)
9. [常见问题](#常见问题)
10. [许可协议](#许可协议)

---

## 这个 mod 加了什么

每种流体的行为都跟原版流体一样：能用桶舀起来、倒出去、也能用机械动力（Create）的水泵管道系统运输。每种流体都有自己独立的：

- **物理特性**——`"lava"` 或 `"water"` 预设（重量、发光、能不能游泳/淹死/水合/灭火、流速、桶的音效），或者选 `"custom"` 自己在配置里填每一项数值
- **颜色**——要么用自带颜色的程序化贴图，要么在共享贴图上叠加一层染色
- **密度/粘度/温度**——由物理特性决定的数值，其他读取流体属性的 mod 也可能用到
- **发光等级**——流体本身发出多少光（0-15）
- **灼烧行为**——碰到实体会不会造成伤害/点燃
- **免疫族群**——对熔岩类伤害免疫的生物是不是对这个流体也免疫

## 安装

1. 安装适用于 Minecraft 1.21.1 的 NeoForge 21.1.234 或更高版本。
2. 安装**机械动力（Create）** 6.0.0 或更高版本（必需前置，没有这个 mod 加载不了）。
3. *（可选）* 如果想要琥珀金/熔融电气石这个流体，安装 **Create: Additions & Synthetics** 1.6.0 或更高版本。不装的话，这一种流体会被跳过，其他流体照常工作。
4. 把 `createliquidmineral-<版本号>.jar` 丢进 `mods` 文件夹。
5. 启动一次游戏——这会在 `config/createliquidmineral/fluids.json` 生成默认配置文件（详见下文）。

## 流体列表

| 流体 ID | 显示名 | 物理特性 | 需要前置 |
|---|---|---|---|
| `molten_iron` | 熔融铁 | 熔岩类 | — |
| `molten_gold` | 熔融金 | 熔岩类 | — |
| `molten_copper` | 熔融铜 | 熔岩类 | — |
| `molten_diamond` | 熔融钻石 | 熔岩类 | — |
| `molten_netherite` | 熔融下界合金 | 熔岩类 | — |
| `molten_zinc` | 熔融锌 | 熔岩类 | — |
| `molten_brass` | 熔融黄铜 | 熔岩类 | — |
| `molten_amber_gold` | 熔融琥珀金（电气石） | 熔岩类 | Create: Additions & Synthetics |

> 熔融钻石在现实里并不存在——钻石在正常条件下不会熔化——所以把它做成了幻想向的"液态水晶"质感：色块更粗大、整套流体里高光最强，贴图里还带了一层半透明 alpha 通道——想让它真的显示成半透明，给它加一行 `"translucent": true` 就行，见下文"已知局限"。

这 8 种流体具体的密度/粘度/温度/发光等级等数值都在下面的配置文件里，改配置就能调，不用碰代码。

## 配置文件：`fluids.json`

位置：`config/createliquidmineral/fluids.json`（首次启动自动生成，用 mod 内置的默认值填充——永远是一份可用、有示例的文件，不会是空的）。

JSON 数组里每一项就是一种流体：

```json
{
  "id": "molten_iron",
  "enabled": true,
  "texture": "generated",
  "physics": "lava",
  "tint": null,
  "density": 7000,
  "viscosity": 6000,
  "temperature": 1800,
  "lightLevel": 15,
  "tickRate": null,
  "slopeFindDistance": null,
  "levelDecreasePerBlock": null,
  "canSwim": null,
  "canDrown": null,
  "canConvertToSource": null,
  "canHydrate": null,
  "canExtinguish": null,
  "burnsEntities": true,
  "protectsFamily": true,
  "requiredMod": null,
  "translucent": false
}
```

| 字段 | 类型 | 含义 |
|---|---|---|
| `id` | 字符串，必填 | 流体的注册名（也用来推导桶物品、方块、默认贴图文件名） |
| `enabled` | 布尔值或 `null` | `false` = 这个流体完全不注册——没有流体、没有方块、没有桶、创造模式栏也不会出现，就跟这条目不存在一样。`true`/`null`/不填 = 照常注册。**这个判断在 `requiredMod` 之前**：就算 `requiredMod` 列的前置全都装了，只要 `"enabled": false`，这个流体照样不会注册。 |
| `texture` | 字符串 | 取值 `"lava"`、`"generated"`、`"default"`、`"water"` 之一。缺失或写了识别不了的值 → 自动退回 `"default"`。详见下表。 |
| `physics` | 字符串 | `"lava"` 或 `"water"` 预设；填 `"custom"` 则从一个中性基线出发，完全由下面这些字段决定行为。缺失或识别不了的值 → 退回 `"water"`。详见下方[「物理预设 vs. `"custom"`」](#物理预设-vs-custom)。 |
| `tint` | 十六进制颜色字符串或 `null` | 给流体贴图染色，例如 `"#FF5A1F"`。在 `"water"` 贴图上染色效果最干净；用在 `"lava"` 贴图上会偏暖色，因为那张贴图本身就是橙色的。如果贴图本身已经带颜色（所有内置的 `"generated"` 贴图都是），就留 `null`。 |
| `density` | 整数或 `null` | 流体密度；`null` 就用物理预设的值 |
| `viscosity` | 整数或 `null` | 流体粘度；`null` 就用物理预设的值 |
| `temperature` | 整数或 `null` | 流体温度；`null` 就用物理预设的值 |
| `lightLevel` | 整数 0-15 或 `null` | 流体发光等级；`null` 就用物理预设的值 |
| `tickRate` | 整数或 `null` | 流体多少 tick 扩散一次——数值越小流得越快。原版水是 5，熔岩是 30。`null` 就用物理预设的值。 |
| `slopeFindDistance` | 整数或 `null` | 流体往侧面扩散前，往下找坡的搜索距离。原版水是 4，熔岩是 2。`null` 就用物理预设的值。 |
| `levelDecreasePerBlock` | 整数或 `null` | 流体每往侧面扩散一格，液位下降多少。原版水是 1，熔岩是 2。`null` 就用物理预设的值。 |
| `canSwim` / `canDrown` / `canConvertToSource` / `canHydrate` / `canExtinguish` | 布尔值或 `null` | 单独的物理行为开关（能不能游泳、会不会淹死、流动的能不能变成水源、会不会给耕地保湿、能不能灭火）。`null` 就用物理预设的值。 |
| `burnsEntities` | 布尔值 | `true` = 像熔岩一样造成伤害/点燃；`false`/不填 = 无害 |
| `protectsFamily` | 布尔值 | `true` = 对熔岩类伤害免疫的生物同样免疫这个流体；`false`/不填 = 这个流体有自己独立的免疫族群 |
| `requiredMod` | 字符串、字符串数组或 `null` | 一个（或多个）modid——其中**任意一个**已加载，这个流体就会注册。单个字符串（例如 `"createaddition"`）用法跟以前完全一样；想要"装了 A 或 B 任意一个就行"（不是"两个都要装"），就写成数组，比如 `["create", "createaddition"]`。不需要前置就留 `null`/不填。只有在 `enabled` 没被设成 `false` 的前提下才会检查——`enabled` 的优先级更高，见上面那行。 |
| `translucent` | 布尔值或 `null` | `true` = 用半透明混合渲染这个流体（像原版水一样），而不是完全不透明（像原版熔岩，也是这里每个流体的默认值）。要有实际效果，贴图本身得真的带 alpha 通道——见[「贴图生成工具」](#贴图生成工具)的 `--alpha` 参数。`false`/`null`/不填 = 不透明。 |

### 物理预设 vs. `"custom"`

`"physics": "lava"` 和 `"physics": "water"` 只是起点——它们设置的每一项（`density`、`viscosity`、`temperature`、`lightLevel`、`tickRate`、`slopeFindDistance`、`levelDecreasePerBlock`，以及五个 `can*` 布尔值）都还能单独按流体覆盖，跟以前一样。想要一个稍微重一点的熔岩，或者流得快一点的水，并不需要上 `"custom"`。

`"physics": "custom"` 则完全跳过预设：流体从一个中性、无害的基线出发（水速扩散、不能游泳/淹死/水合/灭火、水类的密度/粘度/温度/发光度），你在这条目里实际填的每个字段会叠加在上面。没填的字段就停在那个中性基线——所以一个只填了 `density` 和 `burnsEntities` 的自定义流体也是合法的，只是没有其他特殊行为。适合做那些既不像熔岩也不像水的流体——比如又重又不能游泳但不会烫伤人的"油"，或者流得很快但还能让人游泳的"强酸"。

### `texture` 字段详解

| 取值 | 实际用的是什么 |
|---|---|
| `"lava"` | 原版熔岩的静止/流动贴图，没有叠加层 |
| `"generated"` | 本 mod 自己程序化生成的动画贴图，路径是 `assets/createliquidmineral/textures/block/<id>_still.png` / `_flow.png`——也可以是你自己丢进 `config/createliquidmineral/textures/block/` 的 PNG，见[「使用你自己的贴图」](#使用你自己的贴图)。两边都没有的话会静默退回 `"default"`（不会出现缺失材质的紫黑格子） |
| `"default"` | 共享的灰白程序化贴图，既是上面的退回选项，也可以直接选它要一个朴素的外观 |
| `"water"` | 原版水的静止/流动/叠加层贴图——唯一能干净染色的贴图，因为它本身没有颜色 |
| *（其他值或不填）* | 退回到 `"default"` |

改这个文件里的数值、重启游戏，就能改流体的数值、颜色、发光度和行为——**不需要重新编译**。唯一需要小心处理的是"新增一个全新的流体 ID"，见下一节。

## 如何新增一个自定义流体

### 只改配置文件能做到什么程度

因为 mod 的注册代码（流体、流体方块、桶物品、创造模式栏条目）在游戏启动时读的就是这份 JSON，**在 `fluids.json` 里加一条新条目、重启游戏，这个流体就是完全能用的**——能放置、能舀、能被水泵运输，桶图标正常、名字也能看懂，全程不用碰 Java 代码，也不用跑任何构建工具。

图标和名字这块之所以能自动生效，是因为 mod 自带一个"生成式兜底资源包"：游戏启动时会在内存里现场给 `fluids.json` 里当前存在的每一个流体，动态造出桶的物品模型和中英文语言条目——本质上就是把 `gradlew runData` 原本要提前烘焙进 jar 里的东西，换成运行时现造。它只补缺口，如果某个流体本来就有真实的（跑过 datagen 的）资源（内置的 8 个矿物都有），那些真实资源永远优先。

不做后续步骤的话，还会缺这一样：
- **好看的自定义名字**，如果你不满足于自动生成的"拆词大写"名字（比如 `molten_titanium` → "Molten Titanium"）。见下方。
- **贴图**，如果你用的是 `"texture": "generated"`——得真的准备好 `<id>_still.png` / `<id>_flow.png`，不然会退回纯灰色（依然是能正常用的贴图，只是不是自定义的）。见下方[「使用你自己的贴图」](#使用你自己的贴图)——不需要 Java，也不需要打包资源包。

### 想要自定义名字

如果不想用自动生成的名字，两个办法：
- 做一个资源包，在 `assets/createliquidmineral/lang/zh_cn.json`/`en_us.json` 里覆盖 `item.createliquidmineral.<id>_bucket` 等 key（不需要 Java、不需要 Gradle、不需要编译）；
- 或者如果你有 mod 源码，在 `MoltenFluidNames.KNOWN` 里加一条，然后跑一次 `gradlew runData` 把名字直接烘焙进 mod 本体。

## 使用你自己的贴图

想给某个流体换个自定义外观，不需要 Java、不需要 Gradle、也不需要打包资源包——把 PNG 丢进配置文件夹就行：

```
config/createliquidmineral/textures/block/<id>_still.png
config/createliquidmineral/textures/block/<id>_flow.png
```

`<id>` 就是这个流体在 `fluids.json` 里的 `id` 字段（比如 `molten_iron`）。装了这个 mod 后第一次启动游戏，会自动创建这个文件夹（里面还带一份 `README.txt` 说明同样的用法）。

- 一个流体要**同时提供** `_still` 和 `_flow` 两个文件。
- 想要**动画**贴图，就在 PNG 旁边放一个同名的 `<文件名>.png.mcmeta`——格式跟任何 Minecraft 资源包一样，比如 `{"animation": {"frametime": 3}}`。
- 放在这里的贴图会**覆盖**这个流体原本的贴图，不管原本的贴图是 mod 自带的美术资源，还是某个只在 `fluids.json` 里新增、还没做贴图的流体所退回的纯灰色。
- **完整重启游戏后生效**（这个功能底层是用资源包的方式实现的，跟游戏里其他东西一样，只在启动时加载一次）。

这个功能和 `fluids.json` 里的 `"texture": "generated"` 是配合使用的——贴图模式还是照常选，这里只是负责提供（或替换）具体的像素内容，不需要重新构建 mod。

## 贴图生成工具

`"generated"` 贴图不是手绘的——是用 **[FluidLoom](https://github.com/HenryCROSS/FluidLoom)** 这个独立开源 Python 工具程序化画出来的。完整用法、每个 CLI 参数、以及怎么复现/重新生成这 8 个内置流体的贴图，见它的 README——如果你想画好贴图分享给别人，让他们按上面说的丢进自己的 `config/createliquidmineral/textures/` 文件夹使用，这个工具是个不错的起点。

## 已知局限

- **熔融钻石的贴图带了 alpha 通道（`alpha=0.75`），但还没标记成半透明。** 半透明渲染这个功能本身已经实现了（见上面的 `translucent` 字段），但熔融钻石的 `fluids.json` 条目默认没开启，所以现在还是完全不透明——想要那个 alpha 真的生效，自己在它的条目上加一行 `"translucent": true` 就行。
- **染色（`tint` 字段）只有在 `"water"` 贴图上效果干净。** 套在 `"lava"` 贴图上会偏暖色/偏橙，因为那张贴图本身颜色就很重。
- **桶的装水/倒水音效跟着 `physics` 预设走，没法单独覆盖。** `"lava"` 用熔岩的桶音效，`"water"`/`"custom"` 用水的桶音效，配置里没有单独字段能给某个流体挑一套不一样的音效。
- **高光/气泡贴图层是静态的，不会动。** 为了避免闪烁，这两层是每张贴图只生成一次（不是逐帧生成），所以不会跟着底下流动的噪声一起漂移。

## 常见问题

**问：我改了 `fluids.json`，但游戏里没变化。**
答：完整重启一次游戏——配置文件只在游戏启动时读取一次。

**问：我新加的流体，桶显示成缺失材质的图标。**
答：正常不应该这样——mod 会给 `fluids.json` 里任何流体（包括全新加的）自动生成兜底桶模型。先确认改完配置有没有完整重启游戏；如果重启了还是不对，那是别的问题，去看日志里有没有 `GeneratedFallbackPack` 相关的报错。

**问：`tint` 可以填任意颜色吗？**
答：可以，但只有 `"texture": "water"` 上染色效果才干净。其他贴图本身都已经带了颜色。

**问：这个 mod 必须装 Create 吗？**
答：是的，机械动力（Create）是硬性必需前置（见 `neoforge.mods.toml`）。Create: Additions & Synthetics 是可选的，只影响琥珀金那一种流体。

**问：`requiredMod` 能写多个 mod 吗？**
答：可以——`"requiredMod": ["create", "createaddition"]` 只要**任意一个**装了就会注册，不是要求两个都装。单个字符串照样能用，比如 `"requiredMod": "createaddition"`，只依赖一个 mod 的常见情况不用改写法。

**问：想临时关掉某个流体，但又不想删掉它在配置里的设置，怎么办？**
答：把那条目的 `"enabled"` 设成 `false`，重启游戏。它就完全不会被注册——跟删掉这条目效果一样——但配置文件里其他设置都还留着，以后想用回来直接改回 `true` 就行。

**问：我把 PNG 放进了 `config/createliquidmineral/textures/block/`，但流体看起来还是没变。**
答：完整重启一次游戏，并仔细核对文件名——`<id>_still.png` 和 `<id>_flow.png`，`<id>` 是这个流体在 `fluids.json` 里的 `id`（全小写，要完全一致）。两个文件都要提供：mod 只靠 `_still.png` 存不存在来判断"这个流体有没有真实贴图"，所以缺了 `_flow.png` 不会阻止静止贴图生效，但流体流动时会显示成缺失材质的紫黑格子。

## 许可协议

[GNU General Public License v3.0](LICENSE)（GPL-3.0-only）。你可以自由使用、研究、修改和再分发本 mod（包括修改版），前提是衍生作品保持同一协议、并继续公开源码。完整条款见 [LICENSE](LICENSE) 文件。
