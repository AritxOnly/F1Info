# 0.5.1 验证

构建：assembleRelease、assembleDebug、testDebugUnitTest、lintRelease 成功。30 项单元测试，0 失败/错误；lint 0 错误、43 警告（新增 Preference 依赖版本提示，其余已有警告）。核心 .kt/.java 文件无超过 1000 有效行；spec 验证通过。

版本：com.aritxonly.f1info / 0.5.1 / versionCode 10。APK 使用原 F1Info Release 证书，v2 校验通过，SHA-256 指纹见 release-signature.txt，APK 摘要见 release-apk-sha256.txt。Debug 的预览 Activity 不在 Release Manifest 中。

模拟器：仅 F1InfoChrome042 / emulator-5588，Android 35，720×1600，280dpi。八种组件采用生产编辑器与实际 Glance RemoteViews 验收，不使用仿制 Compose 预览。所有车队矢量资源均通过原生 VectorDrawable 加载，修复奔驰紧凑 arc 标志解析问题。

交互：超级岛模式下拉展开/选择 Shizuku/收起；赛季下拉选择 2025；比赛详情通过 ArrowPreference 进入结果并返回；小组件选择深色文字、透明背景、George Russell，实际预览即时更新。未执行真实 Root/Shizuku 授权或 Sponsor 登录。

字体：4×4 排名和赛程在 1.3 倍系统字号下保留单位、所有本场时间和缓存说明，列表按剩余高度减少行数。字号已恢复 1.0。

编辑页配色：直接 WidgetEditorTheme → lightColorScheme()/darkColorScheme()；SecondaryPage brandStyling=false，原生 surface 与 surfaceContainer，按钮/滑块/菜单 Defaults，完整明暗运行截图。系统明暗从 no 切到 yes 后恢复 no。没有使用 Material/Monet 色板生成。

截图：screenshots/widget-editor-miuix-light-051.png、widget-editor-miuix-dark-051.png；widget-dropdown-miuix-light-051.png、widget-dropdown-miuix-dark-051.png；八种 widget-*-051.png；widget-font-scale-130-051.png、widget-schedule-font-scale-130-051.png；live-mode-dropdown-051.png、live-season-dropdown-051.png、race-detail-layout-051.png。

小米桌面编辑桥接未在本轮进行真机验证；注册和持久化契约保持原实现。

附加回归：从积分榜打开 George Russell 详情，关注图标显示“替换关注车手”下拉入口；依次切换首页、赛程、积分榜、Live、首页，均成功。

ANR 记录：连续预览测试后冷启动积分榜出现一次 FocusEvent 输入超时。DropBox 同期记录系统 InputManagerService/ActivityManager 阻塞 17 秒、CPU pressure avg10=75.12；应用主线程为 Runnable，位于 MIUIX PullToRefresh snapshotFlow 收集，RenderThread 在正常等待。系统负载可能参与，单次堆栈不能确认唯一原因。强制停止后重新启动，同一路径和上述 Tab 回归通过，未再次出现无响应对话框。保留该限制，不将本轮结果表述为零 ANR。
