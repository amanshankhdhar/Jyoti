// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var badge: TextView
    private lateinit var power: ImageButton
    private lateinit var strength: SeekBar

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_home)
        badge = findViewById(R.id.badge)
        power = findViewById(R.id.btnPower)
        strength = findViewById(R.id.seekStrength)

        TorchManager.listener = { runOnUiThread { refresh() } }

        power.setOnClickListener {
            when (TorchManager.toggle(this)) {
                TorchResult.BLOCKED -> Toast.makeText(this,
                    "System blocked torch — grant Camera permission in Settings", Toast.LENGTH_LONG).show()
                TorchResult.NO_HARDWARE, TorchResult.FAILED -> Toast.makeText(this,
                    "LED unavailable — use SCREEN LIGHT", Toast.LENGTH_LONG).show()
                else -> {}
            }
            refresh()
        }

        val max = TorchManager.maxStrength(this)
        if (max > 1) {
            strength.visibility = View.VISIBLE
            strength.max = max; strength.progress = max
            strength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) {
                    if (from && TorchManager.isOn) TorchManager.setStrength(this@MainActivity, p.coerceAtLeast(1))
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }

        findViewById<View>(R.id.btnEffects).setOnClickListener { startActivity(Intent(this, EffectsActivity::class.java)) }
        findViewById<View>(R.id.btnScreen).setOnClickListener { startActivity(Intent(this, ScreenLightActivity::class.java)) }
        findViewById<View>(R.id.btnSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        findViewById<View>(R.id.btnTile).setOnClickListener { Toast.makeText(this,
            "Pull down Quick Settings -> pencil (edit) -> drag 'Jyoti' tile in", Toast.LENGTH_LONG).show() }
        findViewById<TextView>(R.id.linkPrivacy).setOnClickListener { legal("privacy.html") }
        findViewById<TextView>(R.id.linkTerms).setOnClickListener { legal("terms.html") }
        findViewById<TextView>(R.id.linkAbout).setOnClickListener { legal("about.html") }

        val timer = findViewById<Spinner>(R.id.spinTimer)
        timer.setSelection(Prefs.getI(this, "timerIdx", 0))
        timer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                Prefs.setI(this@MainActivity, "timerIdx", pos)
                TimerSched.schedule(this@MainActivity, intArrayOf(0, 1, 5, 10, 30)[pos])
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 77)
        refresh()
    }

    private fun refresh() {
        val on = TorchManager.isOn
        power.isActivated = on
        badge.text = when {
            TorchManager.mode != "NORMAL" -> "MODE: " + TorchManager.mode
            on -> "TORCH ACTIVE"
            else -> "TORCH INACTIVE"
        }
        badge.setTextColor(if (on) 0xFFFFC107.toInt() else 0xFF888888.toInt())
    }

    private fun legal(p: String) =
        startActivity(Intent(this, LegalActivity::class.java).putExtra("page", p))
}
