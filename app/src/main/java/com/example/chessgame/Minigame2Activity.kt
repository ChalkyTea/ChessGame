package com.example.chessgame

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class Minigame2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_minigame2)

        // Simulate the mini-game with a 5-second delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent()
            intent.putExtra("RESULT", Math.random() > 0.5)  // Simulating a random result for now
            setResult(Activity.RESULT_OK, intent)
            finish()  // This will close the CaptureActivity and return to MainActivity
        }, 5000)  // 5 seconds delay
    }
}