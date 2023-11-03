package com.example.chessgame

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import com.example.chessgame.R
import kotlin.math.sqrt

class Minigame2Activity : AppCompatActivity() {
    private var sensorManager: SensorManager? = null
    private var accelerometerSensor: Sensor? = null
    private var shakeCount = 0
    private var lastShakeTime = 0L
    private var lastAcceleration = 0.0f
    private var targetShakes = (1..5).random()
    private var gameEnded = false
    private val gameDuration = 10000L // 5 seconds
    private lateinit var failCaptureHandler: Handler

    // Threshold for acceleration difference to consider as a shake
    private val SHAKE_THRESHOLD_GRAVITY = 2.7f
    private val SHAKE_SLOP_TIME_MS = 500

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate the acceleration
            val acceleration = sqrt(x * x + y * y + z * z)

            // Calculate the change in acceleration
            val delta = acceleration - lastAcceleration

            // Normalize the change
            val deltaGravity = delta / SensorManager.GRAVITY_EARTH

            val now = SystemClock.elapsedRealtime()

            if (deltaGravity > SHAKE_THRESHOLD_GRAVITY) {
                // Ignore shakes too close to each other (500ms)
                if (lastShakeTime + SHAKE_SLOP_TIME_MS > now) {
                    return
                }

                lastShakeTime = now
                shakeCount++

                if (shakeCount >= targetShakes) {
                    endGame(true)
                }
            }

            // Update the last acceleration to the current one
            lastAcceleration = acceleration
        }
    }

    private fun endGame(success: Boolean) {
        if (!gameEnded) {
            gameEnded = true
            failCaptureHandler.removeCallbacksAndMessages(null)
            sensorManager?.unregisterListener(sensorEventListener)
            setResult(if (success) RESULT_OK else RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_minigame2) // Make sure you have a corresponding layout file

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)

        failCaptureHandler = Handler()
        failCaptureHandler.postDelayed({ endGame(false) }, gameDuration)
    }

    override fun onDestroy() {
        super.onDestroy()
        failCaptureHandler.removeCallbacksAndMessages(null)
        sensorManager?.unregisterListener(sensorEventListener)
    }
}
