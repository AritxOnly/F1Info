# F1Info 排版与组件选择优化
## 背景
选择、导航和提交操作混用 TextButton，页面出现按钮堆叠，小组件标题与数字层级不够清晰。
## 目标
按 MIUIX 0.9.4-rc01 规范使用 DropdownPreference、ArrowPreference、SliderPreference、SwitchPreference；统一小组件的正常/中等字重、列对齐与留白。编辑页直接采用 MIUIX 原生明暗色板，不继承车队品牌覆盖或浮光。
## 非目标
修改数据源、柔光玻璃导航、授权协议和小组件注册规格。
## 用户场景
用户选择历史赛季、配置超级岛与小组件，在比赛详情进入结果或 Live；桌面快速查看赛程和积分。
## 验收标准
设置/编辑用原生 MIUIX 选项行，保留主要提交按钮；小组件各尺寸预览可读；构建、单元测试、lint 与原证书签名通过。
## 风险与约束
OverlayDropdown 必须由既有 Scaffold 承载，不采样自身；一台模拟器；Glance 字体使用系统正常/Medium，不用 Material 色板。
