package com.unistack.app.core.notifications

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

private const val HISTORY_PREFS = "unistack_notification_history"
private const val HISTORY_ITEMS = "items"
private const val MAX_HISTORY_ITEMS = 40
private const val DUPLICATE_WINDOW_MILLIS = 12L * 60L * 60L * 1000L

data class NotificationHistoryItem(
    val id: Int,
    val requestCode: Int,
    val title: String,
    val body: String,
    val timestampMillis: Long,
    val scheduledAtMillis: Long?,
    val delivered: Boolean,
    val read: Boolean,
    val targetRoute: String? = null
)

object NotificationHistoryStore {
    private val itemsFlow = MutableStateFlow<List<NotificationHistoryItem>>(emptyList())
    private var loaded = false

    fun observe(context: Context): StateFlow<List<NotificationHistoryItem>> {
        ensureLoaded(context.applicationContext)
        return itemsFlow
    }

    fun recordDelivered(
        context: Context,
        requestCode: Int,
        title: String,
        body: String,
        targetRoute: String? = null
    ): NotificationHistoryItem? {
        ensureLoaded(context.applicationContext)
        val deliveredAt = System.currentTimeMillis()
        val duplicate = itemsFlow.value.any { item ->
            item.delivered &&
                item.requestCode == requestCode &&
                item.title == title &&
                item.body == body &&
                deliveredAt - item.timestampMillis < DUPLICATE_WINDOW_MILLIS
        }
        if (duplicate) return null

        var deliveredItem: NotificationHistoryItem? = null
        update(context.applicationContext) { current ->
            val item = NotificationHistoryItem(
                id = historyId(requestCode, deliveredAt),
                requestCode = requestCode,
                title = title,
                body = body,
                timestampMillis = deliveredAt,
                scheduledAtMillis = null,
                delivered = true,
                read = false,
                targetRoute = targetRoute
            )
            deliveredItem = item
            (listOf(item) + current.filterNot { it.id == item.id })
                .sortedByDescending { it.timestampMillis }
                .take(MAX_HISTORY_ITEMS)
        }
        return deliveredItem
    }

    fun markAllRead(context: Context) {
        ensureLoaded(context.applicationContext)
        update(context.applicationContext) { current ->
            current.map { it.copy(read = true) }
        }
    }

    fun markRead(context: Context, id: Int) {
        ensureLoaded(context.applicationContext)
        update(context.applicationContext) { current ->
            current.map { item ->
                if (item.id == id) item.copy(read = true) else item
            }
        }
    }

    fun delete(context: Context, id: Int) {
        ensureLoaded(context.applicationContext)
        update(context.applicationContext) { current ->
            current.filterNot { it.id == id }
        }
    }

    private fun ensureLoaded(context: Context) {
        if (loaded) return
        val deliveredItems = readItems(context)
            .filter { it.delivered }
            .sortedByDescending { it.timestampMillis }
            .take(MAX_HISTORY_ITEMS)
        itemsFlow.value = deliveredItems
        writeItems(context, deliveredItems)
        loaded = true
    }

    private fun update(
        context: Context,
        transform: (List<NotificationHistoryItem>) -> List<NotificationHistoryItem>
    ) {
        val next = transform(itemsFlow.value)
        itemsFlow.value = next
        writeItems(context, next)
    }

    private fun readItems(context: Context): List<NotificationHistoryItem> {
        val raw = context.getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE)
            .getString(HISTORY_ITEMS, null)
            ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                NotificationHistoryItem(
                    id = item.optInt("id"),
                    requestCode = item.optInt("requestCode", item.optInt("id")),
                    title = item.optString("title"),
                    body = item.optString("body"),
                    timestampMillis = item.optLong("timestampMillis"),
                    scheduledAtMillis = item.optLong("scheduledAtMillis").takeIf { it > 0L },
                    delivered = item.optBoolean("delivered"),
                    read = item.optBoolean("read", true),
                    targetRoute = item.optString("targetRoute").takeIf { it.isNotBlank() }
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun writeItems(context: Context, items: List<NotificationHistoryItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("requestCode", item.requestCode)
                    .put("title", item.title)
                    .put("body", item.body)
                    .put("timestampMillis", item.timestampMillis)
                    .put("scheduledAtMillis", item.scheduledAtMillis ?: 0L)
                    .put("delivered", item.delivered)
                    .put("read", item.read)
                    .put("targetRoute", item.targetRoute ?: "")
            )
        }
        context.getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE)
            .edit {
                putString(HISTORY_ITEMS, array.toString())
            }
    }

    private fun historyId(requestCode: Int, occurrenceMillis: Long): Int {
        return "$requestCode:$occurrenceMillis".hashCode() and Int.MAX_VALUE
    }
}
