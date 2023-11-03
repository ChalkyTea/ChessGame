package com.example.chessgame

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Minigame3Activity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private lateinit var countdownText: TextView
    private var countdownTimer: CountDownTimer? = null
    private var countdownTimeInSeconds: Int = 0 // The random countdown time
    private var endTime: Long = 0 // The time when the countdown finishes
    private var isDark: Boolean = false // Flag to check if it's dark
    private var isCountdownStarted: Boolean = false // Flag to indicate that countdown has started
    private var isCountdownFinished: Boolean = false // Flag to indicate that countdown has finished

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_minigame3)
        countdownText = findViewById(R.id.countdown_text)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        // Determine the random countdown time on creation and display it
        countdownTimeInSeconds = (5..10).random()
        countdownText.text = "Time: $countdownTimeInSeconds seconds"
        // Do not start countdown immediately; wait for it to be dark
    }

    private fun startCountdown() {
        isCountdownStarted = true
        countdownTimer = object : CountDownTimer(countdownTimeInSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownText.text = "Time: ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                countdownText.text = "Flip now!"
                endTime = System.currentTimeMillis()
                isCountdownFinished = true
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        countdownTimer?.cancel()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val lightValue = event.values[0]
        if (!isCountdownStarted && lightValue <= DARK_THRESHOLD) {
            isDark = true
            val randomTime = (5..10).random()
            startCountdown()
        } else if (isCountdownStarted && !isCountdownFinished && lightValue > DARK_THRESHOLD) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        } else if (isCountdownFinished && lightValue > DARK_THRESHOLD) {
            val currentTime = System.currentTimeMillis()
            // Check if the flip occurred within the 0.5 second threshold
            if (currentTime >= (endTime - TIME_WINDOW) && currentTime <= (endTime + TIME_WINDOW)) {
                setResult(Activity.RESULT_OK)
            } else {
                setResult(Activity.RESULT_CANCELED)
            }
            finish()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used in this context
    }

    companion object {
        private const val DARK_THRESHOLD = 5 // Define suitable threshold for darkness
        private const val TIME_WINDOW = 1000 // Plus minus threshold in milliseconds
    }
}
