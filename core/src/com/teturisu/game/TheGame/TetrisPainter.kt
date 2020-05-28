package com.teturisu.game.TheGame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.JsonValue
import kotlin.math.PI


class TetrisPainter(var tetrisLogic: TetrisLogic, var screenWidth: Int, var screenHeight: Int) {
    var offsetX = 0f
    var offsetY = 0f
    var rows: Int
    var cols: Int
    var blockSize: Vector2
    var shapeRenderer: ShapeRenderer
    var spriteRenderer: SpriteBatch

    data class FrameData(val x: Int, val y: Int, val w: Int, val h: Int)

    init {
        shapeRenderer = ShapeRenderer()
        spriteRenderer = SpriteBatch()
        val w = screenWidth.toFloat() / tetrisLogic.cols.toFloat()
        val h = screenHeight.toFloat() / tetrisLogic.rows.toFloat()
        blockSize = Vector2(w, h)
        rows = tetrisLogic.rows
        cols = tetrisLogic.cols
    }


    fun drawBlocks(spritesheetTexture: Texture, spritesheetJson: JsonValue, simpleGraphics: Boolean) {

        if (simpleGraphics) {
            // Draw tetromino with rects
            // draw border and filled rects
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val currentCell = tetrisLogic.gameGrid[row][col]
                    if (currentCell != ' ') {
                        val x = offsetX + col * blockSize.x
                        val y = offsetY + row * blockSize.y
                        drawBlock(x, y, blockSize.x, blockSize.y, currentCell, true)
                    }
                }
            }
            shapeRenderer.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val currentCell = tetrisLogic.gameGrid[row][col]
                    if (currentCell != ' ') {
                        val x = offsetX + col * blockSize.x
                        val y = offsetY + row * blockSize.y
                        drawBlock(x, y, blockSize.x, blockSize.y, currentCell, false)
                    }
                }
            }

            shapeRenderer.end()
        } else {
            // Draw tetrominoes with sprites
            spriteRenderer.begin()
            for (row in 0 until rows) {
                for (col in 0 until cols) {

                    val currentCell = tetrisLogic.gameGrid[row][col]
                    if (currentCell != ' ') {
                        val (srcX, srcY, srcW, srcH) = getCoords(spritesheetJson, getSpriteNum(currentCell))
                        val x = offsetX + col * blockSize.x
                        val y = offsetY + row * blockSize.y

                        var rotation = 0f
                        var (w, h) = Pair(blockSize.x, blockSize.y)

                        val col = spriteRenderer.color
                        if (row !in tetrisLogic.linesToRemove){
                            rotation = 0f
                        } else {
                            // interpolate Timer.time between 0(?) and getDifficulty()
                            val interp = Interpolation.fastSlow
                            rotation = interp.apply(0F, 360F, Timer.time.toFloat()/tetrisLogic.getDiffuculty())
                            w = interp.apply(blockSize.x, 0f, Timer.time.toFloat()/tetrisLogic.getDiffuculty())
                            h = interp.apply(blockSize.y, 0f, Timer.time.toFloat()/tetrisLogic.getDiffuculty())

                            val alpha = interp.apply(1f, 0f, Timer.time.toFloat()/tetrisLogic.getDiffuculty())
                            spriteRenderer.setColor(col.r, col.g, col.b, alpha)
                        }

                        spriteRenderer.draw(spritesheetTexture, x+(blockSize.x-w)/2, y+(blockSize.y-h)/2,
                                w/2, h/2, w, h, 1f, 1f, rotation, srcX, srcY, srcW, srcH, false, false)
                        spriteRenderer.setColor(col.r, col.g, col.b, 1f)
                    }
                }
            }
            spriteRenderer.end()
        }

        // dark transparent background while game paused
        if (tetrisLogic.gamePaused){
            Gdx.gl.glEnable(GL20.GL_BLEND)
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)
            shapeRenderer.rect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat())
            shapeRenderer.end()
            Gdx.gl.glDisable(GL20.GL_BLEND)
        }
    }

    fun drawBlock(x: Float, y: Float, w: Float, h: Float, ch: Char, isBorder: Boolean = false) {
        shapeRenderer.color = getColor(ch)
        val pad = 3;
        if (isBorder) {
            shapeRenderer.color = Color.GRAY
            Gdx.gl20.glLineWidth(6f)
            shapeRenderer.rect(x+pad, y+pad, w-2*pad, h-2*pad)
        } else {
            shapeRenderer.rect(x+pad, y+pad, w-2*pad, h-2*pad)
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

        return when (if (ch.isLowerCase()) ch.toUpperCase()
                        else ch.toLowerCase())
        {

            // blue
            'A' -> Color.valueOf("#54d6f1") // staying color
            'a' -> Color.valueOf("#095666") // moving color

            // mint (greenish?)
            'B' -> Color.valueOf("#00CF91")
            'b' -> Color.valueOf("#006244")

            // orange
            'C' -> Color.valueOf("#b0a0bc")
            'c' -> Color.valueOf("#554561")

            // purple
            'D' -> Color.valueOf("#dde809")
            'd' -> Color.valueOf("#626703")

            // skin color???
            'E' -> Color.valueOf("#ff7979")
            'e' -> Color.valueOf("#880000")

            'G' -> Color.valueOf("#df87b0")
            'g' -> Color.valueOf("#ac152d")

            'F' -> Color.valueOf("#cba556")
            'f' -> Color.valueOf("#7e6227")

            else -> {
                Color.valueOf("#FFFFFF")
            }
        }
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

}