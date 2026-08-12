// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.content.Context

object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("jyoti", 0)
    fun getB(c: Context, k: String, d: Boolean) = sp(c).getBoolean(k, d)
    fun setB(c: Context, k: String, v: Boolean) = sp(c).edit().putBoolean(k, v).apply()
    fun getI(c: Context, k: String, d: Int) = sp(c).getInt(k, d)
    fun setI(c: Context, k: String, v: Int) = sp(c).edit().putInt(k, v).apply()
    fun getL(c: Context, k: String, d: Long) = sp(c).getLong(k, d)
    fun setL(c: Context, k: String, v: Long) = sp(c).edit().putLong(k, v).apply()
    fun getS(c: Context, k: String, d: String) = sp(c).getString(k, d) ?: d
    fun setS(c: Context, k: String, v: String) = sp(c).edit().putString(k, v).apply()
}