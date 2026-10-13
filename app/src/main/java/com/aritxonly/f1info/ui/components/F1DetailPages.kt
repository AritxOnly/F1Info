package com.aritxonly.f1info.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aritxonly.f1info.model.*
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.preference.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.time.ZoneId

@Composable
fun RaceDetailContent(race: Race?, onResults: (Race) -> Unit = {}, onLive: () -> Unit = {}) {
    val uri = LocalUriHandler.current
    race?.let { r -> LazyColumn(Modifier.fillMaxSize(), contentPadding = secondaryContentPadding(8.dp)) {
            item {
                Text("${r.season} · 第 ${r.round} 轮", color = MiuixTheme.colorScheme.primary, style = MiuixTheme.textStyles.footnote1)
                Text(r.name, style = MiuixTheme.textStyles.title2, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
                TrackOutline(r.circuit, Modifier.fillMaxWidth().height(180.dp), true)
                Text(r.circuit.name, style = MiuixTheme.textStyles.title3)
                Text("${r.circuit.locality}, ${r.circuit.country}", color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 4.dp))
                SettingsSection {
                    ArrowPreference(title = "比赛结果", summary = "正赛、排位赛与冲刺赛", onClick = { onResults(r) })
                    ArrowPreference(title = "Live 围场", summary = "排名与赛会动态", onClick = onLive)
                }
                SectionTitle("完整周末赛程", ZoneId.systemDefault().id)
                SettingsSection { r.sessions.forEach { InfoRow(it.name, sessionTime(it)) } }
                SectionTitle("赛道信息")
                val track = rememberTrack(r.circuit)
                SettingsSection {
                    InfoRow("所在地", "${r.circuit.locality}, ${r.circuit.country}")
                    track?.length?.let { InfoRow("参考长度", "%.3f km".format(it / 1000.0)) }
                    track?.firstGp?.let { InfoRow("首次大奖赛", "$it") }
                    if (r.circuit.latitude != null && r.circuit.longitude != null)
                        InfoRow("坐标", "%.4f, %.4f".format(r.circuit.latitude, r.circuit.longitude))
                }
                Text("轮廓与参考长度来自 f1-circuits，布局可能与本赛季不同。时间来自 Jolpica，均已转换为设备时区。",
                    style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(vertical = 12.dp))
                SettingsSection {
                if (r.circuit.url.isNotBlank()) ArrowPreference(title = "赛道百科", onClick = { uri.openUri(r.circuit.url.replace("http://", "https://")) })
                if (r.circuit.latitude != null && r.circuit.longitude != null) ArrowPreference(title = "在地图中查看", onClick = {
                    uri.openUri("https://www.openstreetmap.org/?mlat=${r.circuit.latitude}&mlon=${r.circuit.longitude}#map=14/${r.circuit.latitude}/${r.circuit.longitude}")
                })
                }
            }
        } }
}

@Composable
fun EntityDetailContent(driver: Driver?, team: Team?,
    favorites: Favorites, data: SeasonData?, onFavorites: (Favorites) -> Unit) {
    val uri = LocalUriHandler.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = secondaryContentPadding()) {
        item { Column(Modifier.fillMaxWidth()) {
            val accent = LocalRacingAccent.current.value
            Box(Modifier.fillMaxWidth().height(280.dp)) {
                TeamLogo(driver?.teamId ?: team?.id, Modifier.align(Alignment.Center).fillMaxWidth(.9f).height(230.dp).alpha(.13f))
                if (driver != null) DriverPortrait(driver.id, Modifier.align(Alignment.TopEnd).fillMaxWidth(.75f).height(280.dp))
                else TeamCarVisual(team?.id, data?.season, Modifier.fillMaxSize())
            }
            if (driver != null) DriverNumberIcon(driver.id, driver.number, Modifier.size(72.dp, 48.dp).padding(bottom = 8.dp))
            Text(driver?.name ?: team?.name.orEmpty(), style = MiuixTheme.textStyles.title2)
            Text(driver?.team ?: team?.nationality.orEmpty(), color = accent, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
            DetailFollowControl(driver, team, favorites, data, onFavorites)
            SettingsSection {
            driver?.let {
                InfoRow("赛车号码", it.number, emphasis = true); InfoRow("国籍", it.nationality)
                InfoRow("出生日期", it.birthDate)
            }
            team?.let { InfoRow("国籍", it.nationality) }
            InfoRow("当前排名", (driver?.position ?: team?.position)?.let { "P$it" } ?: "待公布", emphasis = true)
            InfoRow("赛季积分", driver?.points ?: team?.points ?: "待公布", emphasis = true)
            InfoRow("胜场", (driver?.wins ?: team?.wins)?.toString() ?: "待公布", emphasis = true)
            }
            val url = driver?.url ?: team?.url.orEmpty()
            if (url.isNotBlank()) SettingsSection { ArrowPreference(title = "查看百科", onClick = { uri.openUri(url.replace("http://", "https://")) }) }
        } }
    }
}

@Composable
private fun DetailFollowControl(driver: Driver?, team: Team?, favorites: Favorites,
    data: SeasonData?, onSave: (Favorites) -> Unit) {
    var choosingSlot by remember(driver?.id, team?.id) { mutableStateOf(false) }
    val followed = driver?.let { it.id in favorites.driverIds } ?: (team?.id == favorites.teamId)
    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(if (followed) "已在我的围场" else "关注到首页", style = MiuixTheme.textStyles.headline1)
            Text(if (followed) "首页与桌面小组件同步显示" else if (driver != null) "选择要替换的关注车手" else "替换当前关注车队",
                style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
        IconButton(onClick = {
            if (team != null) onSave(favorites.copy(teamId = team.id))
            else choosingSlot = !choosingSlot
        }, enabled = !followed) {
            Icon(if (followed) MiuixIcons.FavoritesFill else MiuixIcons.Favorites,
                if (followed) "已关注" else "关注", Modifier.size(24.dp), tint = MiuixTheme.colorScheme.primary)
        }
    }
    if (choosingSlot && driver != null) {
        SettingsSection {
            OverlayDropdownPreference(title = "替换关注车手", showValue = false,
                items = favorites.driverIds.map { id -> data?.drivers?.firstOrNull { it.id == id }?.name ?: id },
                selectedIndex = -1, onSelectedIndexChange = { index ->
                    onSave(favorites.copy(driverIds = favorites.driverIds.toMutableList().apply { this[index] = driver.id }))
                    choosingSlot = false
                })
        }
    }
}

@Composable
fun FavoritesDetailContent(data: SeasonData?, current: Favorites, onDismiss: () -> Unit, onSave: (Favorites) -> Unit, category: Int) {
    var ids by remember { mutableStateOf(current.driverIds) }
    var teamId by remember { mutableStateOf(current.teamId) }
    var hint by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        ids = current.driverIds.filter { id -> data?.drivers?.any { it.id == id } == true }
        teamId = current.teamId.takeIf { id -> data?.teams?.any { it.id == id } == true }.orEmpty()
        hint = if (ids.size != current.driverIds.size || teamId != current.teamId) "部分关注未出现在当前赛季，请重新选择。" else null
    }
    val valid = ids.size == 2 && ids.distinct().size == 2 && data?.drivers?.map { it.id }?.containsAll(ids) == true && data.teams.any { it.id == teamId }
    val padding = secondaryContentPadding()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())) {
        item {
            Text("关注两名车手和一支车队，积分卡片会显示在首页。", style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp))
            hint?.let { Text(it, color = MiuixTheme.colorScheme.primary, style = MiuixTheme.textStyles.footnote1, modifier = Modifier.padding(16.dp)) }
        }
        if (category == 0) items(data?.drivers.orEmpty(), key = { it.id }) { d ->
            Box(Modifier.padding(horizontal = 16.dp)) { SettingsSection {
                CheckboxPreference(title = d.name, summary = d.team, checked = d.id in ids,
                    enabled = d.id in ids || ids.size < 2, onCheckedChange = { selected ->
                        ids = if (selected) ids + d.id else ids - d.id
                        hint = null
                    })
            } }
        } else items(data?.teams.orEmpty(), key = { it.id }) { t ->
            Box(Modifier.padding(horizontal = 16.dp)) { SettingsSection {
                RadioButtonPreference(title = t.name, summary = t.nationality, selected = t.id == teamId,
                    onClick = { teamId = t.id })
            } }
        }
        item { TextButton("保存关注", { onSave(Favorites(ids, teamId)); onDismiss() },
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp), enabled = valid,
            colors = ButtonDefaults.textButtonColorsPrimary()) }
    }
}

