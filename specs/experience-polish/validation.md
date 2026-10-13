# 验证记录

2026-10-11，F1Info 0.4.0 / versionCode 6。

- assembleRelease、testDebugUnitTest、lintRelease 成功；16 项单元测试，0 失败。新增不透明度及上下间距边界、NaN 与全透明测试。
- 规格校验通过，最大 Kotlin 文件 442 有效行。
- APK Signature Scheme v2 验证通过，证书保持原 SHA-256 98a49b84160601567e03769ab44c7a6018a4d673f035f92a3be7dd1818902e63。
- 独立模拟器 F1InfoValidation / 5556：8 个 home_screen provider 的 2×2、4×2、4×4 元数据及系统注册核对通过。旧车手 receiver 仅 keyguard 分类，保留历史实例。
- 实际 Glance 编辑预览正常，#4 透明度拖动后保存并重进显示 35%，#2 保持 100%，验证实例隔离。
- 首页保留中性 MIUIX 表面/正文；积分榜大矢量 Mercedes Logo 在车手图后方，圆角、渐变与 16dp 边距视觉检查通过。关于页只有应用说明；编辑页的已安装实例与完整尺寸入口已通过 UI 控件核对。
- 软件 GPU 初次模拟器出现 WorkManager onStopJob ANR；host GPU 重启后运行检查未复现。未获得完整中断前 bugreport，因此未将初次 ANR 认定为应用代码根因。
- 小米 miuiEditUri 来自 Deadliner 标准桥接；真机桌面列项和专属编辑入口仍需小米设备验收。
- 八支车队矢量 Logo；Haas/Williams/Racing Bulls 保留官方位图。

- 用户明确禁止 Material 色板生成后，已移除全部 Monet、keyColor、ThemePaletteStyle、seed-generator 调用；只使用 MIUIX 原生颜色与显式品牌强调色覆盖。源代码审计未发现 Material UI 或色板 API 引用。重新构建、16 项测试、lint 和签名验证通过。

- 下拉刷新在赛程顶部手势验证，完成后更新时间为 23:22；前台重进自动刷新正常。最终硬件 GPU 日志未发现应用 ANR、FATAL EXCEPTION 或 Glance 预览错误。

- 赛程 4×4 实际 Glance 预览通过：左上赛道、右上全部五场次、下方未来四站，未发现裁切。品牌强调控件为原生 MIUIX 颜色覆盖的法拉利红，组件本体使用中性灰与中性文字。

- 车手榜 4×4 实际 Glance 预览通过：左上领先车手、右上积分、下方排名列表，文字和底色保持中性。

## 0.4.1（2026-10-12）
- Release / Debug 构建完成；18 项单元测试通过（新增素材 ID 别名回归）；lint 0 errors。
- 原证书签名，SHA-256 98a49b84160601567e03769ab44c7a6018a4d673f035f92a3be7dd1818902e63；APK 36,077,063 bytes。
- 真实 Jolpica drivers 数据确认 arvid_lindblad；别名仅在素材边界解析，首页、详情、缓存与小组件离线回退统一。角田官方 2025 红牛肖像与 22 车号已补入 CDN 源清单与离线资源；高清原图视觉检查完成。
- Red Bull Racing / Alpine SVG 从 Formula 1 官方队伍素材获取，逐路径比对 Android VectorDrawable 完整保留，主应用及小组件共用同一标识。
- 首页 MIUIX 主色角色从当前动画浮光计算；2026 SEASON / 数据状态赛季 / ROUND / 倒计时同用该语义色，原生中性表面不变。无 Material/Monet 生成或组件导入。
- 编辑围场入口、对应内容与顶部刷新图标删除；保留关于、详情关注、前台与下拉刷新、桌面每实例小组件编辑。
- 模拟器截图确认林德布拉德肖像、赛季与 ROUND 蓝色一致、顶部只剩关于；Red Bull 官方队标在详情 Hero 显示。模拟器网络不可用，采用本次真实 API 缓存；后续该模拟器被其他任务切到 Deadliner，停止交互，未进一步完成角田详情及完整轮播手势验收，未关闭正在被使用的模拟器。

## 0.4.2（2026-10-12）
- Release 构建、18 项单元测试和 lint 通过（0 errors）；原证书签名，APK 36,079,295 bytes。
- 底栏额外底间距由 14dp 改为 Deadliner 4dp，navigationBarsPadding 消费一次；上侧 12dp，列表底部预留=导航 inset + 胶囊 54dp + 间距 4dp + 阅读留白 24dp。HyperModifier 设置主页上游为额外 8dp，二者证据路径记录于 UPSTREAM.md。
- 三页 Hero 都保留名称两行，以同一 MIUIX title2/body2、单行副标题和同一统计字体布局；支持字体缩放，未用固定像素高度截断内容。
- 独立验证 AVD F1InfoChrome042，port 5588，720×1600 / 280dpi。真实在线刷新成功；依次滑动 Max Verstappen / Charles Leclerc / Ferrari，三页下方“下一个比赛周末” bounds 全部为 [28,1092][273,1143]，无跳动；赛季/ROUND 色随蓝→红浮光改变。截图 docs/screenshots/home-hero-[1-3]-042.png。
- 原创启动器矢量含 MIUIX 深灰中性底、白 F、赛车红 1；adaptive foreground/background/monochrome 已打包，1024/256 图标渲染检查完成。
