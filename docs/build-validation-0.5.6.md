# F1Info 0.5.6 / 15

## 实现
- 右上角使用 MiuixIcons.Settings，保留自适应柔光玻璃按钮。
- 小组件外层先预留上下边距，再绘制背景卡片与内容；固定12dp内容内边距。
- 预设：默认0/0、4×6为16/2、4×7无字12/0、4×6无字18/2；另有自定义。
- 自定义遵循Deadliner范围：单侧0–28dp、合计≤40dp；背景圆角8–32dp，默认22dp。
- 每个实例保存预设、圆角及自定义草稿；未设置新预设的旧配置回退默认，旧内边距数值作为自定义草稿保留。
- 真实RemoteViews预览不再固定裁切24dp圆角，使用矩形边界；背景透明度与文字模式继续独立。
- 4×2赛程剩余空间较少时省略重复的顶部时间、压缩行距，保持完整场次和时间。

## 验证
assembleRelease、assembleDebug、testDebugUnitTest、lintRelease成功；45项单测通过，Lint0错误44警告。新增4项测试覆盖预设数值、自定义合计限制/圆角界限、切换保留草稿、旧/未知预设回退。

只使用一台F1InfoChrome042模拟器：真实RemoteViews2×2卡片与MIUIX编辑页；Dropdown所有选项与预设数值；4×2 18/2留白、8dp圆角与全部五场赛程；4×4最大28/12留白与32dp圆角的积分榜实际渲染。截图见screenshots/widget-card-wide-margin-radius8-056.png、widget-card-large-custom-radius32-056.png、widget-custom-editor-056.png。构建后核对主页设置按钮。

spec检查及1000行核心文件检查通过；原发行证书apksigner验证通过，哈希见release-apk-sha256.txt。未启动第二个模拟器。
