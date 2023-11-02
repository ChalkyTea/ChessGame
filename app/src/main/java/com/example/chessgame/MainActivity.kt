package com.example.chessgame

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import java.io.PrintWriter
import java.net.ConnectException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.Scanner
import java.util.concurrent.Executors
import kotlin.random.Random

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), ChessDelegate, OnPieceCapturedListener {

    private lateinit var chessView: ChessView
    private lateinit var resetButton: Button
    private  val CAPTURE_REQUEST_CODE = 100
    private lateinit var from: Square
    private lateinit var to: Square



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repeat(20) {
            Log.d(TAG, "Test random number: ${Random.nextInt(1, 3)}")
        }

        chessView = findViewById<ChessView>(R.id.chess_view)
        resetButton = findViewById<Button>(R.id.reset_button)
        chessView.chessDelegate = this

        resetButton.setOnClickListener {
            ChessGame.reset()
            chessView.invalidate()
        }

        ChessGame.addOnPieceCapturedListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ChessGame.removeOnPieceCapturedListener(this)
    }

//    override fun onPieceCaptured(capturedPiece: ChessPiece) {
//        val intent = Intent(this, CaptureActivity::class.java)  // Note the .java
//        startActivity(intent)
//        Log.d(TAG, "Attempting to capture piece")
//
//    }

    override fun pieceAt(square: Square): ChessPiece? = ChessGame.pieceAt(square)


    override fun onPieceCaptured(capturedPiece: ChessPiece) {
        val intent = Intent(this, CaptureActivity::class.java)
        startActivityForResult(intent, CAPTURE_REQUEST_CODE)
        Log.d(TAG, "Starting CaptureActivity for piece capture")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult called with requestCode: $requestCode, resultCode: $resultCode")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAPTURE_REQUEST_CODE) {
            val miniGameResult = when (resultCode) {
                Activity.RESULT_OK -> data?.getBooleanExtra("RESULT", false) ?: false
                Activity.RESULT_CANCELED -> false
                else -> false
            }
            ChessGame.movePiece(from, to, miniGameResult) // Finalize the move with the result
            chessView.invalidate()
        }
    }


    override fun movePiece(from: Square, to: Square) {
        this.from = from
        this.to = to
//        val needsMiniGame = ChessGame.movePiece(from, to)
//        if (needsMiniGame) {
//            Log.d(TAG, "Starting CaptureActivity due to move")
//            val intent = Intent(this, CaptureActivity::class.java)
//
//            startActivityForResult(intent, CAPTURE_REQUEST_CODE)
//        } else {
//            chessView.invalidate()
//        }
        val needsMiniGame = ChessGame.movePiece(from, to)
        if (needsMiniGame) {
//            val randomMiniGame = (1..2).random() // Assuming 2 mini-games
            val randomMiniGame = Random.nextInt(1, 3)
            Log.d(TAG, "Selected mini-game: $randomMiniGame")
            val MiniGame = 1
            val intent = when (MiniGame) {
                1 -> Intent(this, Minigame1Activity::class.java)
                2 -> Intent(this, Minigame2Activity::class.java)
                else -> throw IllegalArgumentException("Invalid mini-game number")
            }
            startActivityForResult(intent, CAPTURE_REQUEST_CODE)
        } else {
            chessView.invalidate()
        }

    }



//    fun movePiece(from: Square, to: Square, miniGameResult: Boolean) {
//        // Handle the mini-game result here. For example:
//        if (miniGameResult) {
//            // Do something when mini-game is won
//        } else {
//            // Do something when mini-game is lost
//        }
//        movePiece(from, to) // This will call the above method
//    }


}
