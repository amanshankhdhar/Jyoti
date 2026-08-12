// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView

class LegalActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val page = intent.getStringExtra("page") ?: "about.html"
        setContentView(WebView(this).also { it.loadUrl("file:///android_asset/$page") })
    }
}
