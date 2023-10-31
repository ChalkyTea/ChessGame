package com.example.chessgame

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Point
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.Surface
import android.view.View
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
class Minigame1Activity : AppCompatActivity() {
    private var xPos = 0f
    private var yPos = 0f
    private var xMax = 0f
    private var yMax = 0f
    private var statusBarHeight = 75
    private lateinit var ball: Bitmap

    private var sensorManager: SensorManager? = null
    private var accelerometerSensor: Sensor? = null
    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor == accelerometerSensor) {
                val rotation = windowManager.defaultDisplay.rotation
                val accelerationX = event.values[0]
                val accelerationY = event.values[1]

                val displacementX: Float
                val displacementY: Float

                when (rotation) {
                    Surface.ROTATION_0 -> {
                        displacementX = accelerationX * 2
                        displacementY = accelerationY * 2
                    }
                    Surface.ROTATION_90 -> {
                        displacementX = -accelerationY * 2
                        displacementY = accelerationX * 2
                    }
                    Surface.ROTATION_180 -> {
                        displacementX = -accelerationX * 2
                        displacementY = -accelerationY * 2
                    }
                    Surface.ROTATION_270 -> {
                        displacementX = accelerationY * 2
                        displacementY = -accelerationX * 2
                    }
                    else -> {
                        displacementX = 0f
                        displacementY = 0f
                    }
                }

                xPos += displacementX
                yPos += displacementY

                if (xPos < 0) xPos = 0f
                if (xPos > xMax) xPos = xMax
                if (yPos < 0) yPos = 0f
                if (yPos > yMax) yPos = yMax

                Log.d("BallPosition", "xPos: $xPos, yPos: $yPos")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val ballView = CustomBallView(this)
        setContentView(ballView)

        val tv = TypedValue()
        this.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
        val actionBarHeight = resources.getDimensionPixelSize(tv.resourceId)

        val point = Point()
        val display = windowManager.defaultDisplay
        display.getSize(point)
        xMax = point.x.toFloat() - 100
        yMax = point.y.toFloat() - 100 - statusBarHeight - actionBarHeight

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)

        Handler().postDelayed({
            setResult(RESULT_OK)
            finish()
        }, 5000)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(sensorEventListener)
    }

    private inner class CustomBallView(context: Context?) : View(context) {
        override fun onDraw(canvas: Canvas) {
            canvas.drawBitmap(ball, xPos, yPos, null)
            invalidate()
        }

        init {
            val ballSrc = BitmapFactory.decodeResource(resources, R.drawable.ball)
            val ballWidth = 100
            val ballHeight = 100
            ball = Bitmap.createScaledBitmap(ballSrc, ballWidth, ballHeight, true)
        }
    }
}
