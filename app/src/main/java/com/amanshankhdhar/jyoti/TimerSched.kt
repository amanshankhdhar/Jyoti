// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock

object TimerSched {
    fun schedule(c: Context, minutes: Int) {
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val flags = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
        val pi = PendingIntent.getBroadcast(c, 1, Intent(c, TorchOffReceiver::class.java), flags)
        am.cancel(pi)
        if (minutes > 0)
            am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + minutes * 60000L, pi)
    }
}
