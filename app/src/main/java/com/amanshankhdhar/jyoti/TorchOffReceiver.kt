// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TorchOffReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) = TorchManager.forceOff(c)
}
