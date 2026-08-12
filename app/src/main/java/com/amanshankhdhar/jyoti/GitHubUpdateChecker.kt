// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import java.net.HttpURLConnection
import java.net.URL

object GitHubUpdateChecker {
    private const val REPO_URL = "https://api.github.com/repos/amanshankhdhar/Jyoti/releases/latest"

    fun check(activity: Activity) {
        Thread {
            try {
                val conn = URL(REPO_URL).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "Jyoti-App-Updater")
                conn.connectTimeout = 5000
                
                if (conn.responseCode == 200) {
                    val json = conn.inputStream.bufferedReader().readText()
                    // Extract "tag_name":"vX.Y.Z" using Regex
                    val regex = "\"tag_name\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                    val match = regex.find(json)
                    val latestTag = match?.groupValues?.get(1) ?: return@Thread
                    
                    val currentName = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
                    
                    // If GitHub version is different and starts with 'v', prompt update
                    if (latestTag.startsWith("v") && latestTag != "v$currentName") {
                        activity.runOnUiThread { showUpdateDialog(activity, latestTag) }
                    }
                }
            } catch (e: Exception) {
                // No internet or parse error, fail silently
            }
        }.start()
    }

    private fun showUpdateDialog(activity: Activity, newVersion: String) {
        AlertDialog.Builder(activity)
            .setTitle("Update Available 🚀")
            .setMessage("Jyoti $newVersion is ready! Download the latest version from GitHub to get new features and bug fixes.")
            .setPositiveButton("Update Now") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/amanshankhdhar/Jyoti/releases/latest"))
                activity.startActivity(intent)
            }
            .setNegativeButton("Later", null)
            .show()
    }
}