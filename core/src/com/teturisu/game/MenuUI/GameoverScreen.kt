package com.teturisu.game.MenuUI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.FitViewport
import com.teturisu.game.TheGame.Score


fun TetrisMainMenu.addHighscore(name: String, score: Int){
    if (name.isEmpty()) return

    val currMin = highscoresData.toList().minBy { it.second }!!.second
    if (score > currMin) {
        if (!highscoresData.contains(name) || highscoresData.get(name)!! < score) {
            highscoresData.put(name, score)

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


fun TetrisMainMenu.initGameoverScreen(){
    val viewport = FitViewport(width/2f, height/2f)
    gameoverStage = Stage(viewport)


    val skin = Skin(Gdx.files.internal("skin/uiskin.json"))
    val table = Table(skin)


    val btnStyle = TextButton.TextButtonStyle()
    btnStyle.font = cartoonFont
    btnStyle.fontColor = Color(0.7f, 1f, 0.5f, 1f)



    val nameField = TextField("", skin)

    // button to save highscore
    val okButton = TextButton("Save", btnStyle)
    okButton.color = Color(1f, 0.8f, 0.5f, 1f)
    okButton.touchable = Touchable.enabled
    okButton.addListener(object : ClickListener() {
        override fun clicked(event: InputEvent, x: Float, y: Float) {
            addHighscore(nameField.text, Score.score.toInt())
            setStage(TetrisMainMenu.GameState.HIGHSCORES)
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
    table.touchable = Touchable.enabled

    gameoverStage.addActor(table)
}