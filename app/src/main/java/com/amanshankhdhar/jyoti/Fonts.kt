// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.content.Context
import android.graphics.Typeface

object Fonts {
    private var reg: Typeface? = null
    private var med: Typeface? = null
    private var bold: Typeface? = null
    fun regular(c: Context): Typeface { if (reg == null) reg = Typeface.createFromAsset(c.assets, "fonts/poppins_regular.ttf"); return reg!! }
    fun medium(c: Context): Typeface { if (med == null) med = Typeface.createFromAsset(c.assets, "fonts/poppins_medium.ttf"); return med!! }
    fun bold(c: Context): Typeface { if (bold == null) bold = Typeface.createFromAsset(c.assets, "fonts/poppins_bold.ttf"); return bold!! }
}