package com.teturisu.game.TheGame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.JsonValue


class TetrisPainter(var tetrisGrid: TetrisGrid, var screenWidth: Int, var screenHeight: Int) {
    var offsetX = 0f
    var offsetY = 0f
    var rows: Int
    var cols: Int
    var blockSize: Vector2
    var shapeRenderer: ShapeRenderer
    var spriteRenderer: SpriteBatch

    var angleForStopppedCells = 0f

    data class FrameData(val x: Int, val y: Int, val w: Int, val h: Int)


    fun drawBlocks(spritesheetTexture: Texture, spritesheetJson: JsonValue, simpleGraphics: Boolean) {

        if (simpleGraphics) {
            // Draw tetromino with rects
            // draw border
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    if (tetrisGrid.gameGrid[row][col] != ' ') {
                        val x = offsetX + col * blockSize.x
                        val y = offsetY + row * blockSize.y
                        drawBlock(x, y, blockSize.x, blockSize.y, tetrisGrid.gameGrid[row][col], isBorder = true)
                    }
                }
            }
            shapeRenderer.end()

            //  draw filled rects
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val currentCell = tetrisGrid.gameGrid[row][col]
                    if (currentCell != ' ') {
                        val x = offsetX + col * blockSize.x
                        val y = offsetY + row * blockSize.y
                        drawBlock(x, y, blockSize.x, blockSize.y, currentCell)
                    }
                }
            }
            shapeRenderer.end()
        } else {
            // Draw tetrominoes with sprites
            val rotate = false;
            angleForStopppedCells += .25f
            spriteRenderer.begin()
            for (row in 0 until rows) {
                for (col in 0 until cols) {

                    val currentCell = tetrisGrid.gameGrid[row][col]
                    if (currentCell != ' ') {
                        val (srcX, srcY, srcW, srcH) = getCoords(spritesheetJson, getSpriteNum(currentCell))
                        val x = offsetX + col * blockSize.x
                        val y = offsetY + row * blockSize.y

                        val (originX, originY) = Pair(blockSize.x / 2, blockSize.y / 2)
                        val (scaleX, scaleY) = Pair(1f, 1f)
                        var rotation = if (currentCell.isLowerCase())
                                            -tetrisGrid.activeTetromino.angle.toFloat()
                                        else angleForStopppedCells
                        if (!rotate) rotation = 0f

                        spriteRenderer.draw(spritesheetTexture, x, y, originX, originY, blockSize.x, blockSize.y,
                                scaleX, scaleY, rotation, srcX, srcY, srcW, srcH, false, false)
                    }
                }
            }
            spriteRenderer.end()
        }



    }

    fun drawBlock(x: Float, y: Float, w: Float, h: Float, ch: Char, isBorder: Boolean = false) {
        shapeRenderer.color = getColor(ch)

        if (isBorder) {
            shapeRenderer.color = Color.GRAY
            Gdx.gl20.glLineWidth(6f)
            shapeRenderer.rect(x, y, w, h)
        } else {
            shapeRenderer.rect(x, y, w, h)
        }
    }

    fun getCoords(jsonFile: JsonValue, spriteNum: Int): FrameData {
        return jsonFile["frames"][spriteNum]["frame"].let {
            FrameData(it.getInt("x"), it.getInt("y"), it.getInt("w"), it.getInt("h"))
        }
    }

    private fun getSpriteNum(ch: Char): Int {
        return ch.toUpperCase() - 'A'
    }

    private fun getColor(ch: Char): Color {
        return when (ch) {
            // blue
            'A' -> Color.valueOf("#0c7b93")
            'a' -> Color.valueOf("#27496d")

            // mint (greenish?)
            'B' -> Color.valueOf("#00CF91")
            'b' -> Color.valueOf("#004631")

            // orange
            'C' -> Color.valueOf("#6f5a7e")
            'c' -> Color.valueOf("#ffae8f")

            // purple
            'D' -> Color.valueOf("#F375F3")
            'd' -> Color.valueOf("#6A2F6A")

            // skin color???
            'E' -> Color.valueOf("#ff4d4d")
            'e' -> Color.valueOf("#912E00")

            'G' -> Color.valueOf("#df87b0")
            'g' -> Color.valueOf("#ac152d")

            'F' -> Color.valueOf("#8e6d2b")
            'f' -> Color.valueOf("#9b7846")

            else -> {
                Color.valueOf("#FFFFFF")
            }
        }

//        return when (ch) {
//            // floor
//            'F' -> Color.valueOf("#1E3C00")
//
//            // blue
//            'A' -> Color.valueOf("#0c7b93")
//            'a' -> Color.valueOf("#27496d")
//
//            // mint (greenish?)
//            'B' -> Color.valueOf("#00CF91")
//            'b' -> Color.valueOf("#004631")
//
//            // orange
//            'c' -> Color.valueOf("#ffae8f")
//            'C' -> Color.valueOf("#6f5a7e")
//
//            // purple
//            'D' -> Color.valueOf("#F375F3")
//            'd' -> Color.valueOf("#6A2F6A")
//
//            // skin color???
//            'E' -> Color.valueOf("#ff9d9d")
//            'e' -> Color.valueOf("#912E00")
//
//            else -> Color.valueOf("#FFFFFF")
//        }
    }

    fun drawGridLines() {
        shapeRenderer.color = Color.WHITE
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        // draw horizontal lines
        for (row in 0 until rows) {
            shapeRenderer.rectLine(offsetX + 0, offsetY + row * blockSize.y, offsetX + screenWidth, offsetY + row * blockSize.y, 1f)
        }
        // draw vertical lines
        for (col in 0 until cols) {
            shapeRenderer.rectLine(offsetX + col * blockSize.x, offsetY + 0, offsetX + col * blockSize.x, offsetY + screenHeight, 1f)
        }
        shapeRenderer.end()
    }

    fun setOffset(dx: Float, dy: Float) {
        offsetX = dx
        offsetY = dy
    }

    init {
        shapeRenderer = ShapeRenderer()
        spriteRenderer = SpriteBatch()
        val w = screenWidth.toFloat() / tetrisGrid.cols.toFloat()
        val h = screenHeight.toFloat() / tetrisGrid.rows.toFloat()
        blockSize = Vector2(w, h)
        rows = tetrisGrid.rows
        cols = tetrisGrid.cols
    }
}