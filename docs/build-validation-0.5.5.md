# F1Info 0.5.5 / 14

关于页改为专用 `ic_about_f1info.xml` 矢量图标。背景采用 HyperModifier `ic_about_hypermodifier.xml` 原连续圆角轮廓，28×28 等比例映射到108×108，保留F1Info灰底红白标识。AboutAppHeader 同样使用专用drawable，而不交给painterResource加载adaptive-icon。

assembleRelease、assembleDebug 成功；原发行证书签名通过 apksigner 验证，最新 APK SHA-256 见 release-apk-sha256.txt。本次为图标资源展示修正，未添加重复实现的单元测试。

只启动一台 F1InfoChrome042 模拟器，实际打开关于页并确认圆角、标识与0.5.5版本显示正确。截图：screenshots/about-rounded-055.png；验证后已关闭模拟器。
