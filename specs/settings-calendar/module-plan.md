# 模块计划
## 模块拆分
- calendar：纯场次规划与 ICS 序列化，Android Provider 查询和批量写入。
- ui/settings：设置入口、日历选择/预览/权限状态。
- ui/racing：沿用授权、超级岛实现，按子页面分组。
- ui/components：关于保留技术致谢和应用身份。
## 平台映射
Android CalendarContract 与 Compose MIUIX；ICS 可跨平台导入。
## 文件拆分策略
规划、存储和界面各自独立，不扩大应用壳。
## 风险点
权限撤销、只读日历、重复写入、未知时间、日历时间更新。
