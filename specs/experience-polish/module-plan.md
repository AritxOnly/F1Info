# Module plan
## 模块拆分
UI shell 管理刷新/导航/语义主色。Hero 负责连续混色、类型和标识层。Widget store、editor、catalog、render 分离。
## 平台映射
Android Compose MIUIX 与 Glance；标准配置 Activity 加 Deadliner 小米编辑 URI，无其他平台新增。
## 文件拆分策略
按职责拆分，核心文件低于 1000 有效行；About、编辑、预览、存储各自独立，保留柔光玻璃依赖闭包。
## 风险点
Glance ID 使用公开 Manager API；更新对旧实例兼容；独立 provider 解决桌面尺寸发现；透明文字需要用户选择对比。
