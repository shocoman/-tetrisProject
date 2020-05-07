package com.teturisu.game.TheGame

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.JsonValue
import java.text.DecimalFormat
import kotlin.math.pow



object Score {
    var score = 0f
    var scoreForFullLine = 100f
    var lineMultiplier = 2
    var destroyedLines: Int = 0

    fun increaseScore(clearedLines: Int) {
        score += scoreForFullLine * clearedLines.toFloat().pow(lineMultiplier)
        destroyedLines += clearedLines
    }
}

object Timer {
    var time: Double = 0.0
    var timeInterval = 1.0
}


class TetrisGame(val backTexture: Texture, val localeBundle: I18NBundle, var cartoonFont: BitmapFont) : ApplicationAdapter() {
    val batch = SpriteBatch()

    var width = Gdx.graphics.width.toFloat()
    var height = Gdx.graphics.height.toFloat()
    var pauseBtnLayout = GlyphLayout()

    var cols: Int = 8
    var rows: Int = 12
    var gameSpeed: Int = 2

    var tetrisGrid = TetrisGrid(rows, cols)
    lateinit var tetrisPainter: TetrisPainter

    var showBackground = false
    var showGridLines = false

    override fun create() {
        width = Gdx.graphics.width.toFloat()
        height = Gdx.graphics.height.toFloat()
    }


    fun initGame() {
        rows = cols * Gdx.graphics.height / Gdx.graphics.width

        tetrisGrid = TetrisGrid(rows, cols)
        tetrisPainter = TetrisPainter(tetrisGrid, width.toInt(), height.toInt())
        Score.score = 0f
    }

    fun render_game(spritesheetTexture: Texture, spritesheetJson: JsonValue, simpleGraphics: Boolean) {
        batch.begin()
            if (showBackground)
                batch.draw(backTexture, 0f, 0f, height, height)

            // draw score label
            cartoonFont.setColor(0.7f, 1f, 0.5f, 1f)
            cartoonFont.draw(batch, "${localeBundle.get("scoreLabel")}: ${DecimalFormat("#,###")
                    .format(Score.score)}", 10f, height-1)

            // draw 'pause' button
            pauseBtnLayout = cartoonFont.draw(batch, localeBundle.get("pauseLabel"), width-1, height-1, 0f, Align.right, false)


        batch.end()

        if (showGridLines)
            tetrisPainter.drawGridLines()

        tetrisPainter.drawBlocks(spritesheetTexture, spritesheetJson, simpleGraphics)
    }

    fun gameUpdate() {
        tickGame()
        updateControls()
    }

    private fun updateControls() {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            tetrisGrid.rotate()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            tetrisGrid.quickFall()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            showBackground = !showBackground
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            showGridLines = !showGridLines
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            tetrisGrid.shift(Tetromino.moveDirection.LEFT)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            tetrisGrid.shift(Tetromino.moveDirection.RIGHT)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            initGame() // reset
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            cols++
            initGame()
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            rows++
            initGame()
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            if (cols > 1)
                cols--
            initGame()
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            if (rows > 1)
                rows--
            initGame()
        }
    }

    private fun tickGame() {
        Timer.time += Gdx.graphics.deltaTime

        val gameDifficulty = 1.0f/gameSpeed*2
        if (Timer.time > gameDifficulty) {
            tetrisGrid.fall()
            Timer.time -= gameDifficulty
        }

        // increase difficulty after 5 successfully destroyed lines
        if (Score.destroyedLines > 5) {
            Score.destroyedLines -= 5
            gameSpeed += 1
        }
    }


    override fun resize(width: Int, height: Int) {
        this.width = width.toFloat()
        this.height = height.toFloat()
    }

    override fun dispose() {
        batch.dispose()
    }
}


