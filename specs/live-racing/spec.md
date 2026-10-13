# Live Racing
## 背景
Hero 逐帧修改整页主题造成卡顿，Hero 外层留白割裂沉浸。缺少比赛结果、Live 动态与超级岛。
## 目标
局部动画与全宽 Hero 内边距；可变宽度艺术数字；正赛/冲刺/排位结果；OpenF1 历史及授权实时动态；迁移 Deadliner Root/Shizuku 超级岛；读取 HyperModifier 抬高值；去掉 Hero 点击高亮；优化底部 Tab 切换主线程工作和共享资源。
## 非目标
购买实时账号、伪造实时数据、对用户真机执行 Root、防止 OEM 固件差异。
## 用户场景
查看已结束比赛结果；赛中打开 Live 并在后台持续看超级岛；授权 Shizuku/Root；自动跟随 HyperModifier。
## 验收标准
签名 APK、构建 lint 和解析/合并/过期/恢复测试；Hero/Live/结果模拟器运行；超级岛模板和两种后端完整集成，普通 Android 通知回退。
## 风险与约束
OpenF1 赛中需要订阅，过期 token 明示；默认 15 秒刷新并遵守 API 限额。Root/Shizuku 仅用户主动授权，保留 Deadliner 看门狗和恢复网络入口。MIUIX 原生中性色，禁止 Material。
