package com.aritxonly.f1info.widgets

import android.content.Context
import androidx.compose.runtime.*
import androidx.glance.GlanceId
import androidx.glance.appwidget.*
import com.aritxonly.f1info.data.F1Repository
import com.aritxonly.f1info.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.theme.*

internal data class WidgetSnapshot(val data: SeasonData?, val favorites: Favorites)
internal object WidgetState { val revision = MutableStateFlow(0L) }
internal enum class WidgetKind { Schedule, Portrait, Drivers, Teams }

abstract class RacingWidget internal constructor(private val kind: WidgetKind, private val slot: Int = 0, private val previewConfiguration: WidgetConfiguration? = null) : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = F1Repository(context)
        val initial = WidgetSnapshot(repo.cached(), repo.favorites())
        val widgetId = runCatching { GlanceAppWidgetManager(context).getAppWidgetId(id) }.getOrDefault(-1)
        provideContent {
            val revision by WidgetState.revision.collectAsState()
            var snapshot by remember { mutableStateOf(initial) }
            LaunchedEffect(revision) { snapshot = WidgetSnapshot(repo.cached(), repo.favorites()) }
            val configuration = remember(revision, widgetId) { previewConfiguration ?: WidgetConfigurationStore.read(context, widgetId) }
            val colors = if (configuration.lightText) darkColorScheme() else lightColorScheme()
            RacingWidgetContent(snapshot, colors, kind, slot, configuration)
        }
    }
}

class F1Widget : RacingWidget(WidgetKind.Schedule)
class DriverPortraitWidget : RacingWidget(WidgetKind.Portrait)
class SecondDriverPortraitWidget : RacingWidget(WidgetKind.Portrait, 1)
class DriversStandingsWidget : RacingWidget(WidgetKind.Drivers)
class TeamsStandingsWidget : RacingWidget(WidgetKind.Teams)

abstract class RacingWidgetReceiver : GlanceAppWidgetReceiver() {
    override fun onUpdate(context: Context, appWidgetManager: android.appwidget.AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetConfigurationStore.registerEditor(context, appWidgetIds)
    }
}
class ScheduleWideWidget : RacingWidget(WidgetKind.Schedule)
class ScheduleLargeWidget : RacingWidget(WidgetKind.Schedule)
class DriversLargeWidget : RacingWidget(WidgetKind.Drivers)
class TeamsLargeWidget : RacingWidget(WidgetKind.Teams)
class ScheduleWideWidgetReceiver : RacingWidgetReceiver() { override val glanceAppWidget = ScheduleWideWidget() }
class ScheduleLargeWidgetReceiver : RacingWidgetReceiver() { override val glanceAppWidget = ScheduleLargeWidget() }
class DriversLargeWidgetReceiver : RacingWidgetReceiver() { override val glanceAppWidget = DriversLargeWidget() }
class TeamsLargeWidgetReceiver : RacingWidgetReceiver() { override val glanceAppWidget = TeamsLargeWidget() }

class F1WidgetReceiver : RacingWidgetReceiver() { override val glanceAppWidget = F1Widget() }
class DriverPortraitWidgetReceiver : RacingWidgetReceiver() { override val glanceAppWidget = DriverPortraitWidget() }
class SecondDriverPortraitWidgetReceiver : RacingWidgetReceiver() { override val glanceAppWidget = SecondDriverPortraitWidget() }
class DriversStandingsWidgetReceiver : RacingWidgetReceiver() { override val glanceAppWidget = DriversStandingsWidget() }
class TeamsStandingsWidgetReceiver : RacingWidgetReceiver() { override val glanceAppWidget = TeamsStandingsWidget() }

suspend fun refreshF1Widgets(context: Context) {
    WidgetState.revision.value = WidgetState.revision.value + 1
    withContext(Dispatchers.IO) {
        listOf(F1Widget(), DriverPortraitWidget(), SecondDriverPortraitWidget(), DriversStandingsWidget(), TeamsStandingsWidget(), ScheduleWideWidget(), ScheduleLargeWidget(), DriversLargeWidget(), TeamsLargeWidget()).forEach { it.updateAll(context) }
    }
}
