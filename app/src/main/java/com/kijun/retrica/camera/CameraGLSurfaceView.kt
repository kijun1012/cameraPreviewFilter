package com.kijun.retrica.camera

import android.content.Context
import android.opengl.GLSurfaceView

class CameraGLSurfaceView(context: Context) : GLSurfaceView(context) {

    val renderer: CameraRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = CameraRenderer(this)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        preserveEGLContextOnPause = true
    }
}
