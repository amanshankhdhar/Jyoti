// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView

class EffectsActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_effects)
        val label = findViewById<TextView>(R.id.hzLabel)
        val seek = findViewById<SeekBar>(R.id.seekHz)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) {
                label.text = "Strobe: ${p + 1} Hz"
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        findViewById<Button>(R.id.btnStrobe).setOnClickListener {
            TorchManager.startStrobe(this, seek.progress + 1)
        }
        findViewById<Button>(R.id.btnSOS).setOnClickListener { TorchManager.startSOS(this) }
        findViewById<Button>(R.id.btnStop).setOnClickListener { TorchManager.forceOff(this) }
    }
}
