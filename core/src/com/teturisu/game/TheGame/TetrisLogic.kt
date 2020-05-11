package com.teturisu.game.TheGame


class TetrisLogic(var rows: Int, var cols: Int) {
    var gameGrid: Array<CharArray> = Array(rows) { CharArray(cols) }

    lateinit var activeTetromino: Tetromino
    var linesToRemove = emptyArray<Int>()
    var spawnNextTurn = false
    var gameSpeed = 2

    var gamePaused = false
    var gameOver = false

    init {
        initGrid()
        spawnTetromino()
    }

    private fun initGrid() {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                gameGrid[row][col] = ' '
            }
        }
    }

    fun fall() {
        if (spawnNextTurn) {
            spawnTetromino()
            spawnNextTurn = false
        } else {
            val hasMoved = activeTetromino.move()

            if (!hasMoved) {
                makeThemStop()
                if (linesToRemove.isEmpty())
                    spawnTetromino()
                else
                    spawnNextTurn = true
            }
        }
    }

    fun quickFall() {
        if (!spawnNextTurn) {
            activeTetromino.quickFall()
            Timer.time = Timer.timeInterval * 8 / 10
        }
    }

    fun makeThemStop() {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                gameGrid[row][col] = Character.toUpperCase(gameGrid[row][col])
            }
        }

        markFullLines()
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

        removeFullLines()
    }

    fun rotate() {
        activeTetromino.rotate()
    }


    fun markFullLines(){
        linesToRemove = (0 until rows).filter { row -> gameGrid[row].all{it.isUpperCase()} }.toTypedArray()
    }

    fun removeFullLines() {
//        val linesToRemove = mutableListOf<Int>()
//
//        for (row in 0 until rows) {
//            gameGrid[row].all { it.isUpperCase() }.let {
//                if (it) {
//                    linesToRemove.add(row)
//                }
//            }
//        }



        Score.increaseScore(linesToRemove.size)
        for (lineNumber in linesToRemove) {
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

        linesToRemove = emptyArray()
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

    fun getDiffuculty(): Float {
        return 1.0f/gameSpeed*2
    }

    fun nextStep() {
        val difficulty = getDiffuculty()
        if (Timer.time > difficulty) {
            fall()
            Timer.time -= difficulty
        }

        // increase difficulty after 5 successfully destroyed lines
        if (Score.destroyedLines > 5) {
            Score.destroyedLines -= 5
            gameSpeed += 1
        }
    }
}