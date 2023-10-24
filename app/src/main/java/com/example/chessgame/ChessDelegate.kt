package com.example.chessgame

interface ChessDelegate {
    fun pieceAt (col: Int, row: Int) : ChessPiece?
}