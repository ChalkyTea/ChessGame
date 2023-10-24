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
import android.view.View
import androidx.annotation.RequiresApi

class ChessView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private final val originX = 20f
    private final val originY = 200f
    private final val cellSide: Float = 130f
    @RequiresApi(Build.VERSION_CODES.O)
    private final val lightColor = Color.argb(1f,.9f,.9f, .9f)
    @RequiresApi(Build.VERSION_CODES.O)
    private final val darkColor = Color.argb(1f,.7f,.7f, .7f)
    private final val imgResIDs = setOf(
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
    private final val bitmaps = mutableMapOf<Int, Bitmap>()
    private final val paint = Paint()

    var chessDelegate: ChessDelegate? = null

    init{
        loadBitmaps()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas) {
        drawChessboard(canvas)
        drawPieces(canvas)

    }
    private fun drawPieces(canvas:Canvas?){
        for(row in 0..7){
            for (col in 0..7){
                chessDelegate?.pieceAt(col,row)?.let{drawPiecesAt(canvas, col,row,it.resID)}
            }
        }
    }

    private fun drawPiecesAt(canvas:Canvas?, col:Int, row: Int, resID:Int){
        val bitmap = bitmaps[resID]!!
        canvas?.drawBitmap(bitmap, null, RectF(originX + col * cellSide,originY +(7- row) * cellSide,originX + (col + 1) * cellSide,originY + (7-row + 1)* cellSide), paint)
    }

    private fun loadBitmaps(){
        imgResIDs.forEach{
            bitmaps[it] = BitmapFactory.decodeResource(resources, it)

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawChessboard(canvas:Canvas?){
        for (row in 0..7){
            for (col in 0..7){
                drawSquareAt(canvas,row,col,(row+col)%2 ==1)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawSquareAt(canvas: Canvas?, col:Int, row: Int, isDark:Boolean){
        paint.color = if(isDark) darkColor else lightColor
        canvas?.drawRect(originX + col * cellSide, originY + row * cellSide, originX + (col+1) * cellSide,originY+(row+1)*cellSide, paint)
    }
}