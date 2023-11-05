package com.example.chessgame

import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import kotlin.math.abs

object ChessGame {
    private var piecesBox = mutableSetOf<ChessPiece>()
    private val onPieceCapturedListeners = mutableListOf<OnPieceCapturedListener>()

    fun addOnPieceCapturedListener(listener: OnPieceCapturedListener) {
        onPieceCapturedListeners.add(listener)
    }

    fun removeOnPieceCapturedListener(listener: OnPieceCapturedListener) {
        onPieceCapturedListeners.remove(listener)
    }

    init {
        reset()
    }

    fun clear() {
        piecesBox.clear()
    }

    fun addPiece(piece: ChessPiece) {
        piecesBox.add(piece)
    }

    fun tryCapture(attacker: ChessPiece, defender: ChessPiece): Boolean {
        var gamesToPlay = defender.lives

        while (gamesToPlay > 0) {
            val gameResult = playMiniGame() // This function will handle the minigame logic
            if (gameResult) {
                gamesToPlay--
            } else {
                attacker.lives--
                if (attacker.lives == 0) {
                    // The attacking piece has been destroyed
                    return false
                }
            }
        }
        // The defending piece has been captured
        return true
    }

    fun playMiniGame(): Boolean {
        // Placeholder for minigame logic
        // Return true if the minigame is won, false otherwise
        return Math.random() > 0.5 // 50% chance to win for now
    }


    private fun privateMovePiece(fromCol: Int, fromRow: Int, toCol: Int, toRow: Int) {
        if (fromCol == toCol && fromRow == toRow) return
        val movingPiece = pieceAt(fromCol, fromRow) ?: return

        pieceAt(toCol, toRow)?.let {
            if (it.player == movingPiece.player) {
                return
            }
            piecesBox.remove(it)
            onPieceCapturedListeners.forEach { listener ->
                Log.d("ChessGame", "Notifying listener about captured piece") // <-- Add this log
                listener.onPieceCaptured(it)
            }
        }

        piecesBox.remove(movingPiece)
        addPiece(movingPiece.copy(col = toCol, row = toRow))
    }

    private fun canKnightMove(from: Square, to: Square): Boolean {
        return abs(from.col - to.col) == 2 && abs(from.row - to.row) == 1 ||
                abs(from.col - to.col) == 1 && abs(from.row - to.row) == 2
    }

    private fun canRookMove(from: Square, to: Square): Boolean {
        if (from.col == to.col && isClearVerticallyBetween(from, to) ||
            from.row == to.row && isClearHorizontallyBetween(from, to)) {
            return true
        }
        return false
    }

    private fun isClearVerticallyBetween(from: Square, to: Square): Boolean {
        if (from.col != to.col) return false
        val gap = abs(from.row - to.row) - 1
        if (gap == 0 ) return true
        for (i in 1..gap) {
            val nextRow = if (to.row > from.row) from.row + i else from.row - i
            if (pieceAt(Square(from.col, nextRow)) != null) {
                return false
            }
        }
        return true
    }

    private fun isClearHorizontallyBetween(from: Square, to: Square): Boolean {
        if (from.row != to.row) return false
        val gap = abs(from.col - to.col) - 1
        if (gap == 0 ) return true
        for (i in 1..gap) {
            val nextCol = if (to.col > from.col) from.col + i else from.col - i
            if (pieceAt(Square(nextCol, from.row)) != null) {
                return false
            }
        }
        return true
    }

    private fun isClearDiagonally(from: Square, to: Square): Boolean {
        if (abs(from.col - to.col) != abs(from.row - to.row)) return false
        val gap = abs(from.col - to.col) - 1
        for (i in 1..gap) {
            val nextCol = if (to.col > from.col) from.col + i else from.col - i
            val nextRow = if (to.row > from.row) from.row + i else from.row - i
            if (pieceAt(nextCol, nextRow) != null) {
                return false
            }
        }
        return true
    }

    private fun canBishopMove(from: Square, to: Square): Boolean {
        if (abs(from.col - to.col) == abs(from.row - to.row)) {
            return isClearDiagonally(from, to)
        }
        return false
    }

    private fun canQueenMove(from: Square, to: Square): Boolean {
        return canRookMove(from, to) || canBishopMove(from, to)
    }

    private fun canKingMove(from: Square, to: Square): Boolean {
        if (canQueenMove(from, to)) {
            val deltaCol = abs(from.col - to.col)
            val deltaRow = abs(from.row - to.row)
            return deltaCol == 1 && deltaRow == 1 || deltaCol + deltaRow == 1
        }
        return false
    }

    fun canPawnMove(from: Square,to: Square):Boolean{
        val deltaCol = abs(from.col - to.col)
        val deltaRow = abs(from.row - to.row)

        val movingPiece = pieceAt(from) ?: return false
        val player = movingPiece.player
        val direction = if (player == Player.WHITE) 1 else -1

        // Moving forward
        if (deltaCol == 0) {
            // Single step forward
            if (deltaRow == 1 && to.row - from.row == direction && pieceAt(to) == null) {
                return true
            }
            // Double step forward from starting position
            if ((from.row == 1 || from.row == 6) && deltaRow == 2 && to.row - from.row == direction * 2 && pieceAt(to) == null) {
                return true
            }
        }
        // Diagonal attack
        else if (deltaCol == 1 && deltaRow == 1) {
            val targetPiece = pieceAt(to)
            if (targetPiece != null && targetPiece.player != player && to.row - from.row == direction) {
                return true
            }
        }
        return false
    }



    fun canMove(from: Square, to: Square): Boolean {
        if (from.col == to.col && from.row == to.row) {
            return  false
        }
        val movingPiece = pieceAt(from) ?: return false
        return when(movingPiece.chessman) {
            Chessman.KNIGHT -> canKnightMove(from, to)
            Chessman.ROOK -> canRookMove(from, to)
            Chessman.BISHOP -> canBishopMove(from, to)
            Chessman.QUEEN -> canQueenMove(from, to)
            Chessman.KING -> canKingMove(from, to)
            Chessman.PAWN -> canPawnMove(from, to)
            else -> false
        }

    }

    fun movePiece(from: Square, to: Square, resultOfCaptureGame: Boolean? = null): Boolean {
        if (!canMove(from, to)) {
            return false
        }

        val attacker = pieceAt(from)
        val defender = pieceAt(to)

        if (defender != null && attacker?.player != defender.player) {
            if (resultOfCaptureGame == null && shouldTryCapture(attacker!!, defender)) {
                // Notify the UI layer to start the mini-game.
                return true
            } else if (resultOfCaptureGame != null) {
                if (resultOfCaptureGame) {
                    piecesBox.remove(defender)
                    privateMovePiece(from.col, from.row, to.col, to.row)
                } else {
                    piecesBox.remove(attacker)
                }
            }
        } else {
            privateMovePiece(from.col, from.row, to.col, to.row)
        }
        return false
    }

    fun shouldTryCapture(attacker: ChessPiece, defender: ChessPiece): Boolean {
        return attacker.lives >= 1 || defender.lives >= 1
    }



    fun reset() {
        clear()
        for (i in 0 until 2) {
            addPiece(ChessPiece(0 + i * 7, 0, Player.WHITE, Chessman.ROOK, R.drawable.rook_white))
            addPiece(ChessPiece(0 + i * 7, 7, Player.BLACK, Chessman.ROOK, R.drawable.rook_black))

            addPiece(ChessPiece(1 + i * 5, 0, Player.WHITE, Chessman.KNIGHT, R.drawable.knight_white))
            addPiece(ChessPiece(1 + i * 5, 7, Player.BLACK, Chessman.KNIGHT, R.drawable.knight_black))

            addPiece(ChessPiece(2 + i * 3, 0, Player.WHITE, Chessman.BISHOP, R.drawable.bishop_white))
            addPiece(ChessPiece(2 + i * 3, 7, Player.BLACK, Chessman.BISHOP, R.drawable.bishop_black))
        }

        for (i in 0 until 8) {
            addPiece(ChessPiece(i, 1, Player.WHITE, Chessman.PAWN, R.drawable.pawn_white))
            addPiece(ChessPiece(i, 6, Player.BLACK, Chessman.PAWN, R.drawable.pawn_black))
        }

        addPiece(ChessPiece(3, 0, Player.WHITE, Chessman.QUEEN, R.drawable.queen_white))
        addPiece(ChessPiece(3, 7, Player.BLACK, Chessman.QUEEN, R.drawable.queen_black))
        addPiece(ChessPiece(4, 0, Player.WHITE, Chessman.KING, R.drawable.king_white))
        addPiece(ChessPiece(4, 7, Player.BLACK, Chessman.KING, R.drawable.king_black))
    }

    fun pieceAt(square: Square): ChessPiece? {
        return pieceAt(square.col, square.row)
    }

    private fun pieceAt(col: Int, row: Int): ChessPiece? {
        for (piece in piecesBox) {
            if (col == piece.col && row == piece.row) {
                return  piece
            }
        }
        return null
    }

    fun pgnBoard(): String {
        var desc = " \n"
        desc += "  a b c d e f g h\n"
        for (row in 7 downTo 0) {
            desc += "${row + 1}"
            desc += boardRow(row)
            desc += " ${row + 1}"
            desc += "\n"
        }
        desc += "  a b c d e f g h"

        return desc
    }

    override fun toString(): String {
        var desc = " \n"
        for (row in 7 downTo 0) {
            desc += "$row"
            desc += boardRow(row)
            desc += "\n"
        }
        desc += "  0 1 2 3 4 5 6 7"

        return desc
    }

    private fun boardRow(row: Int) : String {
        var desc = ""
        for (col in 0 until 8) {
            desc += " "
            desc += pieceAt(col, row)?.let {
                val white = it.player == Player.WHITE
                when (it.chessman) {
                    Chessman.KING -> if (white) "k" else "K"
                    Chessman.QUEEN -> if (white) "q" else "Q"
                    Chessman.BISHOP -> if (white) "b" else "B"
                    Chessman.ROOK -> if (white) "r" else "R"
                    Chessman.KNIGHT -> if (white) "n" else "N"
                    Chessman.PAWN -> if (white) "p" else "P"
                }
            } ?: "."
        }
        return desc
    }
}