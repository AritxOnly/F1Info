package com.aritxonly.f1info.calendar

import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars as C
import android.provider.CalendarContract.Events as E

data class WritableCalendar(val id: Long, val name: String, val account: String) {
    val label get() = "$name · $account"
}
data class ImportResult(val inserted: Int, val updated: Int)

/** Called on Dispatchers.IO. Only this app's tagged events in the selected calendar are touched. */
object CalendarStore {
    fun calendars(context: Context): List<WritableCalendar> = buildList {
        context.contentResolver.query(C.CONTENT_URI, arrayOf(C._ID, C.CALENDAR_DISPLAY_NAME, C.ACCOUNT_NAME),
            "${C.CALENDAR_ACCESS_LEVEL} >= ? AND ${C.VISIBLE} = 1", arrayOf(C.CAL_ACCESS_CONTRIBUTOR.toString()), "${C.CALENDAR_DISPLAY_NAME} ASC")?.use {
            while (it.moveToNext()) add(WritableCalendar(it.getLong(0), it.getString(1).orEmpty(), it.getString(2).orEmpty()))
        } ?: error("无法读取系统日历")
    }
    @Synchronized
    fun import(context: Context, calendarId: Long, entries: List<CalendarEntry>): ImportResult {
        require(calendars(context).any { it.id == calendarId }) { "目标日历已不可写，请重新选择" }
        val existing = mutableMapOf<String, Long>()
        context.contentResolver.query(E.CONTENT_URI, arrayOf(E._ID, E.CUSTOM_APP_URI),
            "${E.CALENDAR_ID} = ? AND ${E.CUSTOM_APP_PACKAGE} = ? AND ${E.DELETED} = 0",
            arrayOf(calendarId.toString(), context.packageName), null)?.use {
            while (it.moveToNext()) it.getString(1)?.let { key -> existing[key] = it.getLong(0) }
        } ?: error("无法核对已导入的日程")
        var inserted = 0; var updated = 0
        val operations = ArrayList<ContentProviderOperation>()
        entries.forEach { entry ->
            val id = existing[entry.key]
            val op = if (id == null) ContentProviderOperation.newInsert(E.CONTENT_URI).withValue(E.CALENDAR_ID, calendarId)
                else ContentProviderOperation.newUpdate(ContentUris.withAppendedId(E.CONTENT_URI, id))
            operations += op.withValue(E.TITLE, entry.title).withValue(E.EVENT_LOCATION, entry.location)
                .withValue(E.DESCRIPTION, entry.description).withValue(E.DTSTART, entry.start.toEpochMilli())
                .withValue(E.DTEND, entry.end.toEpochMilli()).withValue(E.EVENT_TIMEZONE, "UTC")
                .withValue(E.CUSTOM_APP_PACKAGE, context.packageName).withValue(E.CUSTOM_APP_URI, entry.key).build()
            if (id == null) inserted++ else updated++
        }
        // A season is at most ~150 events, safely below Binder's batch limit; atomic provider transaction.
        if (operations.isNotEmpty()) context.contentResolver.applyBatch(CalendarContract.AUTHORITY, operations)
        return ImportResult(inserted, updated)
    }
}
