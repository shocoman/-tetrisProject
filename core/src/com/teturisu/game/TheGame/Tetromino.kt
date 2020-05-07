package com.teturisu.game.TheGame

import com.teturisu.game.Utilities
import java.util.*

class Tetromino(gameGrid: TetrisGrid) {
    val grid = gameGrid
    var tetromino = randomlyChooseTetromino()
    val tetrominoRows = tetromino.size
    val tetrominoCols = tetromino[0].size

    var tetrominoRow: Int = gameGrid.rows
    var tetrominoCol: Int = gameGrid.cols / 2 - tetrominoCols / 2

    var angle = 0

    lateinit var type: Types

    enum class Types {
        line, L, LFlipped, square, S, SFlipped, T;

        companion object {
            fun valueOf(value: Int) = Types.values().first { it.ordinal == value }
        }
    }

    private fun randomlyChooseTetromino(): Array<CharArray> {
        // choose random char from 'a' to 'e'

        val fruitNumber = Random().nextInt(7)
//        val fruitNumber = 2
        val ch = 'a' + fruitNumber.toChar().toInt()

        // 4 by 4 small grid
        val lineShape = arrayOf(
                String.format(" %c  ", ch).toCharArray(),
                String.format(" %c  ", ch).toCharArray(),
                String.format(" %c  ", ch).toCharArray(),
                String.format(" %c  ", ch).toCharArray())
        val LShape = arrayOf(
                String.format(" %c  ", ch).toCharArray(),
                String.format(" %c  ", ch).toCharArray(),
                String.format(" %c%c ", ch, ch).toCharArray(),
                String.format("    ", ch, ch).toCharArray())
        val LFlippedShape = arrayOf(
                String.format(" %c  ", ch).toCharArray(),
                String.format(" %c  ", ch).toCharArray(),
                String.format("%c%c  ", ch, ch).toCharArray(),
                String.format("    ", ch, ch).toCharArray())
        val squareShape = arrayOf(
                String.format(" %c%c ", ch, ch).toCharArray(),
                String.format(" %c%c ", ch, ch).toCharArray(),
                String.format("    ", ch, ch).toCharArray(),
                String.format("    ", ch, ch).toCharArray())
        val SShape = arrayOf(
                String.format("  %c ", ch).toCharArray(),
                String.format(" %c%c ", ch, ch).toCharArray(),
                String.format(" %c  ", ch).toCharArray(),
                String.format("    ", ch).toCharArray())
        val SFlippedShape = arrayOf(
                String.format(" %c  ", ch).toCharArray(),
                String.format(" %c%c ", ch, ch).toCharArray(),
                String.format("  %c ", ch).toCharArray(),
                String.format("    ", ch).toCharArray())
        val TShape = arrayOf(
                String.format(" %c  ", ch).toCharArray(),
                String.format(" %c%c ", ch, ch).toCharArray(),
                String.format(" %c  ", ch).toCharArray(),
                String.format("    ", ch).toCharArray())


        val shapes = arrayOf(
                lineShape, LShape, LFlippedShape, squareShape, SShape, SFlippedShape, TShape
        )

        val shapeNumber = Random().nextInt(7)
//        val shapeNumber = 6
        type = Types.valueOf(shapeNumber)

        return shapes[shapeNumber]
    }

    enum class moveDirection {
        NONE, LEFT, DOWN, RIGHT
    }

    fun move(dir: moveDirection = moveDirection.DOWN): Boolean {
        var hasMoved = false

        if (canMove(dir)) {
            remove()
            put(dir)
            hasMoved = true

        } else {
            hasMoved = false
        }

        return hasMoved
    }

    fun canMove(dir: moveDirection = moveDirection.NONE): Boolean {
        val (newRow, newCol) = when (dir) {
            moveDirection.LEFT -> Pair(tetrominoRow, tetrominoCol - 1)
            moveDirection.DOWN -> Pair(tetrominoRow - 1, tetrominoCol)
            moveDirection.RIGHT -> Pair(tetrominoRow, tetrominoCol + 1)
            moveDirection.NONE -> Pair(tetrominoRow, tetrominoCol)
        }

        var result = true
        for (row in 0 until tetrominoRows) {
            for (col in 0 until tetrominoCols) {
                val gridRowIndex = newRow - row - 1
                val gridColIndex = newCol + col

                val gridCell = grid.gameGrid.getOrNull(gridRowIndex)?.getOrNull(Utilities.mod(gridColIndex, grid.cols))

                if (gridCell != null && tetromino[row][col] != ' ' && gridCell.isUpperCase() || newRow == getLowestCell()) {
                    result = false
                }
            }
        }

        return result
    }

    fun getLowestCell(): Int {
        var lowestRow = 0
        for (row in 0 until tetrominoRows) {
            if (tetromino[row].any { it.isLetter() })
                lowestRow = row
        }
        return lowestRow
    }

    fun put(dir: moveDirection = moveDirection.NONE) {
        when (dir) {
            moveDirection.LEFT -> tetrominoCol -= 1
            moveDirection.RIGHT -> tetrominoCol += 1
            moveDirection.DOWN -> tetrominoRow -= 1
        }

        loopViaGrid(tetrominoRow, tetrominoCol) { row, col, gridRowIndex, gridColIndex ->

            val gridCell = grid.gameGrid.getOrNull(gridRowIndex)?.getOrNull(Utilities.mod(gridColIndex, grid.cols))
            val tetrominoCell = tetromino[row][col]

            if (gridCell != null && gridCell == ' ' && tetrominoCell != ' ') {
                grid.gameGrid[gridRowIndex][Utilities.mod(gridColIndex, grid.cols)] = tetrominoCell
            }
        }
    }

    fun remove() {
        loopViaGrid(tetrominoRow, tetrominoCol) { row, col, gridRowIndex, gridColIndex ->
            val gridCell = grid.gameGrid.getOrNull(gridRowIndex)?.getOrNull(Utilities.mod(gridColIndex, grid.cols))

            val tetrominoCell = tetromino[row][col]
            if (gridCell != null && gridCell != ' ' && tetrominoCell != ' ') {
                grid.gameGrid[gridRowIndex][Utilities.mod(gridColIndex, grid.cols)] = ' '
            }
        }
    }

    fun loopViaGrid(tetrominoRow: Int, tetrominoCol: Int, callback: (Int, Int, Int, Int) -> Unit) {
        for (row in 0 until tetrominoRows) {
            for (col in 0 until tetrominoCols) {
                val gridRowIndex = tetrominoRow - row - 1
                val gridColIndex = tetrominoCol + col

                callback(row, col, gridRowIndex, gridColIndex)
            }
        }
    }

    fun rotate() {

        fun Array<CharArray>.copy() = Array(size) { get(it).clone() }
        val newTetromino = tetromino.copy()

        // a little bit different rotation algorithm for line tetromino
        if (type == Types.line) {
            for (row in 0 until tetrominoRows) {
                for (col in 0 until tetrominoCols) {
                    newTetromino[row][col] = tetromino[col][row]
                }
            }
        } else {
            val (rows, cols) = Pair(tetrominoRows - 1, tetrominoCols - 1)
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    newTetromino[row][rows - col - 1] = tetromino[col][row]
                }
            }
        }


        remove()
        val oldTetromino = tetromino
        tetromino = newTetromino

        // just go back if there is not enough place for rotation
        if (!canMove(moveDirection.NONE))
            tetromino = oldTetromino

        put()
        angle += 90
    }

    fun quickFall() {
        val oldTetrominoRow = tetrominoRow

        var newRow = tetrominoRow
        while (canMove() && tetrominoRow >= 0) {
            tetrominoRow -= 1
        }

        newRow = tetrominoRow + 1
        tetrominoRow = oldTetrominoRow
        remove()
        tetrominoRow = newRow
        put()
    }
}