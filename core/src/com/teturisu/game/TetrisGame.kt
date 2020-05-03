package com.teturisu.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.*
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.FitViewport
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow

class Block {
    var pos: Vector2? = null
    var size: Vector2? = null
    var sprite: Sprite

    enum class dir {
        LEFT, UP, RIGHT, DOWN
    }

    constructor(texture: Texture?, startX: Float, startY: Float, sizeX: Float, sizeY: Float) {
        pos = Vector2(startX, startY)
        size = Vector2(sizeX, sizeY)
        sprite = Sprite(texture)
        sprite.setSize(size!!.x, size!!.y)
    }

    constructor(texture: Texture?) {
        sprite = Sprite(texture)
    }

    fun draw(batch: SpriteBatch?, _pos: Vector2?, _size: Vector2?) {
        pos = _pos
        size = _size
        sprite.setPosition(pos!!.x, pos!!.y)
        sprite.setSize(size!!.x, size!!.y)
        sprite.draw(batch)
    }

    fun move(dx: Float, dy: Float) {
        pos!!.add(dx, dy)
        sprite.setPosition(pos!!.x, pos!!.y)
    }

    fun move(d: dir?) {
        when (d) {
            dir.LEFT -> move(-size!!.x, 0f)
            dir.UP -> move(0f, size!!.y)
            dir.RIGHT -> move(size!!.x, 0f)
            dir.DOWN -> move(0f, -size!!.y)
        }
    }
}


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




class TetrisGame : ApplicationAdapter() {
    lateinit var batch: SpriteBatch
    lateinit var fruitsSpritesheetTexture: Texture
    lateinit var fruitsSpritesheetJson: JsonValue

    lateinit var backgroundTexture: Texture

    var width = 0f
    var height = 0f

    lateinit var tetrisGrid: TetrisGrid
    lateinit var tetrisPainter: TetrisPainter

    lateinit var scoreFont: BitmapFont
    var showBackground = false
    var showGridLines = false

    var cols: Int = 8
    var rows: Int = 12
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

    var currentState = GameState.MAIN_MENU // start state

    override fun create() {

        localeBundle = I18NBundle.createBundle(Gdx.files.internal("i18n/labels"))
        Gdx.app.log("TAG",localeBundle.get("backLabel"))

        // init structure for load/save system
        saveData = Gdx.app.getPreferences("mysavedata")
        if (saveData.contains("gameSpeed")) gameSpeed = saveData.getInteger("gameSpeed")
        if (saveData.contains("texturePack")) texturePack = saveData.getString("texturePack")
//        saveData.putString("highscores", "p1:12312;p2:123;p3:512;p4:88812;p5:99")
//        saveData.flush()

        // load hashmap from savedata
        if (saveData.contains("highscores")) {
            for (entry in saveData.getString("highscores", "").split(';')) {
                val (name, scoreStr) = entry.split(':')
                highscoresData.set(name, scoreStr.toInt())
            }
        }


        // init font
        val fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("fonts/NewFont.otf"))
        val fontParameter = FreeTypeFontGenerator.FreeTypeFontParameter()
        fontParameter.size = 30
        // add cyrillic chars
        fontParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
        scoreFont = fontGenerator.generateFont(fontParameter)
        fontGenerator.dispose()

        width = Gdx.graphics.width.toFloat()
        height = Gdx.graphics.height.toFloat()



        backgroundTexture = Texture("sprites/bg.png")

        batch = SpriteBatch()

        fruitsSpritesheetTexture = Texture("sprites/characters/spritesheet.png")
        fruitsSpritesheetJson = JsonReader().parse(Gdx.files.internal("sprites/characters/spritesheet.json"))


        setStage(GameState.MAIN_MENU)
    }


    fun initHighscoreScreen() {
        val viewport = FitViewport(width/5f, height/5f)
        highscoresStage = Stage(viewport)


        val skin = Skin(Gdx.files.internal("skin/uiskin.json"))
        val table = Table(skin)


        val btnStyle = TextButton.TextButtonStyle()
        btnStyle.font = scoreFont
        btnStyle.fontColor = Color(0.7f, 1f, 0.5f, 1f)


        val backBtn = TextButton(localeBundle.get("backLabel"), btnStyle)
        backBtn.color = Color(0.7f, 1f, 0.5f, 0.5f)
        backBtn.touchable = Touchable.enabled
        backBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                setStage(GameState.MAIN_MENU)
            }
        })
        backBtn.label.setFontScale(0.5f)
        backBtn.setPosition(0f  - backBtn.width/4,highscoresStage.height, Align.topLeft)

        val highscoresLabel = TextButton( localeBundle.get("highscoresLabel"), btnStyle)
        highscoresLabel.setPosition(highscoresStage.width/2.0f,highscoresStage.height*0.8f, Align.center)


//        val highscores : List<String> = if (saveData.contains("highscores"))
//                saveData.getString("highscores").split(';')
//            else
//                ArrayList<String>(0)

        val highscoreList = highscoresData.toList().sortedByDescending { it.second }

        table.add(highscoresLabel).colspan(3); table.row(); table.add(""); table.row()
        // show highscores: name and score
        table.add("#"); table.add("Name"); table.add("Score"); table.row()
        for (i in 0 until 5) {
            val (name, score) = if (highscoreList.size > i) highscoreList[i]  else Pair("---", 0)

            table.add((i+1).toString() + ") "); table.add(name)
            table.add(score.toString()).expandX(); table.row()
        }

        table.setFillParent(true)
//        table.debugAll()
////        table.touchable = Touchable.enabled

//        highscoresStage.addActor(highscoresLabel)
        highscoresStage.addActor(backBtn)
        highscoresStage.addActor(table)
    }

    fun initGameoverScreen(){
        val viewport = FitViewport(width/4f, height/4f)
        gameoverStage = Stage(viewport)


        val skin = Skin(Gdx.files.internal("skin/uiskin.json"))
        val table = Table(skin)


        val btnStyle = TextButton.TextButtonStyle()
        btnStyle.font = scoreFont
        btnStyle.fontColor = Color(0.7f, 1f, 0.5f, 1f)



        val nameField = TextField("", skin)

        // button to save highscore
        val okButton = TextButton("Save", btnStyle)
        okButton.color = Color(1f, 0.8f, 0.5f, 1f)
        okButton.touchable = Touchable.enabled
        okButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                addHighscore(nameField.text, Score.score.toInt())
                setStage(GameState.HIGHSCORES)
            }
        })


        val developersLabel = TextButton("Game Over", btnStyle)
        table.add(developersLabel).colspan(2); table.row()
        table.add(""); table.row()
        table.add("Score: "); table.add(Score.score.toInt().toString()); table.row().expandX()
        table.add("Difficulty: "); table.add("$gameSpeed"); table.row().expandX()
        table.add("Enter name: ");
        table.add(nameField); table.row().expandX()
        table.add(okButton).colspan(2)

        table.setFillParent(true)
//        table.debugAll()
        table.touchable = Touchable.enabled

        gameoverStage.addActor(table)
    }

    fun initOptionsScreen(){
        val viewport = FitViewport(width/5f, height/5f)
        optionsStage = Stage(viewport)

        val skin = Skin(Gdx.files.internal("skin/uiskin.json"))
        val table = Table(skin)

        val btnStyle = TextButton.TextButtonStyle()
        btnStyle.font = scoreFont
        btnStyle.fontColor = Color(0.7f, 1f, 0.5f, 1f)


        val backBtn = TextButton(localeBundle.get("backLabel"), btnStyle)
        backBtn.color = Color(0.7f, 1f, 0.5f, 0.5f)
        backBtn.touchable = Touchable.enabled
        backBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                setStage(GameState.MAIN_MENU)
            }
        })
        backBtn.label.setFontScale(0.5f)
        backBtn.setPosition(0f  - backBtn.width/4,optionsStage.height, Align.topLeft)


        val optionsLabel = TextButton(localeBundle.get("optionsLabel"), btnStyle)
        optionsLabel.setPosition(optionsStage.width/2.0f,optionsStage.height*0.8f, Align.center)

        table.row(); table.add(""); table.row(); table.add(""); table.row()

        val speedLabel = Label("Difficulty: $gameSpeed", skin)

        val speedSlider = Slider(1f, 20f, 1f,false, skin)
        speedSlider.value = gameSpeed.toFloat()
        speedSlider.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                speedLabel.setText("Difficulty: " + speedSlider.value.toInt().toString() + "")
                gameSpeed = speedSlider.value.toInt()

                saveData.putInteger("gameSpeed", gameSpeed)
                saveData.flush()
            }
        })

        table.add(speedLabel)
        table.add(speedSlider)




        table.row()
        val textureSelectBox = SelectBox<String>(skin)
        textureSelectBox.items = Array<String>(arrayOf("Filled rects", "Fruits"))

        if (texturePack.length > 0)
            textureSelectBox.selected = texturePack

        textureSelectBox.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                Gdx.app.log("TAG", textureSelectBox.selected)
                texturePack = textureSelectBox.selected

                saveData.putString("texturePack", texturePack)
                saveData.flush()
            }
        })

        table.add("Textures: ")
        table.add(textureSelectBox)
        table.row()


        val languageSelectBox = SelectBox<String>(skin)
        languageSelectBox.items = Array<String>(arrayOf("Russian", "English"))
        if (localeBundle.locale.language == "ru") {
            languageSelectBox.selected = "Russian"
        } else if (localeBundle.locale.language == "en") {
            languageSelectBox.selected = "English"
        }

        languageSelectBox.addListener(object : ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (languageSelectBox.selected == "English") {
                    localeBundle = I18NBundle.createBundle(Gdx.files.internal("i18n/labels"), Locale("en"))
                } else if (languageSelectBox.selected == "Russian") {
                    localeBundle = I18NBundle.createBundle(Gdx.files.internal("i18n/labels"), Locale("ru"))
                }
            }
        })

        table.add("Language: ")
        table.add(languageSelectBox)

        table.setFillParent(true)
//        table.debugAll()
//        table.touchable = Touchable.enabled

        optionsStage.addActor(backBtn)
        optionsStage.addActor(optionsLabel)
        optionsStage.addActor(table)

    }

    fun initAuthorsScreen(){
        val viewport = FitViewport(width/5f, height/5f)
        authorsStage = Stage(viewport)

        val skin = Skin(Gdx.files.internal("skin/uiskin.json"))
        val table = Table(skin)


        table.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                setStage(GameState.MAIN_MENU)
            }
        })


        val btnStyle = TextButton.TextButtonStyle()
        btnStyle.font = scoreFont
        btnStyle.fontColor = Color(0.7f, 1f, 0.5f, 1f)


        val developersLabel = TextButton(localeBundle.get("developersLabel"), btnStyle)

        table.add(developersLabel); table.row().fillX();
        table.add(""); table.row().fillX()
        table.add("V. Kulinenko"); table.row().fillX()
        table.add("V. Fadeev"); table.row().fillX()
        table.add("V. Yenin")

        table.setFillParent(true)
//        table.debugAll()
        table.touchable = Touchable.enabled

        authorsStage.addActor(table)
    }

    fun initMainMenuScreen(){
        val viewport = FitViewport(width/5f, height/5f)
        menuStage = Stage(viewport)

        val skin = Skin(Gdx.files.internal("skin/uiskin.json"))
        val table = Table(skin)


        val btnStyle = TextButton.TextButtonStyle()
        btnStyle.font = scoreFont
        btnStyle.fontColor = Color(0.7f, 1f, 0.5f, 1f)


        val startBtn = TextButton(localeBundle.get("startLabel"), btnStyle)
        startBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                setStage(GameState.GAME)
            }
        })


        val optionsBtn = TextButton(localeBundle.get("optionsLabel"), btnStyle)
        optionsBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                setStage(GameState.OPTIONS)
            }
        })

        val authorsBtn = TextButton(localeBundle.get("authorsLabel"), btnStyle)
        authorsBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                setStage(GameState.AUTHORS)
            }
        })

        val highscoresBtn = TextButton(localeBundle.get("highscoresLabel"), btnStyle)
        highscoresBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                setStage(GameState.HIGHSCORES)
            }
        })

        val logoTexture = Texture(Gdx.files.internal("sprites/logo/final.png"))
        val logoImage = Image(logoTexture)
        logoImage.scaleY = 0.75f


        table.add(logoImage); table.row(); table.add(""); table.row()

        table.add(startBtn); table.row(); table.add(""); table.row()
        table.add(highscoresBtn); table.row(); table.add(""); table.row()
        table.add(optionsBtn); table.row(); table.add(""); table.row()
        table.add(authorsBtn)

        table.setPosition(table.x, table.y+0)
        table.setFillParent(true)
//        table.debugAll()
        table.touchable = Touchable.enabled

        menuStage.addActor(table)
    }

    private fun setStage(stage: TetrisGame.GameState) {
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
                initGame()
                Gdx.input.inputProcessor = GestureDetector(MyGestureListener(tetrisGrid))
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


    fun initGame() {
        rows = cols * Gdx.graphics.height / Gdx.graphics.width

        tetrisGrid = TetrisGrid(rows, cols)
        tetrisPainter = TetrisPainter(tetrisGrid, width.toInt(), height.toInt(), fruitsSpritesheetTexture, fruitsSpritesheetJson)
        Score.score = 0f
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
                render_game()
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

    fun render_game() {
        gameUpdate()

        batch.begin()
            if (showBackground)
                batch.draw(backgroundTexture, 0f, 0f, height, height)

            // draw score
            scoreFont.setColor(0.7f, 1f, 0.5f, 1f)
            scoreFont.draw(batch, "${localeBundle.get("scoreLabel")}: ${DecimalFormat("#,###").format(Score.score)}", 10f, height -1)
        batch.end()

        if (showGridLines)
            tetrisPainter.drawGridLines()

        tetrisPainter.drawBlocks()
    }

    private fun gameUpdate() {

        tickGame()

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
//        if (Timer.time > Timer.timeInterval) {
//            tetrisGrid.fall()
//            Timer.time -= Timer.timeInterval
//
//        }
//
//        if (Score.destroyedLines > 5 && Timer.timeInterval > 0.15) {
//            Score.destroyedLines -= 5
//            Timer.timeInterval -= 0.3
//        }

        val gameDifficulty = 1.0f/gameSpeed*2
        if (Timer.time > gameDifficulty) {
            tetrisGrid.fall()
            Timer.time -= gameDifficulty

            if (tetrisGrid.gameOver) {
                setStage(GameState.GAME_OVER)
            }
        }

        if (Score.destroyedLines > 5) {
            Score.destroyedLines -= 5
            gameSpeed += 1
        }
    }

    fun addHighscore(name: String, score: Int){
        if (name.length == 0) return

        val currMin = highscoresData.toList().minBy { it.second }!!.second
        if (score > currMin) {
            if (!highscoresData.contains(name) || highscoresData.get(name)!! < score) {
                highscoresData.put(name, score)

                var entryString = ""
                // save score
                for (entry in highscoresData.toList().sortedByDescending { it.second }){
                    if (entryString.length != 0)
                        entryString += ";"
                    entryString += entry.first + ":" + entry.second.toString()
                }
                saveData.putString("highscores", entryString)
                saveData.flush()
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        this.width = width.toFloat()
        this.height = height.toFloat()
    }

    override fun dispose() {
        batch.dispose()
        fruitsSpritesheetTexture.dispose()
        scoreFont.dispose()

    }
}


