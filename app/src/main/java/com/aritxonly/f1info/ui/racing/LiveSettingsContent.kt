package com.aritxonly.f1info.ui.racing

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aritxonly.f1info.data.racing.*
import com.aritxonly.f1info.notification.*
import com.aritxonly.f1info.ui.components.*
import top.yukonga.miuix.kmp.preference.*
import kotlinx.coroutines.*
import rikka.shizuku.Shizuku
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LiveSettingsContent(section: String = "openf1") {
    val context = LocalContext.current
    val uri = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var authorized by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var mode by remember { mutableStateOf(IslandWhitelistBypass.mode(context)) }
    val status by IslandWhitelistBypass.status.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { authorized = withContext(Dispatchers.IO) { LiveCredentials.configured(context) } }
    DisposableEffect(context) {
        val listener = Shizuku.OnRequestPermissionResultListener { code, result ->
            if (code == 52002 && result == PackageManager.PERMISSION_GRANTED) IslandWhitelistBypass.authorize(context)
        }
        Shizuku.addRequestPermissionResultListener(listener)
        onDispose { Shizuku.removeRequestPermissionResultListener(listener) }
    }
    LazyColumn(Modifier.fillMaxSize().imePadding(), state = LocalSecondaryListState.current, contentPadding = secondaryContentPadding()) {
        item {
            if (section == "openf1") {
            SectionTitle("OpenF1 授权", if (authorized) "已保存授权" else "历史数据无需登录")
            SettingsSection { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("赛中实时接口需要 OpenF1 Sponsor 账号。账号与令牌加密保存在本机，过期时自动续期。", style = MiuixTheme.textStyles.body2)
                TextField(username, { username = it }, Modifier.fillMaxWidth(), label = "OpenF1 邮箱 / 账号", singleLine = true, enabled = !busy)
                TextField(password, { password = it }, Modifier.fillMaxWidth(), label = "密码", singleLine = true, enabled = !busy,
                    visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                TextButton(if (busy) "正在验证…" else "验证并保存", {
                    busy = true; message = null
                    scope.launch {
                        try { withContext(Dispatchers.IO) { LiveCredentials.login(context, username.trim(), password) }; authorized = true; password = ""; message = "授权已保存" }
                        catch (e: Exception) { if (e is CancellationException) throw e; message = e.message }
                        finally { busy = false }
                    }
                }, Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColorsPrimary(), enabled = !busy && username.isNotBlank() && password.isNotBlank())
                message?.let { Text(it, style = MiuixTheme.textStyles.footnote1) }
            }
                ArrowPreference(title = "授权说明", summary = "OpenF1 Sponsor 与实时数据", onClick = { uri.openUri("https://openf1.org/auth.html") })
                if (authorized) BasicComponent(title = "删除本机授权", summary = "同时停止后台关注", enabled = !busy,
                    onClick = { LiveRaceService.stop(context); LiveCredentials.clear(context); authorized = false; password = ""; message = "本机授权已删除" })
            }
            }
            if (section == "island") {
            SectionTitle("超级岛", if (LiveUpdateNotificationAdapter.supportsIsland()) "小米 HyperOS" else "此设备使用常规动态通知")
            SettingsSection {
                Text("仅在后台关注比赛时更新。Root / Shizuku 模式沿用 Deadliner 的短暂白名单绕过与自动网络恢复；可能短暂影响小米推送连接。", modifier = Modifier.padding(16.dp), style = MiuixTheme.textStyles.body2)
                val modes = listOf(IslandBypassMode.OFF, IslandBypassMode.ROOT, IslandBypassMode.SHIZUKU)
                OverlayDropdownPreference(title = "推送方式", items = listOf("常规通知", "Root", "Shizuku"),
                    selectedIndex = modes.indexOf(mode).coerceAtLeast(0),
                    onSelectedIndexChange = { mode = modes[it]; IslandWhitelistBypass.setMode(context, mode) })
                if (mode != IslandBypassMode.OFF) ArrowPreference(title = "连接并授权 ${mode.name}", onClick = {
                    if (mode == IslandBypassMode.SHIZUKU) {
                        IslandWhitelistBypass.requestShizukuPermission()
                        if (runCatching { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED }.getOrDefault(false)) IslandWhitelistBypass.authorize(context)
                    } else IslandWhitelistBypass.authorize(context)
                }, enabled = LiveUpdateNotificationAdapter.supportsIsland())
                status?.let { Text(it, Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MiuixTheme.textStyles.footnote2) }
                ArrowPreference(title = "恢复推送网络", summary = "手动恢复小米推送连接", onClick = { IslandWhitelistBypass.restore(context) })
            }
            }
        }
    }
}
