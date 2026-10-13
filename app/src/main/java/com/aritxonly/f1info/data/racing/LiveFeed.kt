package com.aritxonly.f1info.data.racing

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.net.URLEncoder

/** Shared by the visible Live page and foreground island service; only one poll is in flight. */
object LiveFeed {
    data class State(val data: LiveSnapshot? = null, val loading: Boolean = false, val error: String? = null)
    private val mutable = MutableStateFlow(State())
    val state = mutable.asStateFlow()
    private val lock = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    fun requestRefresh(context: Context) { scope.launch { refresh(context.applicationContext, true) } }
    @Volatile var selectedKey: String = "latest"
        private set
    private var lastPoll = 0L
    fun select(key: Int?) { selectedKey = key?.toString() ?: "latest"; lastPoll = 0; mutable.value = State() }
    private fun endpoint(path: String, query: String) = "https://api.openf1.org/v1/$path?$query"
    suspend fun sessions(year: Int): List<LiveSession> = withContext(Dispatchers.IO) {
        val rows = LiveParser.sessions(RacingHttp.get(endpoint("sessions", "year=$year")))
        val now = Instant.now()
        rows.filter { !it.start.isAfter(now) }.sortedByDescending { it.start } + rows.filter { it.start.isAfter(now) }.sortedBy { it.start }
    }
    suspend fun refresh(context: Context, force: Boolean = false) = lock.withLock {
        val tick = android.os.SystemClock.elapsedRealtime()
        if (!force && tick - lastPoll < 12000) return@withLock
        lastPoll = tick
        val requestedKey = selectedKey
        mutable.update { it.copy(loading = true) }
        if (mutable.value.data == null) withContext(Dispatchers.IO) { LiveSnapshotCache.read(context, requestedKey)?.let { if (requestedKey == selectedKey) mutable.update { previous -> previous.copy(data = it) } } }
        try {
            val token = withContext(Dispatchers.IO) { LiveCredentials.read(context) }
            val session = withContext(Dispatchers.IO) { LiveParser.sessions(RacingHttp.get(endpoint("sessions", "session_key=$requestedKey"), token)).maxBy { it.start } }
            if (requestedKey != selectedKey) return@withLock
            val old = mutable.value.data?.takeIf { it.session.key == session.key }
            if (!force && old != null && !session.active(Instant.now())) { mutable.update { it.copy(loading = false, error = null) }; return@withLock }
            val query = "session_key=${session.key}"
            if (!session.active(Instant.now()) && !Instant.now().isBefore(session.end)) {
                val parts = withContext(Dispatchers.IO) { listOf("session_result", "drivers", "race_control").map { RacingHttp.get(endpoint(it, query), token) } }
                val result = org.json.JSONArray(parts[0])
                val positions = LiveParser.finalPositions(parts[0], session.end)
                val intervals = (0 until result.length()).map { result.getJSONObject(it) }.map {
                    LiveInterval(it.getInt("driver_number"), LiveParser.gap(it.opt("gap_to_leader")), session.end)
                }
                val data = LiveSnapshot(session, LiveParser.drivers(parts[1]), positions, intervals, LiveParser.events(parts[2]).sortedByDescending { it.date }.take(100), Instant.now())
                withContext(Dispatchers.IO) { LiveSnapshotCache.write(context, requestedKey, data) }
                if (requestedKey != selectedKey) return@withLock
                mutable.value = State(data)
                return@withLock
            }
            val since = old?.latestSample()?.minusSeconds(4) ?: session.end.minusSeconds(120).takeIf { !session.active(Instant.now()) } ?: Instant.now().minusSeconds(120)
            val date = URLEncoder.encode(since.toString(), "UTF-8")
            val parts = coroutineScope { listOf("position", "intervals", "race_control", "drivers").map { path -> async(Dispatchers.IO) {
                if (path == "drivers" && old != null) "" else RacingHttp.get(endpoint(path, query + if (path in listOf("position", "intervals")) "&date%3E=$date" else if (path == "race_control" && old != null) "&date%3E=$date" else ""), token)
            } }.awaitAll() }
            val data = LiveSnapshot(session, old?.drivers ?: LiveParser.drivers(parts[3]),
                mergePositions(old?.positions.orEmpty(), LiveParser.positions(parts[0])), mergeIntervals(old?.intervals.orEmpty(), LiveParser.intervals(parts[1])),
                mergeEvents(old?.events.orEmpty(), LiveParser.events(parts[2])), Instant.now())
            withContext(Dispatchers.IO) { LiveSnapshotCache.write(context, requestedKey, data) }
            if (requestedKey != selectedKey) return@withLock
            mutable.value = State(data)
        } catch (cancelled: CancellationException) { if (requestedKey == selectedKey) mutable.update { it.copy(loading = false) }; throw cancelled }
        catch (e: Exception) { if (requestedKey == selectedKey) mutable.update { it.copy(loading = false, error = e.message ?: "无法获取比赛动态") } }
    }
}
