package com.example.chessgame

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.Surface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt
import kotlin.random.Random

class Minigame1Activity : AppCompatActivity() {
    private var xPos = 0f
    private var yPos = 0f
    private var xMax = 0f
    private var yMax = 0f
    private lateinit var ball: Bitmap
    private val targetRadius = 50 // radius of the target circle
    private val ballRadius = 50 // radius of the ball
    private var gameEnded = false

    private var sensorManager: SensorManager? = null
    private var accelerometerSensor: Sensor? = null
    private lateinit var ballView: CustomBallView
    private lateinit var failCaptureHandler: Handler

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor == accelerometerSensor && !gameEnded) {
                handleSensorEvent(event)
            }
        }

        fun isBallTouchingCircle(ballX: Float, ballY: Float, circleX: Float, circleY: Float, circleRadius: Float, ballRadius: Float): Boolean {
            val dx = circleX - (ballX + ballRadius)
            val dy = circleY - (ballY + ballRadius)
            return sqrt(dx * dx + dy * dy) <= circleRadius + ballRadius
        }
    }

    private fun handleSensorEvent(event: SensorEvent) {
        val rotation = windowManager.defaultDisplay.rotation
        val accelerationX = event.values[0]
        val accelerationY = event.values[1]


        val displacementX: Float
        val displacementY: Float

        when (rotation) {
            Surface.ROTATION_0 -> {
                val scaleFactor = Random.nextDouble(0.5, 5.0).toFloat()
                displacementX = accelerationX * scaleFactor
                displacementY = accelerationY * scaleFactor
            }
            Surface.ROTATION_90 -> {
                val scaleFactor = Random.nextDouble(0.5, 5.0).toFloat()
                displacementX = -accelerationY * scaleFactor
                displacementY = accelerationX * scaleFactor
            }
            Surface.ROTATION_180 -> {
                val scaleFactor = Random.nextDouble(0.5, 5.0).toFloat()
                displacementX = -accelerationX * scaleFactor
                displacementY = -accelerationY * scaleFactor
            }
            Surface.ROTATION_270 -> {
                val scaleFactor = Random.nextDouble(0.5, 5.0).toFloat()
                displacementX = accelerationY * scaleFactor
                displacementY = -accelerationX * scaleFactor
            }
            else -> {
                displacementX = 0f
                displacementY = 0f
            }
        }

        xPos += displacementX
        yPos += displacementY

        // Boundaries check
        if (xPos < 0) xPos = 0f
        if (xPos > xMax - ball.width) xPos = xMax - ball.width
        if (yPos < 0) yPos = 0f
        if (yPos > yMax - ball.height) yPos = yMax - ball.height

        ballView.invalidate()

        // Check if the ball is touching the target circle
        if (sensorEventListener.isBallTouchingCircle(xPos, yPos, xMax / 2, yMax / 2, targetRadius.toFloat(), ballRadius.toFloat())) {
            gameEnded = true
            failCaptureHandler.removeCallbacksAndMessages(null)
            val intent = Intent()
            intent.putExtra("RESULT", true) // Add this line to pass the result back
            setResult(RESULT_OK, intent) // Pass intent here
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//
//        val display = windowManager.defaultDisplay
//        val size = Point()
//        display.getSize(size)
//        xMax = size.x.toFloat()
//        yMax = size.y.toFloat()
//
//        // Get the status bar and action bar height
//        val tv = TypedValue()
//        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
//            yMax -= TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
//        }
//        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
//        if (resourceId > 0) {
//            yMax -= resources.getDimensionPixelSize(resourceId)
//        }
//
//        ballView = CustomBallView(this)
//        setContentView(ballView)
//
//        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        sensorManager?.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
//
//        // Set up the handler to end the game after 5 seconds if the ball hasn't touched the circle
//        failCaptureHandler = Handler()
//        failCaptureHandler.postDelayed({
//            if (!gameEnded) {
//                gameEnded = true
//                setResult(RESULT_CANCELED) // Player fails to touch the circle within time, loses their piece
//                finish()
//            }
//        }, 5000) // Corrected the time to 5 seconds

        showGameRulesPopup()
    }


    private fun showGameRulesPopup() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Game Rules")
        builder.setMessage("Move the ball to the center to to win within 10 seconds. If you cannot, you lose!")

        builder.setPositiveButton("OK") { dialog, which ->
            startGame()
        }

        builder.setCancelable(false) // Prevent the dialog from being canceled on back press
        builder.show()
    }

    private fun startGame() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        xMax = size.x.toFloat()
        yMax = size.y.toFloat()

        // Subtract the status bar and action bar height
        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            yMax -= TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            yMax -= resources.getDimensionPixelSize(resourceId)
        }

        ballView = CustomBallView(this)
        setContentView(ballView)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)

        failCaptureHandler = Handler()
        failCaptureHandler.postDelayed({
            if (!gameEnded) {
                gameEnded = true
                setResult(RESULT_CANCELED) // Player fails to touch the circle within time, loses their piece
                finish()
            }
        }, 10000) // Ensure this is the correct time you want for the delay
    }

    override fun onDestroy() {
        super.onDestroy()
        failCaptureHandler.removeCallbacksAndMessages(null)
        sensorManager?.unregisterListener(sensorEventListener)
    }

    private inner class CustomBallView(context: Context) : View(context) {
        private val paintCircle = Paint()

        init {
            val ballSrc = BitmapFactory.decodeResource(resources, R.drawable.ball)
            ball = Bitmap.createScaledBitmap(ballSrc, ballRadius * 2, ballRadius * 2, false)

            paintCircle.color = Color.RED
            paintCircle.style = Paint.Style.FILL
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            // Draw the target circle in the center
            canvas.drawCircle(xMax / 2, yMax / 2, targetRadius.toFloat(), paintCircle)

            // Draw the ball
            canvas.drawBitmap(ball, xPos, yPos, null)
        }
    }
}
