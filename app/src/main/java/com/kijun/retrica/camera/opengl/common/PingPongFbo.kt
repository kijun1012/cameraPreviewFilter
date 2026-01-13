package com.kijun.retrica.camera.opengl.common

import android.opengl.GLES20


// frame buffer object + texId. 체이닝 중간 결과값을 저장하기 위한 버퍼.
//
data class Fbo(val fboId: Int, val texId: Int, val w: Int, val h: Int)

// FBO를 필터마다 만들지 않고 번갈아가면서 사용하기 위한 클래스.
class PingPongFbo {
    private var a: Fbo? = null
    private var b: Fbo? = null
    private var flip = false

    fun resize(w: Int, h: Int) {
        destroy()
        a = createFbo(w, h)
        b = createFbo(w, h)
        flip = false
    }

    fun write(): Fbo = if (!flip) a!! else b!!

    fun swap() {
        flip = !flip
    }

    fun destroy() {
        listOf(a, b).forEach { f ->
            if (f != null) {
                GLES20.glDeleteFramebuffers(1, intArrayOf(f.fboId), 0)
                GLES20.glDeleteTextures(1, intArrayOf(f.texId), 0)
            }
        }
        a = null; b = null
    }

    private fun createFbo(w: Int, h: Int): Fbo {
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
            w, h, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )

        val fbo = IntArray(1)
        GLES20.glGenFramebuffers(1, fbo, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0])
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            tex[0],
            0
        )

        // 디버깅 용도
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        check(status == GLES20.GL_FRAMEBUFFER_COMPLETE) { "FBO incomplete: $status" }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        return Fbo(fbo[0], tex[0], w, h)
    }
}