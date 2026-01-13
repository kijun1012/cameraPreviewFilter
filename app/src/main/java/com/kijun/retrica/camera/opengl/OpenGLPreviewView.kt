package com.kijun.retrica.camera.opengl

import android.content.Context
import android.opengl.GLSurfaceView

class OpenGLPreviewView(context: Context) : GLSurfaceView(context) {

    val renderer: OpenGLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = OpenGLRenderer(this)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        preserveEGLContextOnPause = true
    }
}