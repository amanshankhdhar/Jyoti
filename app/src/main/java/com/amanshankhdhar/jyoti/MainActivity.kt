// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var torchBtn: Button

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)
        torchBtn = findViewById(R.id.btnTorch)

        torchBtn.setOnClickListener {
            when (TorchManager.toggle(this)) {
                TorchResult.BLOCKED ->
                    if (Build.VERSION.SDK_INT >= 23)
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), 42)
                TorchResult.NO_HARDWARE, TorchResult.FAILED ->
                    Toast.makeText(this, "LED torch unavailable — try SCREEN LIGHT", Toast.LENGTH_LONG).show()
                else -> {}
            }
            refresh()
        }

        findViewById<Button>(R.id.btnScreen).setOnClickListener {
            startActivity(Intent(this, ScreenLightActivity::class.java))
        }

        findViewById<Button>(R.id.btnTile).setOnClickListener {
            Toast.makeText(this,
                "Pull down Quick Settings -> press pencil (edit) -> drag 'Jyoti' tile in",
                Toast.LENGTH_LONG).show()
        }

        findViewById<TextView>(R.id.linkPrivacy).setOnClickListener { legal("privacy.html") }
        findViewById<TextView>(R.id.linkTerms).setOnClickListener { legal("terms.html") }
        findViewById<TextView>(R.id.linkAbout).setOnClickListener { legal("about.html") }
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        status.text = if (TorchManager.isOn) "🔦 Torch is ON" else "Torch is OFF"
        torchBtn.text = if (TorchManager.isOn) "TURN OFF" else "TURN ON"
    }

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        super.onRequestPermissionsResult(rc, p, g)
        if (rc == 42 && g.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            TorchManager.toggle(this); refresh()
        }
    }

    private fun legal(page: String) =
        startActivity(Intent(this, LegalActivity::class.java).putExtra("page", page))
}
