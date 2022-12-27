package vn.com.vti.common.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import vn.com.vti.common.serializer.Serializer
import vn.com.vti.common.serializer.deserialize

fun NotificationManager.compatCreateNotificationChannel(
    channelId: String,
    channelName: String,
    channelDescription: String? = null,
    importance: Int = NotificationManager.IMPORTANCE_DEFAULT
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel =
            NotificationChannel(channelId, channelName, importance).apply {
                this.description = channelDescription
                setShowBadge(true)
            }
        createNotificationChannel(channel)
    }
}

inline fun <reified T> RemoteMessage.parseAsJson(serializer: Serializer): T =
    data.let {
        JsonObject().apply {
            it.forEach { (t, u) ->
                add(t, JsonPrimitive(u))
            }
        }.toString()
    }.let {
        serializer.deserialize(it)
    }

inline fun <reified T> Intent.parseAsNotificationDataMessage(
    indicatorKey: String,
    serializer: Serializer
): T? =
    extras?.let {
        if (!it.containsKey(indicatorKey))
            JsonObject().apply {
                it.keySet().forEach { key ->
                    val value = it.get(key)
                    if (value is String) {
                        add(key, JsonPrimitive(value))
                    }
                }
            }.toString()
        else null
    }?.let {
        serializer.deserialize(it)
    }