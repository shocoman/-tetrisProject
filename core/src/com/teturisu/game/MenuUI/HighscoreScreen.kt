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
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport


fun TetrisMainMenu.addHighscore(name: String, score: Int){
    if (name.isEmpty()) return

    val currMin = highscoresData.minBy { it.value }
    if (highscoresData.size < 5 || score > currMin!!.value) {

        if (highscoresData.getOrPut(name,{-1}) < score) {
            highscoresData[name] = score

            var entryString = ""
            // save score
            for (entry in highscoresData.toList().sortedByDescending { it.second }){
                if (entryString.isNotEmpty())
                    entryString += ";"
                entryString += entry.first + ":" + entry.second.toString()
            }

            saveData.putString("highscores", entryString)
            saveData.flush()

        }
    }

}

fun TetrisMainMenu.initHighscoreScreen() {
    val viewport = FitViewport(width/2f, height/2f)
    highscoresStage = Stage(viewport)

    val skin = Skin(Gdx.files.internal("skin/uiskin.json"))
    val table = Table(skin)

    val btnStyle = TextButton.TextButtonStyle()
    btnStyle.font = cartoonFont
    btnStyle.fontColor = Color(0.7f, 1f, 0.5f, 1f)
    btnStyle.downFontColor = Color(1f, 1f, 0.5f, 1f)

    val backBtn = TextButton(localeBundle.get("backLabel"), btnStyle)
    backBtn.color = Color(0.7f, 1f, 0.5f, 0.5f)
    backBtn.addListener(object : ClickListener() {
        override fun clicked(event: InputEvent, x: Float, y: Float) {
            setStage(TetrisMainMenu.GameState.MAIN_MENU)
        }
    })
    backBtn.label.setFontScale(0.5f)
    backBtn.setPosition(0f  - backBtn.width/4,highscoresStage.height, Align.topLeft)

    val clearBtn = TextButton(localeBundle.get("clearLabel"), btnStyle)
    clearBtn.color = Color(0.7f, 1f, 0.5f, 0.5f)
    clearBtn.addListener(object : ClickListener() {
        override fun clicked(event: InputEvent, x: Float, y: Float) {
            highscoresData.clear()
            initHighscoreScreen()

            saveData.remove("highscores")
            saveData.flush()
        }
    })
    clearBtn.label.setFontScale(0.5f)
    clearBtn.setPosition(highscoresStage.width + backBtn.width/4,highscoresStage.height, Align.topRight)


    val highscoresLabel = TextButton( localeBundle.get("highscoresLabel"), btnStyle)
    highscoresLabel.setPosition(highscoresStage.width/2.0f,highscoresStage.height*0.8f, Align.center)


    val highscoreList = highscoresData.toList().sortedByDescending { it.second }

    table.add(highscoresLabel).colspan(3); table.row(); table.add(""); table.row()
    // show highscores: name and score
    table.add(Label("#", Label.LabelStyle(ordinaryFont, Color.WHITE)))
    table.add(Label(localeBundle.get("nameLabel"), Label.LabelStyle(ordinaryFont, Color.WHITE)))
    table.add(Label(localeBundle.get("scoreLabel"), Label.LabelStyle(ordinaryFont, Color.WHITE)))
    table.row()
    for (i in 0 until 5) {
        val (name, score) = if (highscoreList.size > i) highscoreList[i]  else Pair("---", 0)

        table.add(Label((i+1).toString() + ") ", Label.LabelStyle(ordinaryFont, Color.WHITE)))
        table.add(Label(name, Label.LabelStyle(ordinaryFont, Color.WHITE)))
        table.add(Label(score.toString(), Label.LabelStyle(ordinaryFont, Color.WHITE))).expandX();
        table.row()
    }

    table.setFillParent(true)

    highscoresStage.addActor(table)
    highscoresStage.addActor(backBtn)
    highscoresStage.addActor(clearBtn)
}
