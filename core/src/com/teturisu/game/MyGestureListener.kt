package com.teturisu.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.input.GestureDetector.GestureListener
import com.badlogic.gdx.math.Vector2

class MyGestureListener(grid: TetrisGrid) : GestureListener {
    val tetrisGrid = grid

    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        if (count == 1)
            tetrisGrid.rotate()

        return false
    }

    override fun longPress(x: Float, y: Float): Boolean {
        tetrisGrid.quickFall()

        return false
    }

    override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
//        println("${velocityX}, ${velocityY}, ${button}")
//
//        if (velocityX < -1000)
//            tetrisGrid.shift(Tetromino.moveDirection.LEFT)
//        else if (1000 < velocityX)
//            tetrisGrid.shift(Tetromino.moveDirection.RIGHT)
//
        if (velocityY < -1000)
            tetrisGrid.quickFall()

        return false
    }

    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
        val col = Utilities.map(x, 0f, Gdx.graphics.width.toFloat(), 0f, tetrisGrid.cols.toFloat()).toInt()


//        println("${x}, ${y}, ${deltaX}, ${deltaX}")
        println(deltaX)
//        tetrisGrid.moveTetrominoTo(col - 1)
//
        if (Timer.time > 0.2) {
            Timer.time = 0.0
            if (deltaX < -1) {
                tetrisGrid.shift(Tetromino.moveDirection.LEFT)
            } else if (deltaX > 1) {
                tetrisGrid.shift(Tetromino.moveDirection.RIGHT)
            }
        }

        return false
    }

    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {

        tetrisGrid.quickFall()
        return false
    }

    override fun zoom(initialDistance: Float, distance: Float): Boolean {
        return false
    }

    override fun pinch(initialPointer1: Vector2, initialPointer2: Vector2, pointer1: Vector2, pointer2: Vector2): Boolean {
        return false
    }

    override fun pinchStop() {}
}