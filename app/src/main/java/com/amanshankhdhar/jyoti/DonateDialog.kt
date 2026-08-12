// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class DonateDialog(private val activity: Activity) : Dialog(activity) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_donate)
        
        // Apply fonts manually for dialog
        val tvTitle = findViewById<TextView>(R.id.txtUPI)
        tvTitle.typeface = Fonts.regular(activity)

        // Load QR from assets
        val imgQR = findViewById<ImageView>(R.id.imgQR)
        try {
            val input = activity.assets.open("gpay_qr.png")
            val bitmap = BitmapFactory.decodeStream(input)
            imgQR.setImageBitmap(bitmap)
        } catch (e: Exception) {
            imgQR.setImageResource(R.drawable.ic_tile_off) // Fallback
        }

        findViewById<Button>(R.id.btnClose).setOnClickListener { dismiss() }

        findViewById<Button>(R.id.btnPay).setOnClickListener {
            val upiId = activity.getString(R.string.upi_id)
            // Standard UPI deep link format
            val uri = Uri.parse("upi://pay?pa=$upiId&pn=Jyoti%20App&cu=INR")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(intent)
            } else {
                Toast.makeText(activity, "No UPI app found", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
    }
}