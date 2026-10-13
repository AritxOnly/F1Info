# MVP 验收记录

2026-10-11，com.aritxonly.f1info 0.1.0，Android 13+，debug 签名。

## 构建与规则
- `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` 成功。
- 10 项单元测试，0 失败：解析器 3、时间/关注/赛程规则 5、小组件规则 2。
- Lint 0 Error、23 Warning。提醒主要为依赖更新、target SDK 36、配置尺寸读取、Compose modifier 风格和 KTX 建议；完整报告在 app/build/reports/lint-results-debug.html。
- spec validator 通过；Kotlin/Java 核心文件 1000 有效行阈值检查通过。
- App 源无 androidx.compose.material、MaterialTheme、Material 配色或 MIUIX TabRow 引用。Compose 基础设施与 Glance 之外，界面主题/文本/图标/Scaffold/TopAppBar/按钮来自 MIUIX；玻璃导航、弹层和自适应按钮来自 Deadliner 闭包。

## 模拟器交互
Pixel_9 AVD，1080×2424，Asia/Shanghai。
- 实际 Jolpica 数据加载、首页 3 页关注轮播、下一场倒计时；当前返回赛季 2026、23 场比赛。
- 普通与强调内容卡片圆角均为 24dp，按最后用户要求统一；Dialog/Sheet 使用源参数。
- 三个底部 Tab：图标在上、文字在下；仅三导航，右下爱心已移除。
- 分类为原始 ImmersiveTextTabRow。MIUIX 大标题折叠、渐进顶栏模糊与圆形按钮材质随滚动显现，已截图。
- 全赛程/即将举行分类、赛道轮廓和比赛周末 Sheet；Singapore 正赛显示 10 月 11 日 20:00（设备时区），完整冲刺周末场次可见，赛道信息区可滚动。
- 车手/车队榜切换、详情 Dialog，冠军图标 TopDownloads。
- 从 Russell 详情点关注并替换 Leclerc：prefs 保存 russell,antonelli；从 Mercedes 详情直接关注：prefs 保存 mercedes。原关注设置还验证了保存并跨进程重启保持。
- 深色/浅色切换；系统栏图标随主题变化，浅色页面已截图。
- 首次无缓存时网络失败出现重试状态（初始安装验证）。最终版模拟器暂时设置不可用代理：刷新失败后明确提示显示上次保存的数据，赛程继续可用；恢复代理后刷新可恢复。
- 通过应用设置 requestPinAppWidget，系统确认添加 F1 围场 2×2 成功；桌面显示下一场、时间和三项积分。关注及主题变化触发更新，点击可打开应用。
- 测试过程中未出现 AndroidRuntime 异常或崩溃。

## 交付与限制
APK 位于 app/build/outputs/apk/debug/app-debug.apk；截图位于 docs/screenshots/。物理设备安装被系统拒绝（INSTALL_FAILED_USER_RESTRICTED），未更改手机安全设置；实际安装与交互验收在本地模拟器完成。
数据按刷新/缓存更新，不提供实时遥测；赛程完赛状态按开始后 3 小时估算。未知轮廓明确待收录，几何参考数据可能不同于现赛季。小组件使用 Deadliner 位图渐变，Glance 不具有应用内对页面的实时光学采样。公开肖像及几何来源与许可说明在 docs/UPSTREAM.md。

最终 24dp 更新后已再次构建、测试、lint、安装、截图检查；结果保持 10 tests / 0 errors。home.png、schedule.png、topbar-glass.png 为最终 24dp 版，light.png/cached-error.png 是上一验证批次（当时强调 48dp），仅作为主题与缓存状态证据。模拟器临时代理已恢复原始 null 状态，本次启动的模拟器已关闭。

## 沉浸改版 0.2.0
- 迁移 HyperModifier/Deadliner 关于页原始三层动画浮光，首页轮播红牛蓝/法拉利红已验证。页面使用连续底色，肖像渐隐；切换二级设置再返回，Leclerc 轮播位置保持。
- 车队品牌色为用户明确新增的装饰色要求，来源已保存 OpenF1 team_colour；MIUIX 保持基础语义颜色和控件体系。
- 所有 Sheet/资料弹窗入口改成 SecondaryPage；RootState 保存上级轮播/列表状态，二级页面提供自适应返回 IconButton、独立滚动和渐进顶栏。原弹层底层闭包保留供以后复用。
- 分段 Tab 移除固定材质 1f，自动使用 canonical LocalTopBarButtonMaterialProgress + LocalTopBarScrollProgress。
- assembleRelease/assembleDebug/testDebugUnitTest/lintRelease 全通过；10 tests，0 failed；Release lint 0 Error、21 Warning。
- Release 使用独立 F1Info RSA 4096-bit 密钥，PKCS12、10000 天有效期；v2 签名验证通过，证书 SHA256 为 98a49b84160601567e03769ab44c7a6018a4d673f035f92a3be7dd1818902e63。签名报告 docs/release-signature.txt；密钥和私密配置 .signing/f1info-release.p12、signing.local.properties 权限 600，已排除提交。应私密备份以保留升级能力。
- 在模拟器已卸载原 debug 包后安装签名 Release，启动真实数据成功；二次签名 Release 可覆盖安装，AndroidRuntime 无崩溃。debug 与 Release 证书不同，已有 debug 安装不能直接更新为此发布证书，需先卸载 debug。

## 小组件与渐隐改版 0.3.0
- 页面三层品牌浮光叠加向下渐变，最终过渡到 MIUIX surface；小组件渐变回 MIUIX surfaceContainer，内容圆角 24dp。
- 赛程 2×2 / 4×2 / 4×4，车手肖像 2×2 两个关注槽位，车手榜和车队榜 4×2 / 4×4 已实现。赛程完整场次、后续四站、领先者肖像或官方车队标识、前三/更长列表均通过实际 Glance RemoteViews 预览截图检查。
- 新增独立 F1InfoValidation AVD（emulator-5556），避免其他任务正在使用的 emulator-5554。Release 覆盖安装成功，真实缓存数据加载成功；桌面确认添加赛程组件，渲染赛道和正赛时间正常，轻触跳转赛程 Tab 成功。全部尺寸渲染检查在应用真实 RemoteViews 预览完成，未声称每种尺寸都在桌面逐一摆放。
- 实际滚动检查分段 Tab 与顶栏由普通材质渐进到柔光玻璃；二级比赛页面采用 MiuixIcons.ChevronBackward，安装包截图确认。
- Release 构建、13 项单元测试（0 失败）、lintRelease（0 Error、25 Warning）、规格验证、1000 行尺寸守卫通过。最终 Release APK 27.6 MiB，独立原证书 v2 签名验证通过，报告 docs/release-signature.txt。
- 检查期间 AndroidRuntime 与 F1WidgetPreview 错误日志为空。小组件说明与八种尺寸示例见 docs/WIDGETS.md。小组件使用缓存，缺失时间/赛道明确标记，不伪造数据。
- 本次独立模拟器临时代理恢复并关闭；未关闭或改动其他任务正在使用的模拟器。

## 导航、背景、小组件透明度与赛车素材 0.3.1
- 二级界面改为 MIUIX NavDisplay + MiuixDefault，独立 entry backdrop、按身份保存路由。Root 列表与首页轮播仍保留；进入车队详情和返回手势通过实际安装验证。TopAppBar 顶部自动消耗 systemBars 安全区，背景延伸至状态栏。
- 移除积分榜榜首 HeroWash 局部矩形，整页按榜首品牌色渲染连续浮光，主页面与二级页面容器外边距各 16dp。最终截图 team-cutout-final.png / team-detail-final.png。
- 11 队 Logo 与 2026 发布赛车图接入首页、榜首和资料页。最终素材从原摄影直接通过本地前景分割或手工 alpha 蒙版抠出，使用无损 WebP；不含先前 imagegen 重绘素材。正俯视来源有五队，其余保留其他摄影视角，不宣称为正俯视。素材仍是发布时车型，不承诺所有赛事升级。
- 小组件不透明度 0–100% 滑块与透明/50%/100% 快捷设置，共享 appearance 偏好并刷新五类 provider。实测两个已添加赛程组件随全局设置同步更新，0% 壁纸透出、100% 背景遮盖；重启后 100% 设置保留。文字和赛道图维持原有颜色和清晰度。截图 widget-transparent-desktop.png / widget-opaque-desktop.png。
- assembleRelease / assembleDebug / testDebugUnitTest / lintRelease 全部成功；13 项单元测试无失败，Release lint 0 Error、27 Warning（依赖版本及代码建议）。规格校验通过，最大 Kotlin 核心文件 442 有效行。
- 最终签名 APK 29.6 MiB，覆盖安装成功，v2 签名验证通过，沿用原 F1Info 证书 SHA256 98a49b84160601567e03769ab44c7a6018a4d673f035f92a3be7dd1818902e63。
- 最终签名版的设置入口与 100% 保存值再验通过，MIUIX 水平手势返回后车队榜分类和列表保持；AndroidRuntime 错误日志为空。独立模拟器已清除临时代理并关闭。

## 0.3.2 高清、标识和沉浸修正
- 当前赛季 22 位车手官方 1600×1600 图；11 队官网真实 1024px Logo；22 个官方车号图形；11 张 3392px 官方透明侧视赛车图。原始 WebP 直接内置，撤掉全部手工抠图 drawable。所有来源清单在 app/src/main/assets，未打开 Adobe。
- 全部图像使用 ArtworkCache：24MiB 内存、64MiB 磁盘、7 天有效期、URL 键、合并同图请求、失败限频、原子文件替换、离线资源回退。模拟器日志确认 portrait/antonelli 1600×1600、number 图形 1024px、car/mercedes 3392×746 下载并缓存成功。桌面车手图复用缓存并保持原渐变/尺寸处理。
- 首页 SmallTopAppBar 去除 APP 标题。主列表视口改为无水平 padding，各卡片与文字自行保留 16dp；RaceRow/StandingRow 整行可点击。Chinese 赛程按压截图确认高亮覆盖 0–1080px。
- 二级页滚动视口全屏，安全区间距进入滚动内容；设置页滚动截图确认上下区域能够延伸到系统栏与顶栏下方；导航栏关闭强制对比遮罩。MIUIX 转场保持。
- 车手榜行新增官方车号图形；车队榜行新增 Logo，Ferrari 保留黑马与盾牌细节（界面和小组件）。
- assembleRelease / testDebugUnitTest / lintRelease 成功；13 tests，0 failures；Lint 0 Error、27 Warning；最后 Logo 色彩修正后再次 assembleRelease 成功。规格校验通过，最大 Kotlin 文件 442 有效行。继续使用原证书签名。
- 截图：standings-number-icons.png、standings-team-icons.png、schedule-full-width-press.png、secondary-edge-to-edge-scroll.png。
