# 开源发布验证

2026-10-13：发布0.5.6源码到AritxOnly/F1Info，保留远程master初始GPL-3.0许可证提交。

- Release签名配置改为可选；没有signing.local.properties也能构建Debug和未签名Release。
- 以不存在的`-Pf1info.signingProperties=/tmp/f1info-no-signing.properties`执行assembleDebug、assembleRelease、testDebugUnitTest、lintDebug：全部通过，45项测试通过。
- 默认本机私有配置重新执行assembleRelease，并通过原发行证书apksigner验证。签名与APK摘要见公开验证文件。
- .gitignore排除keystore、私钥、本机签名/SDK配置、构建缓存及APK。
- 暂存文件扫描：没有实际本机签名密码、GitHub令牌、常见API令牌或私钥内容。README构建步骤不依赖发行者私有密钥。
- README更新到0.5.6；保留第三方来源归属，补齐GPL、MIT、Apache、OFL许可说明。第三方肖像/标识不作为原创素材重新授权。
- 提交使用GitHub noreply地址，未修改全局Git身份设置。
