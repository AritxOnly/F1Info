# 模块计划
## 模块拆分
- ui/theme：Oxanium 展示字体，保留中文系统回退。
- widgets/WidgetRules：按场次选择、缺失时间及进行中处理。
- widgets/WidgetTypography：缓存原生字体渲染，系统字号、MIUIX 前景色。
- widgets/WidgetCards：下一场次文字、车队 Logo 背景与强调数字。
## 平台映射
Android Compose 和 Glance，保持现有桌面编辑协议。
## 文件拆分策略
字体渲染独立文件，选择逻辑归入 WidgetRules，UI 保留 WidgetCards。
## 风险点
未知时间、字体缩放、RemoteViews 自定义字体兼容、背景 Logo 对比度。
