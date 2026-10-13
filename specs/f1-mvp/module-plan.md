# F1Info MVP Module Plan

## 模块拆分
- model：比赛/场次/车手/车队、下一场和时间规则。
- data：Jolpica HTTPS、JSON 解析、磁盘缓存、关注 SharedPreferences、赛道 GeoJSON。
- ui：App 状态与壳层；home、schedule、standings、components、overlays。
- ui/material、ui/navigation、ui/base：Deadliner 完整玻璃闭包、Tab 动效、Dialog/Sheet。

## 平台映射
Android 落在 app/src/main/java/com/aritxonly/f1info/{model,data,ui}；无另外两端实现，模型/仓储/呈现分层与 Deadliner 对齐。

## 文件拆分策略
业务每文件控制在 400 行以内；复制玻璃源逐文件保留。AdvancedMaterialTuning 仅去掉依赖 Deadliner AppearancePreferences 的未用入口；光学、按压、弹簧与降级保留。SettingsSection 迁移分组、圆角和布局并替换 Material 角色为 MIUIX。

## 风险点
仅录制 page content，玻璃 chrome 放在 recorder 外。公开 JSON 数据服务可能无本赛季积分，显式空态。Modal 保持 Scaffold 内。闭包保留许可与来源，文件尺寸机械验证。

## 小组件扩展
widgets/F1Widget：Glance host、响应式缓存快照、updateAll；WidgetBackground：Deadliner 渐变位图改用 MIUIX；WidgetRules：定期更新适用的近似小时倒计时。复用 data 和 model，不做桌面网络请求。

## 导航修正
ui/navigation/F1Routes 管理可保存的路由栈，MIUIX NavDisplay 负责连续深度转场和返回；AppShell 路由入口持有各自录制层。SecondaryPage 负责安全区与完整背景；StandingsContent 仅绘制图像、文字，背景由壳层统一。

## 高清图片
data/ArtworkCache 管理官方车手与赛车 URL、内存和磁盘缓存；RacingVisuals/TeamVisuals 负责异步呈现与内置原图回退，WidgetArtwork 复用已缓存车手照片。赛车不再经过手工抠图。
