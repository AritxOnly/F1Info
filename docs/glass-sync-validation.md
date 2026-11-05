# 2026-10-20 柔光玻璃同步验证

- 源码/API：对照 Deadliner `5499df0` 本地工作区（包含未提交的阴影预设更新）迁移完整依赖闭包，保持目标 MIUIX 0.9.4-rc01 / Compose 1.12.0。精确来源哈希见 `deadliner-glass-source.json`。
- `./gradlew :app:assembleDebug :app:testDebugUnitTest --offline`：通过。
- 68 项单元测试，0 failures / errors / skipped；其中 23 项上游材质回归测试覆盖 Gaussian kernel + refraction 采样空间、各向异性模糊、明暗亮边、tint 合成与过渡、配方参数、浅色三层阴影与顶栏补偿。
- 上游 Robolectric 图像比较测试依赖其专用渲染测试环境，本次未迁入或声称通过；本项目使用现有 JUnit 环境验证逻辑。
- `git diff --check`：通过。
- 静态组合检查：底部 EdgeMask 和胶囊均在页面 LayerBackdrop 的兄弟层，绘制顺序为 page → EdgeMask → tabs。mask 不含手势处理，不改变导航/内容 padding；系统导航 inset 与 HyperModifier lift 同时进入覆盖高度。

- 运行/视觉：Pixel 9 / Android 36.1 模拟器，因启动时 Pixel Launcher / System UI 无响应而将分辨率降至 540×1212、density 210；随后应用正常显示。检查浅色主页有数据状态、滚动后的底部内容渐隐，以及深色赛程页切换与选中；AndroidRuntime 无崩溃记录。截图保存在本机构建目录 `app/build/outputs/glass-validation/`。
- 视觉检查限于模拟器；未验证真机、所有字号/旋转、手势动画各帧或无 shader 设备。上游 Robolectric 图像基线未运行。


# 2026-10-25 按压反馈同步验证

- 将现用依赖与上一轮来源哈希比较：仅导航的两个文件改变，其余 21 个来源文件一致。同步到 Deadliner `9119af2` + 当时工作区未提交反馈更新；本地差异仅来源注释、MIUIX 类型/语义色和 layout-phase 指示器 offset。
- `./gradlew :app:testDebugUnitTest :app:assembleRelease --offline` 通过；共 73 项测试，0 failures / errors / skipped，包括上游五项导航运动回归。
- `apksigner verify app/build/outputs/apk/release/app-release.apk` 通过。版本保持 0.5.6。
- 本轮运行/视觉：独立 Pixel 9 / Android 36.1 模拟器（540×1212 / density 210），确认选中胶囊按住时缩小，UP 后恢复尺寸；从首页拖到赛程后正确选中赛程。已保存 released / pressed / restored / dragged 截图于 `app/build/outputs/glass-validation-20261025/`。AndroidRuntime 无崩溃输出。
- 模拟器冷启动时曾出现 System UI 无响应，点击 Wait 后正常；本轮使用深色主题与已有离线缓存。未验证真机、浅色模式或逐帧弹簧时间曲线。

# 2026-11-05 阔折叠与平板发布验证

- `./gradlew :app:assembleRelease :app:testDebugUnitTest --offline --max-workers=3`：通过，75 项测试，0 failures / errors / skipped。发布版本 0.5.7，versionCode 16。
- `apksigner verify --verbose --print-certs`：通过，SHA-256 证书指纹与 `docs/release-signature.txt` 一致。
- 本轮此前使用独立 Pixel Tablet 模拟器验证平板横竖屏及 432×634dp 外屏：顶栏位置与滚动玻璃、侧栏收起后导航居中、赛程及积分榜选择无多余返回按钮、比赛结果返回、设置 Sheet 子页系统返回及关闭后保留选择、外屏海报与赛道图。
- 模拟器内容使用本地合成缓存验证布局与交互，不作为真实比赛数据验证。未覆盖实体折叠设备铰链切换和所有字体缩放组合。
