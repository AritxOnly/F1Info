# 模块计划
## 模块拆分
ui/home 局部动画；ui/theme 可变字体；data/racing 结果、OpenF1、缓存和鉴权；ui/racing 结果/Live/设置；notification 独立前台服务与 Deadliner 超级岛完整后端；platform HyperModifier 读取。
## 平台映射
Android Compose/MIUIX；Xiaomi 35+ 超级岛，Android 通知回退。
## 文件拆分策略
网络、模型、解析、服务、模板与设置独立文件，任何文件不超过 1000 有效行。
## 风险点
HTTP 节流、取消、令牌过期、断网旧数据、OEM 权限与特权网络规则自动恢复。
