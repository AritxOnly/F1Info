# 模块计划
## 模块拆分
ui/components 负责详情与统一设置层级；ui/racing 负责场次与授权；widgets 分离配置和 Glance 排版。
## 平台映射
Android Compose MIUIX Preference 与 Glance RemoteViews；保留 Deadliner 小米编辑入口。
## 文件拆分策略
每个职责独立文件，核心文件低于 1000 有效行。
## 风险点
下拉选择应保持原业务状态和关闭行为，设置持久化不变；透明背景与缩放文字需要可读性检查。
