// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class TextToMorseActivity : Activity() {
    private val MORSE_MAP = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".", 'F' to "..-.",
        'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..",
        'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.", 'Q' to "--.-", 'R' to ".-.",
        'S' to "...", 'T' to "-", 'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
        'Y' to "-.--", 'Z' to "--..", '0' to "-----", '1' to ".----", '2' to "..---",
        '3' to "...--", '4' to "....-", '5' to ".....", '6' to "-....", '7' to "--...",
        '8' to "---..", '9' to "----.", ' ' to "/"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_to_morse)

        val etInput = findViewById<EditText>(R.id.etInput)
        val txtPreview = findViewById<TextView>(R.id.txtMorsePreview)
        val btnFlash = findViewById<Button>(R.id.btnFlash)
        val btnStop = findViewById<Button>(R.id.btnStop)

        etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString().uppercase()
                val morse = text.map { MORSE_MAP[it] ?: "" }.joinToString(" ")
                txtPreview.text = morse
            }
        })

        btnFlash.setOnClickListener {
            val text = etInput.text.toString()
            if (text.isNotBlank()) {
                TorchManager.flashMorse(this, text)
            }
        }

        btnStop.setOnClickListener {
            TorchManager.forceOff(this)
        }
    }
}