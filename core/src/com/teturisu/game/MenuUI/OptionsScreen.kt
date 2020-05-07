package com.teturisu.game.MenuUI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.viewport.FitViewport
import java.util.*



fun TetrisMainMenu.changeSpritesheet(newSpritesheet: String){
//    if (spritesheetTexture.isManaged)
//        spritesheetTexture.dispose()

    when (newSpritesheet.toUpperCase(localeBundle.locale)) {
        "SIMPLE" -> {
            simpleGraphics = true;
        }
        "ORIGINAL" -> {
            spritesheetTexture = Texture("sprites/textures/original/spritesheet.png")
            spritesheetJson = JsonReader().parse(Gdx.files.internal("sprites/textures/original/spritesheet.json"))
            simpleGraphics = false;
        }
        "FRUITS" -> {
            spritesheetTexture = Texture("sprites/textures/fruits/spritesheet.png")
            spritesheetJson = JsonReader().parse(Gdx.files.internal("sprites/textures/fruits/spritesheet.json"))
            simpleGraphics = false;
        }
    }
}

fun TetrisMainMenu.initOptionsScreen(){
    val viewport = FitViewport(width/2f, height/2f)
    optionsStage = Stage(viewport)

    val skin = Skin(Gdx.files.internal("skin/uiskin.json"))

    val btnStyle = TextButton.TextButtonStyle()
    btnStyle.font = cartoonFont
    btnStyle.fontColor = Color(0.7f, 1f, 0.5f, 1f)


    val backBtn = TextButton(localeBundle.get("backLabel"), btnStyle)
    backBtn.color = Color(0.7f, 1f, 0.5f, 0.5f)
    backBtn.touchable = Touchable.enabled
    backBtn.addListener(object : ClickListener() {
        override fun clicked(event: InputEvent, x: Float, y: Float) {
            setStage(TetrisMainMenu.GameState.MAIN_MENU)
        }
    })
    backBtn.label.setFontScale(0.5f)
    backBtn.setPosition(0f  - backBtn.width/4,optionsStage.height, Align.topLeft)


    val optionsLabel = TextButton(localeBundle.get("optionsLabel"), btnStyle)
    optionsLabel.setPosition(optionsStage.width/2.0f,optionsStage.height*0.8f, Align.center)



    val difficultyLabel = Label(localeBundle.get("difficultyLabel") + ": ", Label.LabelStyle(ordinaryFont, Color.WHITE))
    val difficultyTooltip = Label(gameSpeed.toString(), Label.LabelStyle(ordinaryFont, Color.WHITE));
    val difficultySlider = Slider(1f, 20f, 1f,false, skin)
    difficultySlider.value = gameSpeed.toFloat()
    difficultySlider.addListener(object : ChangeListener() {
        override fun changed(event: ChangeEvent?, actor: Actor?) {
            difficultyTooltip.setText(difficultySlider.value.toInt().toString())
            gameSpeed = difficultySlider.value.toInt()

            saveData.putInteger("gameSpeed", gameSpeed)
            saveData.flush()
        }
    })
    difficultySlider.style.knob.minHeight = 60f
    difficultySlider.style.knob.minWidth = 20f
    difficultySlider.style.background.minHeight = 30f


    val textureSelectBox = SelectBox<String>(skin)
    textureSelectBox.items = Array<String>(arrayOf("Simple", "Original", "Fruits"))

    if (texturePack.length > 0)
        textureSelectBox.selected = texturePack

    textureSelectBox.addListener(object : ChangeListener(){
        override fun changed(event: ChangeEvent?, actor: Actor?) {
            texturePack = textureSelectBox.selected
            changeSpritesheet(texturePack)

            saveData.putString("texturePack", texturePack)
            saveData.flush()
        }
    })



    val languageSelectBox = SelectBox<String>(skin)
    languageSelectBox.items = Array<String>(arrayOf("Russian", "English"))
    if (localeBundle.locale.language == "ru") {
        languageSelectBox.selected = "Russian"
    } else if (localeBundle.locale.language == "en") {
        languageSelectBox.selected = "English"
    }

    languageSelectBox.style.font = ordinaryFont
    languageSelectBox.style.fontColor = Color.GRAY

    languageSelectBox.addListener(object : ChangeListener(){
        override fun changed(event: ChangeEvent?, actor: Actor?) {
            if (languageSelectBox.selected == "English") {
                localeBundle = I18NBundle.createBundle(Gdx.files.internal("i18n/labels"), Locale("en"))
            } else if (languageSelectBox.selected == "Russian") {
                localeBundle = I18NBundle.createBundle(Gdx.files.internal("i18n/labels"), Locale("ru"))
            }
            setStage(TetrisMainMenu.GameState.OPTIONS)
        }
    })


    val difficultyGroup = HorizontalGroup()
    difficultyGroup.addActor(difficultySlider)
    difficultyGroup.addActor(difficultyTooltip)


    val table = Table(skin)

    table.add(difficultyLabel).left()
    table.add(difficultyGroup).fillX()
    table.row()

    table.add(Label( localeBundle.get("themeLabel") + ": ", Label.LabelStyle(ordinaryFont, Color.WHITE))).left()
    table.add(textureSelectBox).fillX()
    table.row()

    table.add(Label(localeBundle.get("languageLabel") + ": ", Label.LabelStyle(ordinaryFont, Color.WHITE))).left()
    table.add(languageSelectBox).fillX()
    table.setFillParent(true)
    table.debugAll()

    optionsStage.addActor(backBtn)
    optionsStage.addActor(optionsLabel)
    optionsStage.addActor(table)

}