package com.aritxonly.f1info.notification

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat
import com.aritxonly.f1info.MainActivity
import com.aritxonly.f1info.R
import com.aritxonly.f1info.data.racing.*
import kotlinx.coroutines.*
import java.time.Instant

/** User-started session monitoring. A completed session stops the service automatically. */
@android.annotation.SuppressLint("MissingPermission")
class LiveRaceService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var job: Job? = null
    private var lastPublication: String? = null
    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "比赛动态", NotificationManager.IMPORTANCE_LOW))
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == STOP) { stopSelf(); return START_NOT_STICKY }
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) { stopSelf(); return START_NOT_STICKY }
        if (job?.isActive == true) return START_NOT_STICKY
        active.value = true
        startForeground(ID, notification("正在连接比赛动态", "等待 OpenF1 数据", "连接中", false))
        job = scope.launch {
            while (isActive) {
                LiveFeed.refresh(this@LiveRaceService)
                val state = LiveFeed.state.value; val data = state.data
                if (data == null && state.error != null) { stopSelf(); break }
                if (data != null && !data.session.active(Instant.now())) { stopSelf(); break }
                if (data != null) {
                    val label = data.label(Instant.now())
                    val lap = data.events.mapNotNull { it.lap }.maxOrNull()?.let { "L$it" } ?: "LIVE"
                    val title = "${data.session.location} · ${data.session.name}"
                    val content = if (state.error != null || label != "LIVE") "更新中断 · 数据过期" else data.events.firstOrNull()?.message ?: "${data.positions.size} 名车手 · ${data.leader() ?: "—"} 领先"
                    val status = if (state.error != null || label != "LIVE") "数据过期" else "$lap ${data.leader() ?: "—"}"
                    val publication = "$title|$content|$status"
                    if (publication != lastPublication) {
                    lastPublication = publication
                    IslandWhitelistBypass.publish(this@LiveRaceService, "race") {
                        if (job?.isActive == true) getSystemService(NotificationManager::class.java)
                            .notify(ID, notification(title, content, status, true))
                    }
                    }
                }
                delay(15000)
            }
        }
        return START_NOT_STICKY
    }
    private fun notification(title: String, content: String, status: String, island: Boolean): Notification {
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java)
            .putExtra("widget_destination", "live"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 1, Intent(this, LiveRaceService::class.java).setAction(STOP), PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title).setContentText(content).setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentIntent(open).setOnlyAlertOnce(true).setOngoing(true).setCategory(NotificationCompat.CATEGORY_EVENT)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(0, "停止关注", stop)
        if (island && LiveUpdateNotificationAdapter.supportsIsland())
            builder.addExtras(XiaomiIslandExtras.create(this, title, content, status, R.drawable.ic_launcher_monochrome))
        val notification = builder.build()
        return notification
    }
    override fun onTimeout(startId: Int, fgsType: Int) { stopSelf() }
    override fun onDestroy() { active.value = false; scope.cancel(); IslandWhitelistBypass.restore(this); stopForeground(STOP_FOREGROUND_REMOVE); super.onDestroy() }
    override fun onBind(intent: Intent?) = null
    companion object {
        val active = kotlinx.coroutines.flow.MutableStateFlow(false)
        private const val CHANNEL = "live_race"
        private const val ID = 71001
        private const val STOP = "com.aritxonly.f1info.STOP_LIVE"
        fun start(context: Context) { context.startForegroundService(Intent(context, LiveRaceService::class.java)) }
        fun stop(context: Context) { context.stopService(Intent(context, LiveRaceService::class.java)) }
    }
}
