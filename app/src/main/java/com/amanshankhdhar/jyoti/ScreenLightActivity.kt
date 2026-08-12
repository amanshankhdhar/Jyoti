// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.SeekBar

class ScreenLightActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_screen)
        val root = findViewById<FrameLayout>(R.id.root)
        val controls = findViewById<View>(R.id.controls)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        findViewById<SeekBar>(R.id.seekBright).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) {
                    window.attributes = window.attributes.apply {
                        screenBrightness = (p.coerceAtLeast(10) / 100f)
                    }
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })

        val pick = { v: View, c: Int -> v.setOnClickListener { root.setBackgroundColor(c) } }
        pick(findViewById(R.id.cWhite), Color.WHITE)
        pick(findViewById(R.id.cRed), 0xFFFF0000.toInt())
        pick(findViewById(R.id.cWarm), 0xFFFFE082.toInt())
        pick(findViewById(R.id.cAmber), 0xFFFFC107.toInt())

        root.setOnClickListener {
            controls.visibility = if (controls.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }
}
