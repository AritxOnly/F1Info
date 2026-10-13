# 验证记录

版本：0.5.0 / 9；包名 com.aritxonly.f1info。

## 构建与静态检查
- :app:assembleRelease / :app:assembleDebug / :app:testDebugUnitTest / :app:lintRelease。
- 30 项单元测试：原有18项 + 比赛模型/解析/缓存9项 + 迁移的特权网络恢复3项。
- MIUIX Scaffold / TopAppBar / IconButton / TextField / PullToRefresh；NavTransitions.MiuixDefault；无 Material 组件或颜色生成器。
- 所有新文件 <1000 有效行；新增 data/racing、ui/racing、notification、platform 独立职责。
- 字体 OFL 许可保留；签名使用既有 F1Info Release 密钥。

## 数据与运行
- docs/live-api-check 保存 OpenF1 latest session 与 Jolpica 2026 Round17 正赛、排位赛真实响应。
- 单一模拟器 emulator-5588，Android35，720×1600；检查首页、四个 Tab、Live 历史/授权/场次、结果分类、返回转场及无 AndroidRuntime 崩溃。
- 超级岛 Root / Shizuku 均完成代码集成、网络恢复单元测试；模拟器非 Xiaomi，不具备两种授权及 OEM 超级岛显示条件，真机视觉仍需验收。无 Sponsor 账号，未声称实际赛中端到端测试通过。

## 性能与签名
- 连续切换 12 次，预热四页，单一 Android35 模拟器、相同 0.7 秒间隔：95 分位 65→48ms，99 分位 200→93ms，janky 4.72%→3.65%。90 分位 32→34ms；仅一次样本，属于模拟器方向性验证，不代表真机/稳定 60fps。
- Hero 新版连续滑动：95 分位 48ms、99 分位 81ms；早期上一版样本 81/400ms 包含冷启动/编译负载差异，不作为严格对照结论。
- 原因修正：底栏指示器从 composition 读取动画值改为 layout lambda；所有栅格图统一内存即取、后台离线解码/官方缓存；赛道 JSON 后台只解析一次；PagerState 提升保存；轮询不触发刷新控件的内容位移；Hero 去掉点击指示。
- 首次结果页运行曾触发 RenderNode 自采样循环，已把结果分类移动到顶栏、内容内模式切换使用 canonical 无采样回退；修正后重复导航和切换结果分类，crash 缓冲区为空。
- Release 原证书 SHA256：98a49b84160601567e03769ab44c7a6018a4d673f035f92a3be7dd1818902e63；最终签名核验见 docs/release-signature.txt。

- Live 列表优先显示最新已举行场次，未来场次随后；排位 gap 数组取最后完成分段，RT 名次保留为未分类且缓存可往返。

最终运行：Marina Bay Qualifying 历史场次成功加载；crash 缓冲区 0 字节。最终 30 项测试均通过，lint 0 errors / 42 warnings，未对用户真机执行任何特权命令。
