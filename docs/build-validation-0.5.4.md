# F1Info 0.5.4 / 13 验证

## 实现
- 设置统一入口：比赛与数据、通知与显示、关于 F1Info。OpenF1 与超级岛独立二级页；实时页仍可进入设置。
- 关于沿用沉浸浮光，展示应用图标、自动版本号与 Oxanium 许可致谢。
- 日历范围：即将举行 / 整个赛季 / 单个大奖赛；场次：仅正赛 / 全部。可写日历 Dropdown、预览、按需权限与 ICS 导出。
- CalendarContract 批量事务仅插入或更新本应用标记事件，同一日历按赛季/轮次/场次稳定键去重。未知开始时间跳过。结束时间明确标为估算。无后台自动同步。
- 所有页面沿用 MIUIX 0.9.4-rc01、原柔光玻璃导航和 SettingsSection；16dp 两侧间距、24dp 圆角，无 Material 色板。

## 验证
- assembleRelease、assembleDebug、testDebugUnitTest、lintRelease 成功。41 项单测通过（新增 6 项：范围、未知时间、去重稳定键、缺失场次、ICS 转义/UTC/UID、UTF-8 行折叠）。
- 最终提示布局调整后再次 assembleRelease/Debug 与 lintRelease 成功。Lint 0 错误，43 条警告。
- spec 验证及核心文件1000行检查通过。
- 仅使用 F1InfoChrome042 / emulator-5558 一台模拟器；设置、OpenF1、超级岛、关于、导入页面实际渲染检查并保存截图。
- 系统授权拒绝后可重新授权；可写日历选择成功。模拟器隔离日历 F1InfoQA 首次新增6场、再次更新6场，直接查询 Provider 仍为6个事件。
- ICS 通过 Android 文档选择器保存成功，检查实际文件头、事件及 UTC 时间。
- 无可写日历的就近提示已验证。未知时间不写入、结束时间估算和手动重导入说明均展示。
- 未在物理手机写入任何日程；未实际更改 Root/Shizuku 设备白名单，保留原实现。
- 原发行证书 SHA-256：98a49b84160601567e03769ab44c7a6018a4d673f035f92a3be7dd1818902e63；apksigner 验证通过。APK 哈希见 release-apk-sha256.txt。

## 来源
- Android Calendar Provider：https://developer.android.com/identity/providers/calendar-provider
- Deadliner 设置分组、关于页与现有移植的柔光玻璃组件；保留既有源代码归属。
