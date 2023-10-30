package com.example.chessgame

data class ChessPiece(val col: Int, val row: Int, val player: Player, val chessman: Chessman, val resID: Int, var lives: Int = defaultLivesFor(chessman)) {
    companion object {
        fun defaultLivesFor(chessman: Chessman): Int {
            return when (chessman) {
                Chessman.PAWN -> 1
                Chessman.KNIGHT, Chessman.BISHOP, Chessman.ROOK -> 2
                Chessman.QUEEN -> 3
                Chessman.KING -> 1
                else -> 0
            }
        }
    }
}
