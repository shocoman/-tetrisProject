package com.teturisu.game


class TetrisGrid(var rows: Int, var cols: Int) {
    var gameGrid: Array<CharArray> = Array(rows) { CharArray(cols) }

    lateinit var activeTetromino: Tetromino

    var gameOver = false

    init {
        initGrid()
        spawnTetromino()
    }

    private fun initGrid() {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                gameGrid[row][col] = ' '
//                if (row == 0 && col < 3) gameGrid[row][col] = 'F'
            }
        }
    }

    fun fall() {
        if (!activeTetromino.move()) {
            makeThemStop()
            spawnTetromino()
        }
    }

    fun quickFall() {
        activeTetromino.quickFall()
        Timer.time = Timer.timeInterval*8/10
    }

    fun makeThemStop() {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                gameGrid[row][col] = Character.toUpperCase(gameGrid[row][col])
            }
        }

        clearFullLines()
    }

    fun shift(dir: Tetromino.moveDirection) {
        activeTetromino.move(dir)
    }

    fun spawnTetromino() {
        activeTetromino = Tetromino(this)
        if (activeTetromino.canMove(Tetromino.moveDirection.NONE))
            activeTetromino.put()
        else {
            println("Game Over?!")
            gameOver = true

        }
    }

    fun rotate() {
        activeTetromino.rotate()
    }

    fun clearFullLines() {
        val removedLines = mutableListOf<Int>()

        for (row in 0 until rows) {
            gameGrid[row].all { it.isUpperCase() }.let {
                if (it) {
                    removedLines.add(row)
                }
            }
        }

        Score.increaseScore(removedLines.size)
        for (lineNumber in removedLines) {
            emptyRow(lineNumber)
        }

        var lastRow = 0
        var nextRow = findNextNotEmptyRow(lastRow)
        while (nextRow != -1) {
            if (rowIsEmpty(lastRow)) {
                moveRowTo(nextRow, lastRow)
            }

            lastRow++
            nextRow = findNextNotEmptyRow(lastRow)
        }
    }

    fun moveRowTo(sourceRow: Int, destRow: Int) {
        for (col in 0 until cols) {
            if (!gameGrid[sourceRow][col].isUpperCase()) continue
            gameGrid[destRow][col] = gameGrid[sourceRow][col]
            gameGrid[sourceRow][col] = ' '
        }
    }

    fun rowIsEmpty(row: Int): Boolean {
        return !gameGrid[row].any { it.isUpperCase() }
    }

    fun emptyRow(row: Int) {
        for (col in 0 until cols) {
            if (gameGrid[row][col].isUpperCase())
                gameGrid[row][col] = ' '
        }

    }

    fun findNextNotEmptyRow(startRow: Int): Int {
        for (row in startRow until rows) {
            if (!rowIsEmpty(row)) {
                return row
            }
        }

        return -1
    }

    fun moveTetrominoTo(col: Int) {
        val oldTetrominoCol = activeTetromino.tetrominoCol
        activeTetromino.remove()
        activeTetromino.tetrominoCol = col
        if (!activeTetromino.canMove()) {
            activeTetromino.tetrominoCol = oldTetrominoCol
        }

        activeTetromino.put()
    }
}