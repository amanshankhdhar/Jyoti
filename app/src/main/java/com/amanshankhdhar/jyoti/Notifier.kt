// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build

object Notifier {
    private const val ID = 7
    fun update(c: Context, on: Boolean) {
        val nm = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            if (nm.getNotificationChannel("jyoti") == null)
                nm.createNotificationChannel(
                    NotificationChannel("jyoti", "Torch status", NotificationManager.IMPORTANCE_LOW))
        }
        if (!on) { nm.cancel(ID); return }
        val flags = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
        val off = PendingIntent.getBroadcast(c, 0,
            android.content.Intent(c, TorchOffReceiver::class.java), flags)
        val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(c, "jyoti")
                else @Suppress("DEPRECATION") Notification.Builder(c)
        nm.notify(ID, b.setSmallIcon(R.drawable.ic_tile)
            .setContentTitle("🪔 Jyoti torch is ON").setContentText("Tap OFF to extinguish")
            .addAction(android.R.drawable.ic_lock_power_off, "OFF", off)
            .setOngoing(true).build())
    }
}
