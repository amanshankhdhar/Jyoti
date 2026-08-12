// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout

class PoliceScreenActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private var isRed = true
    private var isRunning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_police_screen)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.attributes = window.attributes.apply { screenBrightness = 1.0f }

        val root = findViewById<FrameLayout>(R.id.rootPolice)

        val flasher = object : Runnable {
            override fun run() {
                if (!isRunning) return
                isRed = !isRed
                root.setBackgroundColor(if (isRed) Color.RED else Color.BLUE)
                handler.postDelayed(this, 400) // Flash every 400ms
            }
        }
        handler.post(flasher)

        root.setOnClickListener {
            isRunning = false
            finish()
        }
    }
}