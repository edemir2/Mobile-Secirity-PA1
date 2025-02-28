package kittoku.osc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import kittoku.osc.R

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val backButton: Button = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish() // Closes HelpActivity and returns to the main page
        }
    }
}
