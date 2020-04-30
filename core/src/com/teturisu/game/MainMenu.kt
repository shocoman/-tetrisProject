package com.teturisu.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport


class MainMenuScreen : Screen {

    private var batch: SpriteBatch? = null
    protected var stage: Stage? = null
    private var viewport: Viewport? = null
    private var camera: OrthographicCamera? = null
    private var atlas: TextureAtlas? = null
    protected var skin: Skin? = null


    fun constructor() {
        atlas = TextureAtlas("skin.atlas")
        skin = Skin(Gdx.files.internal("skin.json"), atlas)
        batch = SpriteBatch()
        camera = OrthographicCamera()

        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()
        viewport = FitViewport(width, height, camera)
        (viewport as FitViewport).apply()
        camera!!.position[camera!!.viewportWidth / 2, camera!!.viewportHeight / 2] = 0f
        camera!!.update()
        stage = Stage(viewport, batch)
    }



    override fun show() {

    }

    override fun render(delta: Float) {

    }


    override fun resize(width: Int, height: Int) {

    }



    override fun hide() {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {

    }
}