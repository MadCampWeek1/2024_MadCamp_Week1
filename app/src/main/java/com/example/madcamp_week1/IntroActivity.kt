package com.example.madcamp_week1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val titleText: TextView = findViewById(R.id.titleText)
        val customFont = ResourcesCompat.getFont(this, R.font.instagram_font)
        titleText.typeface = customFont

        val startButton: Button = findViewById(R.id.buttonStart)
        startButton.setOnClickListener {
            // Navigate to MainActivity
            val intent = Intent(this@IntroActivity, MainActivity::class.java)
            startActivity(intent)

            // Finish IntroActivity
            finish()
        }
    }
}
