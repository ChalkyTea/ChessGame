package com.example.chessgame

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

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), ChessDelegate, OnPieceCapturedListener {

    private lateinit var chessView: ChessView
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    override fun onPieceCaptured(capturedPiece: ChessPiece) {
        val intent = Intent(this, CaptureActivity::class.java)  // Note the .java
        startActivity(intent)
        Log.d(TAG, "Attempting to capture piece")

    }



    override fun pieceAt(square: Square): ChessPiece? = ChessGame.pieceAt(square)

    override fun movePiece(from: Square, to: Square) {
        ChessGame.movePiece(from, to)
        chessView.invalidate()
    }
}
