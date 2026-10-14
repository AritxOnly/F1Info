package com.aritxonly.f1info.widgets

import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.aritxonly.f1info.MainActivity
import com.aritxonly.f1info.model.*
import com.aritxonly.f1info.ui.components.teamBrandTint
import top.yukonga.miuix.kmp.theme.Colors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun RacingWidgetContent(snapshot: WidgetSnapshot, colors: Colors, kind: WidgetKind, slot: Int, configuration: WidgetConfiguration) {
    val context = LocalContext.current
    val size = LocalSize.current
    val layout = widgetLayout(size.width.value, size.height.value)
    val data = snapshot.data
    val followed = data?.drivers?.firstOrNull { it.id == configuration.driverId.ifBlank { snapshot.favorites.driverIds.getOrNull(slot).orEmpty() } }
    val accent = colors.onSurfaceSecondary
    val background = remember(colors.surfaceContainer, accent, configuration.themeStyle, layout) {
        ImageProvider(WidgetBackground.render(colors, accent, configuration.themeStyle, layout))
    }
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("widget_destination", when (kind) { WidgetKind.Schedule -> "schedule"; WidgetKind.Drivers, WidgetKind.Teams -> "standings"; else -> "home" })
        putExtra("widget_category", if (kind == WidgetKind.Teams) 1 else 0)
    }

    val hasCustomMargin = configuration.marginPreset != WidgetMarginPreset.Default &&
        (configuration.topMarginDp() > 0 || configuration.bottomMarginDp() > 0)
    val outerModifier = if (hasCustomMargin) {
        GlanceModifier.fillMaxSize().padding(top = configuration.topMarginDp().dp, bottom = configuration.bottomMarginDp().dp)
    } else {
        GlanceModifier.fillMaxSize()
    }

    val cornerModifier = if (configuration.isDefaultCornerRadius()) {
        GlanceModifier.cornerRadius(android.R.dimen.system_app_widget_background_radius)
    } else {
        GlanceModifier.cornerRadius(configuration.cornerRadiusDp.dp)
    }

    val contentPaddingVertical = if (layout == WidgetLayout.Expanded) 16.dp else 14.dp
    val totalVerticalPadding = (if (hasCustomMargin) configuration.topMarginDp() + configuration.bottomMarginDp() else 0) + (contentPaddingVertical.value.toInt() * 2)

    Box(outerModifier) {
        Box(GlanceModifier.fillMaxSize().clickable(actionStartActivity(intent))) {
            Image(
                provider = background,
                contentDescription = null,
                modifier = GlanceModifier.fillMaxSize().then(cornerModifier).appWidgetBackground(),
                contentScale = ContentScale.FillBounds,
                alpha = configuration.opacity
            )
            // HyperOS safe area padding: 16dp horizontal, 14dp/16dp vertical
            Box(GlanceModifier.fillMaxSize().padding(horizontal = 16.dp, vertical = contentPaddingVertical)) {
                val logoId = when (kind) {
                    WidgetKind.Portrait -> followed?.teamId
                    WidgetKind.Drivers -> data?.drivers?.filter { it.position != null }?.minByOrNull { it.position!! }?.teamId
                    WidgetKind.Teams -> data?.teams?.filter { it.position != null }?.minByOrNull { it.position!! }?.id
                    else -> null
                }
                val watermark = remember(logoId, colors.onSurface) { logoId?.let { WidgetArtwork.teamLogo(context, it, colors.onSurface.toArgb()) } }
                if (watermark != null) Image(ImageProvider(watermark), null,
                    modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 16.dp), contentScale = ContentScale.Fit, alpha = .10f)
                if (data == null) Column(GlanceModifier.fillMaxSize()) {
                    Label("F1Info", colors.onSurface, 16, true)
                    Spacer(GlanceModifier.height(12.dp))
                    Label("点击打开应用，加载赛程与积分。", colors.onSurfaceVariantSummary, 12)
                } else when (kind) {
                    WidgetKind.Schedule -> ScheduleCard(data, colors, accent, layout, totalVerticalPadding)
                    WidgetKind.Portrait -> PortraitCard(followed, colors, accent, slot)
                    WidgetKind.Drivers, WidgetKind.Teams -> RankingCard(data, colors, accent, layout, kind == WidgetKind.Teams, totalVerticalPadding)
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(data: SeasonData, c: Colors, accent: Color, layout: WidgetLayout, verticalPadding: Int) {
    val event = nextWidgetSession(data.races, Instant.now())
    val next = event?.race
    val context = LocalContext.current
    val track = remember(next?.circuit?.id, accent) { next?.let { WidgetArtwork.circuit(context, it.circuit, accent.toArgb()) } }
    Column(GlanceModifier.fillMaxSize()) {
        if (next == null || event == null) {
            Label("${data.season} 赛季已结束", c.onSurface, 16, true)
            Spacer(GlanceModifier.height(8.dp)); Label("点击回顾完整赛程", c.onSurfaceVariantSummary, 12)
        } else if (layout == WidgetLayout.Compact) {
            Row(GlanceModifier.fillMaxWidth()) {
                Label("${if (event.ongoing) "进行中" else "下一场"} · R${next.round}", accent, 11, modifier = GlanceModifier.defaultWeight())
                Label("${data.season}", c.onSurfaceVariantSummary, 10)
            }
            if (track != null) Image(ImageProvider(track), "${next.circuit.name}赛道图", GlanceModifier.fillMaxWidth().defaultWeight(), contentScale = ContentScale.Fit)
            else Box(GlanceModifier.fillMaxWidth().defaultWeight()) { Label("赛道图待收录", c.onSurfaceVariantSummary, 11) }
            Label(next.name.removeSuffix(" Grand Prix"), c.onSurface, 15, true)
            Spacer(GlanceModifier.height(3.dp))
            Label("${event.session.name} · ${shortSession(event.session)}", accent, 11)
        } else {
            val expanded = layout == WidgetLayout.Expanded
            val fontScale = context.resources.configuration.fontScale.coerceAtLeast(1f)
            val availableHeight = LocalSize.current.height.value - verticalPadding
            val dense = !expanded && availableHeight < 130f
            val sessionFont = if (!expanded && availableHeight < 108f) 9 else 11
            val headerHeight = maxOf(availableHeight * .44f,
                next.sessions.size * (16f * fontScale + 6f) + 36f * fontScale + 8f)
            Row(GlanceModifier.fillMaxWidth().then(if (expanded) GlanceModifier.height(headerHeight.dp) else GlanceModifier.defaultWeight()),
                verticalAlignment = Alignment.Vertical.CenterVertically) {
                Column(GlanceModifier.width(if (expanded) 116.dp else 96.dp).fillMaxHeight()) {
                    Label("ROUND ${next.round.toString().padStart(2, '0')}", accent, 10)
                    Spacer(GlanceModifier.height(4.dp))
                    Label(next.name.removeSuffix(" Grand Prix"), c.onSurface, 13, true)
                    if (track != null) Image(ImageProvider(track), "${next.circuit.name}赛道图", GlanceModifier.fillMaxWidth().defaultWeight(), contentScale = ContentScale.Fit)
                    else Box(GlanceModifier.defaultWeight()) { Label("赛道图待收录", c.onSurfaceVariantSummary, 11) }
                    Label(next.circuit.locality, accent, 10)
                }
                Spacer(GlanceModifier.width(10.dp))
                Column(GlanceModifier.defaultWeight()) {
                    Label("${if (event.ongoing) "进行中" else "接下来"} · ${event.session.name}", c.onSurface, if (dense) 11 else 13, true)
                    if (!dense) Label(shortSession(event.session), accent, 11)
                    Spacer(GlanceModifier.height(if (dense) 2.dp else 4.dp))
                    next.sessions.forEach { session ->
                        Row(GlanceModifier.fillMaxWidth().padding(vertical = if (expanded) 3.dp else if (dense) 0.dp else 1.dp)) {
                            Label(session.name, c.onSurface, sessionFont, modifier = GlanceModifier.defaultWeight())
                            Label(shortSession(session), c.onSurfaceVariantSummary, sessionFont)
                        }
                    }
                }
            }
            if (expanded) {
                Spacer(GlanceModifier.height(10.dp))
                Label("接下来的比赛", c.onSurface, 13, true)
                val rowCount = ((availableHeight - headerHeight - 32f * fontScale - 10f) /
                    (14f * fontScale + 14f)).toInt().coerceIn(0, 4)
                val races = nearbyRaces(data.races, next).take(rowCount)
                if (races.isEmpty()) Label(if (nearbyRaces(data.races, next).isEmpty()) "本场为赛季最后一站" else "点击查看后续赛程", c.onSurfaceVariantSummary, 11)
                races.forEach { race ->
                    Row(GlanceModifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Vertical.CenterVertically) {
                        Label("R${race.round}", accent, 11, modifier = GlanceModifier.width(32.dp))
                        Label(race.name.removeSuffix(" Grand Prix"), c.onSurface, 12, modifier = GlanceModifier.defaultWeight())
                        Label(raceDate(race), c.onSurfaceVariantSummary, 10)
                    }
                }
                Spacer(GlanceModifier.defaultWeight())
                CacheStamp(data, c)
            }
        }
    }
}

@Composable
private fun PortraitCard(driver: Driver?, c: Colors, accent: Color, slot: Int) {
    val context = LocalContext.current
    val photo = remember(driver?.id) { driver?.let { WidgetArtwork.portrait(context, it.id) } }
    Column(GlanceModifier.fillMaxSize()) {
        Row(GlanceModifier.fillMaxWidth()) {
            Label("车手积分", accent, 11, modifier = GlanceModifier.defaultWeight())
            Label(driver?.position?.let { "P$it" } ?: "—", c.onSurface, 12, true)
        }
        Row(GlanceModifier.fillMaxWidth().defaultWeight(), verticalAlignment = Alignment.Vertical.CenterVertically) {
            Column(GlanceModifier.defaultWeight()) {
                WidgetScore(driver?.points ?: "—", c.onSurface, 32, LocalSize.current.width.value - 110f)
                Label("积分 · ${driver?.wins ?: "—"} 胜", c.onSurfaceVariantSummary, 10)
            }
            if (photo != null) Image(ImageProvider(photo), driver?.name,
                GlanceModifier.width(76.dp).fillMaxHeight(), contentScale = ContentScale.Fit)
        }
        Label(driver?.name?.substringAfterLast(" ") ?: "车手待选择", c.onSurface, 15, true)
        Spacer(GlanceModifier.height(2.dp))
        Label(driver?.team ?: "点击打开应用", accent, 10)
    }
}

@Composable
private fun RankingCard(data: SeasonData, c: Colors, accent: Color, layout: WidgetLayout, teams: Boolean, verticalPadding: Int) {
    val drivers = data.drivers.filter { it.position != null }.sortedBy { it.position }
    val constructors = data.teams.filter { it.position != null }.sortedBy { it.position }
    val driver = if (!teams) drivers.firstOrNull() else null
    val team = if (teams) constructors.firstOrNull() else null
    val context = LocalContext.current
    val art = remember(driver?.id, team?.id, c.onSurface) {
        if (team != null) WidgetArtwork.teamLogo(context, team.id, c.onSurface.toArgb())
        else driver?.let { WidgetArtwork.portrait(context, it.id) }
    }
    val expanded = layout == WidgetLayout.Expanded
    val fontScale = context.resources.configuration.fontScale.coerceAtLeast(1f)
    val headerHeight = 100f * fontScale
    Column(GlanceModifier.fillMaxSize()) {
        Row(GlanceModifier.fillMaxWidth()) {
            Label("${if (teams) "车队" else "车手"}积分榜", c.onSurface, 13, true, GlanceModifier.defaultWeight())
            Label("R${data.standingsRound ?: "—"}", c.onSurfaceVariantSummary, 10)
        }
        Spacer(GlanceModifier.height(6.dp))
        if (driver == null && team == null) {
            Spacer(GlanceModifier.height(12.dp)); Label("积分尚未公布", c.onSurface, 15, true)
        } else {
            Row(GlanceModifier.fillMaxWidth().then(if (expanded) GlanceModifier.height(headerHeight.dp) else GlanceModifier.defaultWeight()),
                verticalAlignment = Alignment.Vertical.CenterVertically) {
                Box(GlanceModifier.width(if (expanded) 116.dp else 96.dp).fillMaxHeight()) {
                    if (art != null) Image(ImageProvider(art), driver?.name ?: team?.name, GlanceModifier.fillMaxSize(), contentScale = ContentScale.Fit)
                }
                Spacer(GlanceModifier.width(10.dp))
                Column(GlanceModifier.defaultWeight()) {
                    if (expanded) {
                        Label("积分领先者", c.onSurfaceVariantSummary, 10)
                        Spacer(GlanceModifier.height(4.dp))
                        Label(driver?.name?.substringAfterLast(" ") ?: team?.name.orEmpty(), c.onSurface, 15, true)
                        Spacer(GlanceModifier.height(6.dp))
                        WidgetScore(driver?.points ?: team?.points ?: "—", c.onSurface, 30, LocalSize.current.width.value - 150f)
                        Label("积分", c.onSurfaceVariantSummary, 10)
                    } else RankingRows(drivers, constructors, teams, 3, c)
                }
            }
            if (expanded) {
                Spacer(GlanceModifier.height(6.dp))
                val count = ((LocalSize.current.height.value - verticalPadding - headerHeight - 48f * fontScale) /
                    (16f * fontScale + 12f)).toInt().coerceIn(1, 11)
                RankingRows(drivers, constructors, teams, count, c)
                Spacer(GlanceModifier.defaultWeight())
                CacheStamp(data, c)
            }
        }
    }
}

@Composable
private fun RankingRows(drivers: List<Driver>, teams: List<Team>, constructors: Boolean, count: Int, c: Colors) {
    val rows = if (constructors) teams.take(count).map { Triple(it.position, it.name, it.points) }
        else drivers.take(count).map { Triple(it.position, if (count == 3) "${it.code} · ${it.name.substringAfterLast(" ")}" else "${it.code} · ${it.name.substringAfterLast(" ")}", it.points) }
    rows.forEach { (position, name, points) ->
        Row(GlanceModifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Vertical.CenterVertically) {
            Label(position?.let { "%02d".format(it) } ?: "—", c.onSurfaceVariantSummary, 10, modifier = GlanceModifier.width(22.dp))
            Label(name, c.onSurface, 12, modifier = GlanceModifier.defaultWeight())
            WidgetScore(points ?: "—", c.onSurface, 12, 46f)
        }
    }
}

@Composable
private fun CacheStamp(data: SeasonData, c: Colors) {
    Label("缓存 ${data.fetchedAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))}", c.onSurfaceVariantSummary, 9)
}

@Composable
private fun Label(text: String, color: Color, size: Int, bold: Boolean = false, modifier: GlanceModifier = GlanceModifier) {
    Text(text, modifier, maxLines = 1, style = TextStyle(ColorProvider(color), fontSize = size.sp,
        // Deadliner Glance typography: explicit medium family survives HyperOS launcher font substitution.
        fontFamily = FontFamily("sans-serif-medium"),
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal))
}

private fun shortSession(session: Session) = session.start?.atZone(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
    ?: "${session.date.monthValue}/${session.date.dayOfMonth} 待公布"
