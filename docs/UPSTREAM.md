# UI and data provenance

Deadliner source: `/Users/aritxonly/Codes/Android/Deadliner`, HEAD `210e4a2d4c6bf7df291b6c6852d39366bc1fee17`. Local source files were copied from its working checkout. Complete required glass implementation, optical shaders, material specifications, press/shadow helpers, navigation motion/shadow, dialog/sheet surface policy, modal backdrop host and material tuning were migrated. Package renamed to `com.aritxonly.f1info`. Material theme/text/icon references were replaced with MIUIX; unused AppearancePreferences mutation entry points were removed from AdvancedMaterialTuning. No optical constants or shader behavior were simplified. SettingsSection was extracted from SettingsComponents and preserves its layout/shape grouping, with MIUIX semantic roles. Original OpenNote attribution retained.

MIUIX: `0.9.4-rc01`, source commit `4a6b750b578880146876e4ab77097d9b01702413`. Verified source for theme/controller/color roles, Scaffold popup/insets, Text/Icon, TabRow, Button, Divider and Card; docs/demo Card, Scaffold and integrated SettingsPage. Original snapshot downloaded to `/tmp/f1info-miuix-source` for API inspection. https://github.com/compose-miuix-ui/miuix/tree/4a6b750b578880146876e4ab77097d9b01702413

Jolpica: https://api.jolpi.ca/ergast/f1/ ; one current season resolved from races, then all tables requested by explicit season. No fabricated fallback championship. Last successfully parsed response is cached atomically. Documentation: https://github.com/jolpica/jolpica-f1/tree/main/docs

Circuit geometry: https://github.com/bacinger/f1-circuits ; bundled `f1-circuits.geojson`, retrieved 2026-10-11. MIT license copied to docs/f1-circuits-LICENSE.md. Reference lengths and first GP year come from this dataset, may differ from the current season. Unknown tracks show an explicit missing-outline state. No synthetic circuit paths are substituted.

Portraits: public Formula 1 CDN URLs supplied by `https://api.openf1.org/v1/drivers?session_key=latest` on 2026-10-11. Exact URLs retained in `app/src/main/assets/portrait-sources.json`. Portraits remain the property of their original rights holders; this does not grant rights for commercial distribution. No OpenF1 runtime dependency. Official F1 branding, commercial features, and BoxBox assets were not copied.

Widget adaptation: Deadliner `widgets/glance/ui/CompactTaskGlanceWidget.kt`, `TaskWidgetComponents.kt`, `TaskWidgetLayoutMetrics.kt`, `theme/TaskWidgetBackgroundRenderer.kt`. Exact sizing, layered 0.92-alpha background, 12dp padding, compact header and three-row structure retained. F1 domain replaces task rows; gradient roles come directly from MIUIX. Glance cannot sample the launcher's wallpaper with the Compose page backdrop, so the canonical widget bitmap recipe is used. Update period 30 minutes; app refresh, favorites and appearance changes trigger updates. No widget network requests or live-data claims.

Latest UI alignment: canonical Deadliner TopBarIconButton and TopBarMaterialProgress migrated (fallback MIUIX IconButton). Reveal begins at collapsed fraction 0.015 across 0.18, smoothstep and 280ms easing retained. ImmersiveTextTabRow migrated for all categories; floating navigation uses Stacked. Exact SettingsSection radius resolved against Deadliner res/values/dimens.xml: 24dp ordinary, 48dp main. MIUIX Scaffold and TopAppBar own insets/scroll; page surface and card surfaceContainer use MIUIX roles. System bar icons follow active MIUIX brightness. Detached favorites button removed; detail IconButton saves team directly or presents two driver replacement choices, preserving 2+1.

User override: all content cards now use the Deadliner ordinary 24dp radius, including emphasis cards; original main-content 48dp is intentionally superseded. Modal and capsule shapes retain canonical parameters.

Immersive revision 0.2.0: HyperModifier source f829eb3fdbee257324fde896c697a0adb0b2638d, ui/SystemFeatureSettingsPages.kt AboutFloatingBackground + toVividGlowColor migrated as RacingFloatingBackground, keeping three motion periods (7500/5600/6400ms), layer geometry, alpha and HSL vivid conversion. Primary/secondary ambient hues replaced with active team brand color per user request; semantic surface and tertiary remain MIUIX. Local snapshot OpenF1 team_colour is recorded in assets/team-colors.json; colors are decorative and not replacements for MIUIX component roles. Home uses one continuous page backdrop, not a rectangular glow inside each pager item; 650ms brand transition follows page changes. Classification tabs use canonical implicit progress, without forced 1f. Active secondary routes replace the primary scaffold, with scroll/backdrop host and adaptive Back IconButton; no sheets/dialogs in feature flows. Root saveable-state holder restores pager and list context when returning.

Widget revision 0.3.0: schedule (2×2/4×2/4×4), two followed-driver portrait providers (2×2), and driver/team rankings (4×2/4×4) use SizeMode.Exact with dp thresholds rather than assuming launcher cell pixels. WidgetArtwork projects the same f1-circuits geometry and renders small bitmaps; unknown geometry remains explicit. Team identifiers from the official https://www.formula1.com/en/teams page were downloaded as unmodified CDN WebP resources, URLs recorded in assets/team-logo-sources.json. Widget renderer tints monochrome logo silhouettes to the active MIUIX foreground. Portraits remain bundled official images. Artwork/color ownership remains with original rights holders. Glance RemoteViews previews use the official 1.2.0 compose API (source AppWidgetComposer.kt) at actual layout sizes, not approximate Compose mockups. Tiles open schedule/standings categories via explicit activity intent. All five providers update from shared cache/favorites/appearance revision, with no additional widget network requests. The lower ambient page now blends down to MIUIX surface.

## 0.3.1 导航与车队图像
- MIUIX miuix-nav-android 0.9.4-rc01：NavDisplay、NavTransitions.MiuixDefault、NavDisplayEffects、LeftToRight 手势 API 按 4a6b750b 源码验证。该 Android artifact 的 inline DSL 编译为 JVM 21，因此 app 编译目标提高为 21。导航保存的是 Bundle 支持的字符串路由，不依赖序列化插件；详情按实体 ID 恢复。
- 进入/退出期间各 entry 独立 LayerBackdrop，背景不包含自身玻璃，避免交叠录制循环。
- 2026 车队摄影来自 Formula1.com 各队发布图集，逐项 URL / season / angle 保存于 app/src/main/assets/team-car-sources.json。严格垂直俯视：Mercedes、Ferrari、Red Bull、Racing Bulls、Cadillac；其余采用非正俯视发布摄影，未称其为严格垂直俯视。最终使用 macOS Vision 本地前景分割及手工 alpha 蒙版，从原摄影直接抠出赛车像素；复杂图的蒙版存档于 assets/car-cutout-sources，原摄影于 assets/car-originals。先前 imagegen 试作因改变像素及残留背景而未纳入最终资源；未打开 Adobe。页面不再使用四周羽化遮罩。最终素材使用无损 WebP 保存原摄影颜色及透明度，不能视为工程资料。素材归原权利方所有，非开源资产。官网发布时部分为涂装展示车，不保证赛事过程中每次技术升级或特殊涂装。
- MIUIX Slider 背景不透明度设置仅改变 Glance 背景 Image.alpha，内容保持清晰；设置保存后刷新全部五个 widget provider。

## 0.3.2 官方原图与缓存（替代上节赛车抠图方案）
- 按用户要求，撤掉全部手工抠图资源。2026 赛车直接使用 F1 官网各队页面的 common/f1/2026/<team>/2026<team>carright.webp，原生透明背景、3392px 宽、侧视图，未进行背景移除或二次生成。精确 URL 记录于 team-car-sources.json。此前抠图文件仅作为历史研发记录，不再打包或使用。
- OpenF1 driver.headshot_url 提供车手图片地址（https://openf1.org/docs/#drivers），其常见 transform 链接属于低清缩略图；移除 transform 后核验真实原图 840px。两条旧路径失效时改用 F1 官方 2026 个人页面照片的 CDN 上半身裁切，未抠图。
- ArtworkCache 负责官方 CDN 图像：24MiB 内存、64MiB 磁盘，7 天有效期、URL SHA256 键、并发请求合并、失败限频、响应大小限制、原子缓存文件替换。断网继续显示缓存或 APK 内高清原图；无需额外 API key。桌面车手图复用缓存，仍不发起网络请求。
- SecondaryPage 的滚动视口覆盖完整窗口；安全间距放在 LazyColumn.contentPadding / Column.verticalScroll 后的内容 padding；关闭系统导航栏对比遮罩。首页使用 MIUIX SmallTopAppBar，只保留玻璃图标按钮。

追加高清标识：全部 22 位车手改用 2026 官方个人页 1600×1600 上半身 CDN 照片；不做背景移除。旧头像仍可通过 OpenF1 获取，但不再依赖其低分辨率链接。11 支车队 Logo 从当前官网队伍页解析真实资源，1024px（部分品牌沿用官网 2025 素材地址）。22 个车号图形来自个人页 numberwhite.webp，1024px 级；清单 driver-number-sources.json，积分榜行与车手资料展示官方车号图形。人物/Logo/车号/赛车统一缓存。


0.4.0 polish: canonical Deadliner TaskWidgetConfigurationActivity/TaskWidgetHostEditor per-instance editor, standard configure/reconfigure and Xiaomi miuiEditUri migrated. Separate initial-size providers preserve old receiver classes/instances; legacy second portrait entry uses keyguard category to remove the duplicate home picker item. Glance IDs use the public GlanceAppWidgetManager API. Neutral widget MIUIX surfaces and Medium/Normal typography; foreground contrast explicitly configurable. Widget previews compose the real Glance renderer. MIUIX PullToRefresh signature verified at the pinned source; LifecycleStartEffect refreshes foreground entries with existing request deduplication. MIUIX original lightColorScheme/darkColorScheme supplies all surface/text roles. Team colors directly override the verified MIUIX accent roles; a hue-preserving lightness adjustment checks readability. No Monet/seed/Material palette generation is used. Hero watermark logos rendered behind unmodified official art. Floating gradients end at their actual drawn circle radius and use eased drift; pager accent interpolates continuously.

Vector logos: pinned Simple Icons 16.0.0 (Ferrari, McLaren, Audi, Aston Martin, Cadillac), 13.0.0 (Mercedes), CC0 library https://github.com/simple-icons/simple-icons; brand marks remain their owners’ property. Red Bull Racing and Alpine use the official Formula 1 SVGs linked in vector-logo-sources.json. Paths are converted losslessly to Android vector drawable coordinates; no tracing, bitmap upscale or AI reconstruction. Exact source map: app/src/main/assets/vector-logo-sources.json. Haas/Williams/Racing Bulls keep original Formula 1 artwork fallback.

### 0.4.1 标识修正
RBR 与 Alpine 改为 F1 官网队伍页所使用的官方 SVG（来源记录在 vector-logo-sources.json），保留原路径与 96×96 视口，替代之前的 Red Bull 饮料公司图标和 Alpine/BWT 页脚组合标识。角田使用官方 2025 RBR 肖像与车号，原图未抠图；林德布拉德通过素材边界别名 arvid_lindblad→lindblad 匹配，数据与关注 ID 不改动。

### 0.4.2 底栏和图标
Deadliner 210e4a2 的 ImmersiveMiuixCompactNavigationScaffold.kt：FloatingNavigationBottomSpacing=4dp，保留 navigationBarsPadding 一次；HyperModifier f829eb3 设置主页为额外 8dp。F1Info 从 14dp 调整至 Deadliner 4dp，顶侧预留 12dp。原创启动器矢量母版保存在 assets/launcher；背景 #242424 与前景 #F2F2F2 取自 MIUIX darkColorScheme surfaceContainer/onSurface，红色 #ED1131 是现用 Ferrari 品牌强调色，非 Material 生成。自适应背景、前景与 monochrome 分层，最终圆角由桌面 mask 决定。

## 0.5.0 比赛动态与性能
- OpenF1 历史/实时契约：https://openf1.org/docs/ ，授权 https://openf1.org/auth.html 。历史（2023+）免费，赛中实时接口需要 Sponsor；15 秒 HTTP 轮询，数据采样超过 60 秒明确标为过期，完成比赛明确为历史。位置、间隔、赛会消息采用重叠增量去重；历史最终排名采用 session_result。结果来自 Jolpica results/qualifying/sprint，未公布即为空；结果和最近动态保存离线缓存。
- 凭据只在本机输入，Android Keystore AES/GCM 加密存储；云备份与设备迁移排除 live_secrets.xml。令牌过期自动续期，错误正文与账号密码不写入日志。
- Roboto Flex 官方 Google Fonts 可变字体，wdth=140、wght=400/600；本地 res/font/racing_flex.ttf，OFL 许可 assets/fonts/RobotoFlex-OFL.txt；用于数字和 Latin 艺术字，中文回退系统 MIUIX 字体。来源 https://github.com/google/fonts/tree/main/ofl/robotoflex 。
- 完整迁移 Deadliner 210e4a2 的 IslandWhitelistBypass / IslandFirewallWindow / IslandPrivilegedServices 和 AIDL 后端；XiaomiIslandExtras 通用模板 closure 移植，业务图标/文字改为比赛动态。保留 Root、Shizuku、持久恢复记录、100ms 网络窗口、5s 特权看门狗、恢复网络入口。RootService 由 libsu app_process 启动，Manifest 中仅该声明抑制 Instantiatable 的误报。常规通知权限与前台 dataSync 服务必须由用户点击开启，比赛结束/停止关注/超时恢复网络并停止服务。
- HyperModifier f829eb3 公开 ContentProvider：content://com.aritxonly.myhypermodifier.settings，get_settings，Bundle Float hyper_glassify_hidden_navigation_lift（0–48dp），恢复应用时读取，缺失回退 0dp；同步调整底栏和内容避让。
- Hero 底图和点击区域无外边距，文字内边距 16dp；pager 预加载三页，settledPage 只改变一次目标色，380ms 局部动画。CompositionLocal 传颜色 State，页级 MIUIX theme 不逐帧更新。浮光径向 Brush 在 drawWithCache 中缓存，漂移在绘制层执行，避免整页重组和每帧创建着色器。

- 后续用户调整：Live 使用 MiuixIcons.Promotions；Hero 保留 Role.Button 和点击导航但 indication=null。底部 Tab 指示器偏移改为 layout-phase lambda，弹簧/拖拽/选中规则保持 Deadliner 原样。肖像/赛车/车号位图不再在主线程使用 painterResource 解码，rememberRacingArtwork 先读取内存，后台解码高清离线图并 prepareToDraw，再更新官方在线缓存；赛道 GeoJSON 只解析一次且运行在 IO，共享几何。Hero PagerState 提升到 AppShell，离开/返回保留当前页。Live 后台轮询不再驱动 PullToRefresh 的布局位移，仅手动下拉显示刷新手势状态。
- 运行检查修复：结果分类 Tab 移到 SecondaryPage 顶栏，与 LayerBackdrop 录制内容保持兄弟关系；授权页内容内模式切换明确使用无采样的 canonical fallback，避免 RenderNode 自采样循环。

## 0.5.1 组件与排版
官方入口：https://compose-miuix-ui.github.io/miuix/zh_CN/components/ 。仍固定 MIUIX 0.9.4-rc01 / 4a6b750b，新增同版本 miuix-preference-android。对照 docs/zh_CN/components/overlaydropdownpreference.md、arrowpreference.md、docs/demo/OverlayDropdownPreferenceDemo.kt、example/shared/component/DropdownSection.kt / SettingsPage.kt，以及各 Preference 实际签名。选项使用 OverlayDropdownPreference，导航使用 ArrowPreference，二元状态使用 SwitchPreference，连续值使用 SliderPreference；使用既有 Scaffold 弹出宿主，不新建主题或自采样玻璃层。

SettingsSection 继续保持 Deadliner 24dp、半透明 MIUIX surfaceContainer 与 16dp 页面边距；SectionTitle 使用 MIUIX subtitle 语义。首页整张赛程卡承载点击，内部重复按钮改为标题与箭头。保存/验证/重试仍为 MIUIX TextButton。

Glance 用系统 Normal / Medium，数字与名称分层，保留中性 MIUIX 色板。奔驰 SVG 转 VectorDrawable 的紧凑 arc 标志补充分隔符；几何不变，Android 原生解析兼容。Debug-only WidgetLayoutPreviewActivity 用生产编辑器和实际 Glance RemoteViews，启动时验证全部队标 VectorDrawable 原生加载；不进入 Release Manifest。

字体缩放复核：Glance 4×4 的固定 100dp 领先者区改为随 Android fontScale 增高；排名/后续赛程根据剩余高度计算数量，避免缩放时裁掉单位或缓存说明。所有新增表面仍是 MIUIX 明暗语义颜色，无 Material 色板。

小组件编辑页单独使用未经参数覆盖的 MIUIX lightColorScheme()/darkColorScheme()，随系统明暗切换。SecondaryPage 的品牌样式在该页关闭：不执行 miuixBrandColors，不绘制品牌浮光，底色为原生 surface；编辑分组用完整不透明的 surfaceContainer。Slider/Dropdown/TextButton 沿用库 Defaults，所以 onPrimary、滑块拇指、禁用和菜单选中配色均保持官方值；不调用 Material/Monet 色板生成。

### 0.5.2 字体微调

- 小组件参考 Deadliner `widgets/glance/ui/TaskWidgetTypography.kt`、`TaskWidgetComponents.kt`、`CapsuleTaskGlanceWidget.kt`：显式 `sans-serif-medium` 字族，重点积分和标题使用 Bold，其他文字 Normal，避免仅依靠系统默认字体。
- 首页积分单独使用 RacingScoreFamily，Roboto Flex wdth=151、wght=650，72sp；原 RacingDisplayFamily 保留。
- 右上角关于入口使用匹配 0.9.4-rc01 源码的 MiuixIcons.Info，保留自适应柔光玻璃按钮与原导航。

### 0.5.3 展示字体与场次小组件

- Oxanium 官方 Google Fonts 字体：https://github.com/google/fonts/tree/main/ofl/oxanium ，OFL 许可：app/src/main/assets/fonts/Oxanium-OFL.txt。原字体存入 res/font/racing_oxanium.ttf，正常 400、强调 600/650；RacingDisplayFamily/RacingScoreFamily 使用真实字体字形，不使用横向缩放。
- 用于首页积分/背景字/ROUND/倒计时、赛程日期、积分榜重点数字、详情页排名/积分/胜场/号码，以及已有 Live/结果重点数字；菜单和正文保留 MIUIX 字体。
- Glance 自定义字体强调数字采用 WidgetDisplayNumbers 的缓存位图，使用同一字体资源、MIUIX 前景角色，遵循系统字号并提供可访问性积分描述。其余文字延续 Deadliner 字体层级。
- 赛程小组件按下一练习/排位/冲刺/正赛选择，未知时间只显示日期和待公布。进行中保留一个粗略场次窗口，不作为实时赛况断言。积分组件复用 vectorLogos/WidgetArtwork 的车队 Logo，以 10% 透明度置于内容背后。

0.5.5 关于图标：采用 HyperModifier `ui/SystemFeatureSettingsPages.kt` 的 AboutAppHeader 专用矢量方案，沿用 `res/drawable/ic_about_hypermodifier.xml` 背景连续圆角路径（28×28），等比例映射到 F1Info 108×108 矢量。F1Info 原品牌颜色与标识保留，新增 `ic_about_f1info.xml`，不修改桌面 adaptive-icon。

0.5.6 小组件外边距：参考 Deadliner `widgets/glance/preferences/TaskWidgetPreferences.kt` 的预设语义、`TaskWidgetConfiguration.kt` 的8–32dp圆角、单侧28dp/合计40dp限制，以及 `ui/CompactTaskGlanceWidget.kt` 外层留白 + `TaskWidgetComponents.kt` 背景Image圆角分层。F1Info新增用户指定的4×6无字18/2预设；未设置新版预设的旧内容padding不直接迁移为背景外边距，自定义草稿保留。实际RemoteViews预览使用矩形裁切边界，不用固定圆角掩盖背景自定义值。
