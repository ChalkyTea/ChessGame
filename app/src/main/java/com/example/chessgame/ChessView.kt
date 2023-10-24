package com.example.chessgame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import java.lang.Integer.min

class ChessView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val scaleFactor = .9f
    private var originX = 20f
    private var originY = 200f
    private var cellSide: Float = 130f
    private val lightColor = Color.parseColor("#EEEEEE")
    private val darkColor = Color.parseColor("#444444")
    private val imgResIDs = setOf(
        R.drawable.bishop_black,
        R.drawable.bishop_white,
        R.drawable.king_black,
        R.drawable.king_white,
        R.drawable.queen_white,
        R.drawable.queen_black,
        R.drawable.rook_black,
        R.drawable.rook_white,
        R.drawable.knight_black,
        R.drawable.knight_white,
        R.drawable.pawn_black,
        R.drawable.pawn_white,
        )
    private val bitmaps = mutableMapOf<Int, Bitmap>()
    private val paint = Paint()
    private var fromCol: Int  = -1
    private var fromRow: Int  = -1

    var chessDelegate: ChessDelegate? = null

    init{
        loadBitmaps()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas) {
        canvas ?: return
        val chessBoardSide = min(width, height) * scaleFactor
        cellSide = chessBoardSide / 8f
        originX = (width - chessBoardSide)/2f
        originY = (height - chessBoardSide)/2f

        drawChessboard(canvas)
        drawPieces(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        when (event.action){
            MotionEvent.ACTION_DOWN -> {
                fromCol = ((event.x - originX)/ cellSide).toInt()
                fromRow = 7 - ((event.y - originY)/ cellSide).toInt()
            }
            MotionEvent.ACTION_MOVE -> {

            }

            MotionEvent.ACTION_UP-> {
                val col = ((event.x - originX)/ cellSide).toInt()
                val row = 7 - ((event.y - originY)/ cellSide).toInt()
                Log.d(TAG, "From ($fromCol, $fromRow) to ($col, $row)")
                chessDelegate?.movePiece(fromCol, fromRow, col, row)
            }

        }
        return true
    }

    private fun drawPieces(canvas:Canvas){
        for(row in 0..7){
            for (col in 0..7){
                chessDelegate?.pieceAt(col,row)?.let{drawPiecesAt(canvas, col,row,it.resID)}
            }
        }
    }

    private fun drawPiecesAt(canvas:Canvas, col:Int, row: Int, resID:Int){
        val bitmap = bitmaps[resID]!!
        canvas.drawBitmap(bitmap, null, RectF(originX + col * cellSide,originY +(7- row) * cellSide,originX + (col + 1) * cellSide,originY + (7-row + 1)* cellSide), paint)
    }

    private fun loadBitmaps(){
        imgResIDs.forEach{
            bitmaps[it] = BitmapFactory.decodeResource(resources, it)

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawChessboard(canvas:Canvas){
        for (row in 0..7){
            for (col in 0..7){
                drawSquareAt(canvas,row,col,(row+col)%2 ==1)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawSquareAt(canvas: Canvas, col:Int, row: Int, isDark:Boolean){
        paint.color = if(isDark) darkColor else lightColor
        canvas?.drawRect(originX + col * cellSide, originY + row * cellSide, originX + (col+1) * cellSide,originY+(row+1)*cellSide, paint)
    }
}