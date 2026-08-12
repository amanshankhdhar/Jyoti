// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class DashboardActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        applyFonts(findViewById<ViewGroup>(android.R.id.content))

        val txtUsage = findViewById<TextView>(R.id.txtUsage)
        val txtBattery = findViewById<TextView>(R.id.txtBattery)

        val sec = TorchManager.usageToday(this)
        val m = sec / 60
        val s = sec % 60
        txtUsage.text = "${m}m ${s}s"

        txtBattery.text = "${TorchManager.batteryPct(this)}%"
    }

    private fun applyFonts(v: View) {
        if (v is TextView) v.typeface = Fonts.medium(this)
        else if (v is ViewGroup) for (i in 0 until v.childCount) applyFonts(v.getChildAt(i))
    }
}