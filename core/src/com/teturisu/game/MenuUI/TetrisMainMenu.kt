package com.teturisu.game.MenuUI

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue
import com.teturisu.game.MyGestureListener
import com.teturisu.game.TheGame.TetrisGame


fun initFont(file_path: String): BitmapFont {
    // init font
    val fontGenerator = FreeTypeFontGenerator(Gdx.files.internal(file_path))
    val fontParameter = FreeTypeFontGenerator.FreeTypeFontParameter()
    fontParameter.size = 50
    // add cyrillic chars
    fontParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS +
            "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    val bitmapFont = fontGenerator.generateFont(fontParameter)
    fontGenerator.dispose()

    return bitmapFont;
}

class TetrisMainMenu : ApplicationAdapter() {
    lateinit var backTexture: Texture
    lateinit var spritesheetTexture: Texture
    lateinit var spritesheetJson: JsonValue
    var simpleGraphics = false;

    var width = 0f
    var height = 0f

    lateinit var cartoonFont: BitmapFont
    lateinit var ordinaryFont: BitmapFont

    var gameSpeed: Int = 1
    var texturePack: String = ""
    lateinit var saveData: Preferences

    val highscoresData = HashMap<String, Int>()


    lateinit var menuStage: Stage
    lateinit var optionsStage: Stage
    lateinit var authorsStage: Stage
    lateinit var highscoresStage: Stage
    lateinit var gameoverStage: Stage

    lateinit var localeBundle: I18NBundle

    enum class GameState {
        GAME, MAIN_MENU, OPTIONS, AUTHORS, HIGHSCORES, GAME_OVER
    }

    lateinit var theGame: TetrisGame

    var currentState = GameState.MAIN_MENU

    override fun create() {
        // for localization
        localeBundle = I18NBundle.createBundle(Gdx.files.internal("i18n/labels"))

        // init structure for load/save system
        saveData = Gdx.app.getPreferences("mysavedata")
        if (saveData.contains("gameSpeed")) gameSpeed = saveData.getInteger("gameSpeed")
        if (saveData.contains("texturePack")) {
            texturePack = saveData.getString("texturePack")

            Gdx.app.log("TAG",texturePack)
            changeSpritesheet(texturePack)
        }

        // load highscore from save data if any exists
        if (saveData.contains("highscores")) {
            for (entry in saveData.getString("highscores", "").split(';')) {
                val (name, scoreStr) = entry.split(':')
                highscoresData.set(name, scoreStr.toInt())
            }
        }

        cartoonFont = initFont("fonts/NewFont.otf")
        ordinaryFont = initFont("fonts/ubuntu.ttf")

        width = Gdx.graphics.width.toFloat()
        height = Gdx.graphics.height.toFloat()

        backTexture = Texture("sprites/bg.png")

        // default texture pack
        spritesheetTexture = Texture("sprites/textures/original/spritesheet.png")
        spritesheetJson = JsonReader().parse(Gdx.files.internal("sprites/textures/original/spritesheet.json"))

        theGame = TetrisGame(backTexture, localeBundle, cartoonFont)

        setStage(GameState.MAIN_MENU)
    }

    fun setStage(stage: GameState) {
        currentState = stage
        when (stage) {
            GameState.OPTIONS -> {
                initOptionsScreen()
                Gdx.input.inputProcessor = optionsStage
            }
            GameState.HIGHSCORES -> {
                initHighscoreScreen()
                Gdx.input.inputProcessor = highscoresStage
            }
            GameState.AUTHORS -> {
                initAuthorsScreen()
                Gdx.input.inputProcessor = authorsStage
            }
            GameState.GAME -> {
                theGame.initGame()
                Gdx.input.inputProcessor = GestureDetector(MyGestureListener(theGame.tetrisGrid))
            }
            GameState.MAIN_MENU -> {
                initMainMenuScreen()
                Gdx.input.inputProcessor = menuStage
            }
            GameState.GAME_OVER -> {
                initGameoverScreen()
                Gdx.input.inputProcessor = gameoverStage
            }
        }
    }


    override fun render() {
        val bColor = .15f
        Gdx.gl.glClearColor(bColor, bColor, bColor, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // game loop
        when(currentState){
            GameState.MAIN_MENU -> {
                menuStage.act()
                menuStage.draw()
            }
            GameState.AUTHORS -> {
                authorsStage.act()
                authorsStage.draw()
            }
            GameState.GAME -> {
                update_the_game()
            }
            GameState.OPTIONS -> {
                optionsStage.act()
                optionsStage.draw()
            }
            GameState.HIGHSCORES -> {
                highscoresStage.act()
                highscoresStage.draw()
            }
            GameState.GAME_OVER -> {
                gameoverStage.act()
                gameoverStage.draw()
            }
        }
    }

    private fun update_the_game() {
        theGame.render_game(spritesheetTexture, spritesheetJson, simpleGraphics)

        if (theGame.tetrisGrid.gameOver) {
            setStage(GameState.GAME_OVER)
        }
    }

    override fun resize(width: Int, height: Int) {
        this.width = width.toFloat()
        this.height = height.toFloat()
    }

    override fun dispose() {
        cartoonFont.dispose()
        ordinaryFont.dispose()
    }
}
