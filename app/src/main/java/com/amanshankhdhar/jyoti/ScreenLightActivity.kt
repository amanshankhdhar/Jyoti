// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager

class ScreenLightActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(View(this).also {
            it.setBackgroundColor(Color.WHITE)
            it.setOnClickListener { finish() }   // tap anywhere to exit
        })
        window.attributes = window.attributes.apply { screenBrightness = 1.0f }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
