package com.teturisu.game.TheGame

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.viewport.FitViewport
import com.teturisu.game.MenuUI.initHighscoreScreen
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

    var tetrisLogic = TetrisLogic(rows, cols)
    lateinit var tetrisPainter: TetrisPainter

    var showBackground = false
    var showGridLines = false

    lateinit var controlOverlayStage : Stage


    fun initControlOverlay(){
        val viewport = FitViewport(width, height)
        controlOverlayStage = Stage(viewport)

        val skin = Skin(Gdx.files.internal("skin/uiskin.json"))
        val table = Table(skin)


        val btnStyle = TextButton.TextButtonStyle()
        btnStyle.font = cartoonFont
        btnStyle.fontColor = Color(0.7f, 1f, 0.5f, 1f)
        btnStyle.downFontColor = Color(1f, 1f, 0.5f, 1f)


        val rotateButton = TextButton(localeBundle.get("rotateLabel"), btnStyle)
        rotateButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                tetrisLogic.rotate()
            }
        })

        val fallButton = TextButton(localeBundle.get("fallLabel"), btnStyle)
        fallButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                tetrisLogic.quickFall()
            }
        })

        val leftButton = TextButton(localeBundle.get("leftLabel"), btnStyle)
        leftButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                tetrisLogic.shift(Tetromino.moveDirection.LEFT)
            }
        })

        val rightButton = TextButton(localeBundle.get("rightLabel"), btnStyle)
        rightButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                tetrisLogic.shift(Tetromino.moveDirection.RIGHT)
            }
        })

        val pauseButton = TextButton(localeBundle.get("pauseLabel"), btnStyle)
        pauseButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                tetrisLogic.gamePaused = true
            }
        })
        pauseButton.setPosition(controlOverlayStage.width,controlOverlayStage.height, Align.topRight)



        val vertLayout = Table()
        vertLayout.add(rotateButton).grow()
        vertLayout.row()
        vertLayout.add(fallButton).grow()


        table.add(leftButton).width(Value.percentWidth(0.25f, table)).grow()
        table.add(vertLayout).width(Value.percentWidth(0.50f, table)).grow()
        table.add(rightButton).width(Value.percentWidth(0.25f, table)).grow()


        table.debugAll()
        table.setSize(width, height/4)
        table.touchable = Touchable.enabled

        controlOverlayStage.addActor(table)
        controlOverlayStage.addActor(pauseButton)
    }


    override fun create() {
        width = Gdx.graphics.width.toFloat()
        height = Gdx.graphics.height.toFloat()
    }


    fun initGame() {
        rows = cols * Gdx.graphics.height / Gdx.graphics.width

        tetrisLogic = TetrisLogic(rows, cols)
        tetrisPainter = TetrisPainter(tetrisLogic, width.toInt(), height.toInt())
        Score.score = 0f

        initControlOverlay()
        initInput()
    }

    fun initInput(){
        val inputs = InputMultiplexer()
        inputs.addProcessor(controlOverlayStage)
        inputs.addProcessor(GestureDetector(MyGestureListener(this)))
        Gdx.input.inputProcessor = inputs
    }

    fun render_game(spritesheetTexture: Texture, spritesheetJson: JsonValue, simpleGraphics: Boolean) {
        controlOverlayStage.act()
        controlOverlayStage.draw()

        batch.begin()
            if (showBackground)
                batch.draw(backTexture, 0f, 0f, height, height)

            // draw score label
            cartoonFont.setColor(0.7f, 1f, 0.5f, 1f)
            cartoonFont.draw(batch, "${localeBundle.get("scoreLabel")}: ${DecimalFormat("#,###")
                    .format(Score.score)}", 10f, height-1)

            // draw 'pause' button
//            pauseBtnLayout = cartoonFont.draw(batch, localeBundle.get("pauseLabel"), width-1, height-1, 0f, Align.right, false)


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
            tetrisLogic.rotate()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            tetrisLogic.quickFall()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            showBackground = !showBackground
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            showGridLines = !showGridLines
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            tetrisLogic.shift(Tetromino.moveDirection.LEFT)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            tetrisLogic.shift(Tetromino.moveDirection.RIGHT)
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

        tetrisLogic.nextStep()
    }


    override fun resize(width: Int, height: Int) {
        this.width = width.toFloat()
        this.height = height.toFloat()
    }

    override fun dispose() {
        batch.dispose()
    }
}


