package com.kijun.retrica.camera.opengl.filter

interface GLFilter2D {
    fun init()
    fun draw(inputTex2D: Int)
    fun release()
}