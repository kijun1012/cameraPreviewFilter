package com.kijun.retrica.camera.opengl.common

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

// quad 데이터와 버퍼를 갖고 있는 클래스
class FullscreenQuad {
    private val quad = floatArrayOf(
        -1f, -1f, 0f, 0f, // 좌하단,
        1f, -1f, 1f, 0f, // 우하단,
        -1f, 1f, 0f, 1f, // 좌상단,
        1f, 1f, 1f, 1f // 우상단
    )

    private val quadBuf: FloatBuffer = ByteBuffer.allocateDirect(quad.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(quad).position(0) }

    // aPos, aTex 연결
    fun bind(aPos: Int, aTex: Int) {
        quadBuf.position(0)
        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 4 * 4, quadBuf)

        quadBuf.position(2)
        GLES20.glEnableVertexAttribArray(aTex)
        GLES20.glVertexAttribPointer(aTex, 2, GLES20.GL_FLOAT, false, 4 * 4, quadBuf)
    }

    fun unbind(aPos: Int, aTex: Int) {
        GLES20.glDisableVertexAttribArray(aPos)
        GLES20.glDisableVertexAttribArray(aTex)
    }
}