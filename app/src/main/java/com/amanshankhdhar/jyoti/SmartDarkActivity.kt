// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView

class SmartDarkActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private var countdown = 3
    private var countdownRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_smart_dark)
        
        Feedback.alert(this) 
        
        val txtCountdown = findViewById<TextView>(R.id.txtCountdown)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        
        progressBar.max = 3000
        progressBar.progress = 3000
        
        countdownRunnable = object : Runnable {
            override fun run() {
                countdown--
                if (countdown > 0) {
                    txtCountdown.text = "Turning on in $countdown..."
                    progressBar.progress = countdown * 1000
                    Feedback.soft(this@SmartDarkActivity)
                    handler.postDelayed(this, 1000)
                } else {
                    TorchManager.toggle(this@SmartDarkActivity)
                    finish()
                }
            }
        }
        handler.postDelayed(countdownRunnable!!, 1000)
        
        btnCancel.setOnClickListener {
            countdownRunnable?.let { handler.removeCallbacks(it) }
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownRunnable?.let { handler.removeCallbacks(it) }
    }
}