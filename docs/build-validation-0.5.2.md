# 0.5.2 验证

本轮仅调整小组件字体、首页积分字体和关于图标。assembleRelease、assembleDebug 成功；原 Release 证书签名通过 apksigner 验证，摘要见 release-apk-sha256.txt。版本 0.5.2 / versionCode 11。

仅启动一个 Android 35 模拟器 F1InfoChrome042 / emulator-5588。使用生产 Glance RemoteViews 检查车手 2×2、排名 4×2、车队排名 4×4、赛程 4×2 的字重和列布局。首页关于图标及积分字体截图见 screenshots/*-052.png。

HyperOS 4 真机字体替换效果仍需要在用户设备确认；本轮通过显式 sans-serif-medium 字族与 Bold 主数字对齐 Deadliner 实现，未使用 Material 色板。
