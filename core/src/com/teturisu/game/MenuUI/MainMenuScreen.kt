package com.teturisu.game.MenuUI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.FitViewport
import com.teturisu.game.TheGame.MyGestureListener


fun TetrisMainMenu.initMainMenuScreen(){
    val viewport = FitViewport(width/2f, height/2f)
    menuStage = Stage(viewport)

    val skin = Skin(Gdx.files.internal("skin/uiskin.json"))
    val table = Table(skin)


    val btnStyle = TextButton.TextButtonStyle()
    btnStyle.font = cartoonFont
    btnStyle.fontColor = Color(0.7f, 1f, 0.5f, 1f)

    val continueBtn = TextButton(localeBundle.get("continueLabel"), btnStyle)
    continueBtn.addListener(object : ClickListener() {
        override fun clicked(event: InputEvent, x: Float, y: Float) {
            // continue game
            theGame.tetrisGrid.gamePaused = false
            currentState = TetrisMainMenu.GameState.GAME;
            Gdx.input.inputProcessor = GestureDetector(MyGestureListener(theGame))
        }
    })

    val startBtn = TextButton(localeBundle.get("startLabel"), btnStyle)
    startBtn.addListener(object : ClickListener() {
        override fun clicked(event: InputEvent, x: Float, y: Float) {
            setStage(TetrisMainMenu.GameState.GAME)
        }
    })

    val optionsBtn = TextButton(localeBundle.get("optionsLabel"), btnStyle)
    optionsBtn.addListener(object : ClickListener() {
        override fun clicked(event: InputEvent, x: Float, y: Float) {
            setStage(TetrisMainMenu.GameState.OPTIONS)
        }
    })

    val authorsBtn = TextButton(localeBundle.get("authorsLabel"), btnStyle)
    authorsBtn.addListener(object : ClickListener() {
        override fun clicked(event: InputEvent, x: Float, y: Float) {
            setStage(TetrisMainMenu.GameState.AUTHORS)
        }
    })

    val highscoresBtn = TextButton(localeBundle.get("highscoresLabel"), btnStyle)
    highscoresBtn.addListener(object : ClickListener() {
        override fun clicked(event: InputEvent, x: Float, y: Float) {
            setStage(TetrisMainMenu.GameState.HIGHSCORES)
        }
    })

    val logoTexture = Texture(Gdx.files.internal("sprites/logo/transparent_logo.png"))
    val logoImage = Image(logoTexture)
    logoImage.setScaling(Scaling.fit);


    table.add(logoImage); table.row(); table.add(""); table.row()

    if (theGame.tetrisGrid.gamePaused)
        table.add(continueBtn); table.row(); table.add(""); table.row()

    table.add(startBtn); table.row(); table.add(""); table.row()
    table.add(highscoresBtn); table.row(); table.add(""); table.row()
    table.add(optionsBtn); table.row(); table.add(""); table.row()
    table.add(authorsBtn)

    table.setPosition(table.x, table.y+0)
    table.setFillParent(true)
    table.touchable = Touchable.enabled

    menuStage.addActor(table)
}