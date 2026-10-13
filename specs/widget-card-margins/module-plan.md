# 模块计划
## 模块拆分
- WidgetConfiguration：预设、边距解析、规范化、每实例存储。
- WidgetCards：外层边距、卡片背景圆角、固定内部间距。
- WidgetConfigurationActivity / WidgetLivePreview：MIUIX Dropdown和Slider；真实卡片预览。
- F1App：Settings图标。
## 平台映射
沿用Deadliner Glance TaskWidgetPreferences / TaskWidgetConfiguration / CompactTaskGlanceWidget分层契约。
## 文件拆分策略
复用现有小文件；预设与配置模型共同管理，不增加大型组件。
## 风险点
小尺寸内容高度、旧配置兼容、预览外壳不能遮盖自定义圆角。
