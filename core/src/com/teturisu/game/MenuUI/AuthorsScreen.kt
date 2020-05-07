package com.teturisu.game.MenuUI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport


fun TetrisMainMenu.initAuthorsScreen(){
    val viewport = ScalingViewport(Scaling.fill, width/2, height/2)

    authorsStage = Stage(viewport)

    val skin = Skin(Gdx.files.internal("skin/uiskin.json"))
    val table = Table(skin)


    table.addListener(object : ClickListener() {
        override fun clicked(event: InputEvent, x: Float, y: Float) {
            setStage(TetrisMainMenu.GameState.MAIN_MENU)
        }
    })


    val btnStyle = TextButton.TextButtonStyle()
    btnStyle.font = cartoonFont
    btnStyle.fontColor = Color(0.7f, 1f, 0.5f, 1f)


    val developersLabel = TextButton(localeBundle.get("developersLabel"), btnStyle)

    table.add(developersLabel); table.row().fillX();
    table.add(""); table.row().fillX()
    table.add(Label("V. Kulineнко", Label.LabelStyle(ordinaryFont, Color.WHITE)))
    table.row().fillX()
    table.add(Label("V. Fadeev", Label.LabelStyle(ordinaryFont, Color.WHITE)))
    table.row().fillX()
    table.add(Label("V. Yenin", Label.LabelStyle(ordinaryFont, Color.WHITE)))

    table.setFillParent(true)
    table.touchable = Touchable.enabled

    authorsStage.addActor(table)
}