# 0.5.3 验证

版本 0.5.3 / versionCode 12，包名 com.aritxonly.f1info。assembleRelease、assembleDebug、testDebugUnitTest、lintRelease 成功；35 项单元测试，0 失败/错误；lint 0 错误、44 警告。最后字体绘制修正后再次 assembleRelease/assembleDebug 成功。原 Release 证书签名经 apksigner 验证，证书与 APK 摘要见 release-signature.txt、release-apk-sha256.txt。APK 包含 Oxanium 字体及完整 OFL 许可。

场次规则覆盖一练优先、进行中保留/结束后跳转、冲刺场次、未知时间、赛季结束与空场次列表。未改变首页下一大奖赛规则。

字体使用真实 Oxanium 字形；小组件局部强调数字通过 ResourcesCompat 加载原字体，在 Paint 上应用 wght=650，并缓存渲染结果。保留系统字号、自适应宽度与可访问性积分描述，菜单/正文维持 MIUIX。

模拟器资源检查发现过去遗留的 F1InfoValidation 实例，已关闭；当前仅 F1InfoChrome042 / emulator-5588。重启当前实例并等待首次资源加载完成后复核。未修改连接的物理手机。HyperOS 4 真机效果未在本轮验证。

实际预览已确认：车手 2×2 的 Oxanium 650 数字和 Red Bull 淡色 Logo；车队排名 4×4 的重点数字、列表积分与奔驰淡色 Logo；赛程 2×2 显示练习赛 1 及其本地时间。4×2 添加最近场次摘要后，压缩行间距以容纳包括正赛在内的完整安排。

最终截图复核通过：home-oxanium-053.png 为已加载的首页，显示实际 Oxanium 积分/背景艺术字/ROUND；widget-weekend-session-053.png 包含练习 1/2/3、排位和正赛全部五场；widget-next-session-053.png、widget-driver-oxanium-logo-053.png、widget-team-oxanium-logo-053.png 均为已完成渲染的真实 RemoteViews。原生小组件 Canvas 使用 Paint.fontVariationSettings 应用字重轴，避免 Typeface.Builder 的系统字体回退。未发现应用崩溃日志；等待的启动/渲染截图已用最终画面覆盖。验证结束关闭当前模拟器。
