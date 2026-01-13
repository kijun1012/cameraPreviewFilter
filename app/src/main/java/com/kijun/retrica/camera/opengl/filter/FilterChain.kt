package com.kijun.retrica.camera.opengl.filter

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.kijun.retrica.camera.opengl.common.FullscreenQuad
import com.kijun.retrica.camera.opengl.common.GlProgram
import com.kijun.retrica.camera.opengl.common.PingPongFbo

class FilterChain {
    private val quad = FullscreenQuad()

    // 내부 고정 단계
    private var progOesTo2D = 0
    private var progBlit = 0

    // 사용자 선택 단계(2D 필터들)
    private val filters = mutableListOf<GLFilter2D>()
    private var pendingTypes: List<FilterType> = emptyList()
    private var glInited = false

    private val pingPong = PingPongFbo()

    private var viewW = 0
    private var viewH = 0

    fun initGL() {
        progOesTo2D = GlProgram.build(VS_OES, FS_OES_TO_2D)
        progBlit = GlProgram.build(VS_2D, FS_BLIT)

        glInited = true
        applyPendingFiltersIfNeeded()
    }

    fun onSizeChanged(w: Int, h: Int) {
        viewW = w
        viewH = h
        pingPong.resize(w, h)
    }

    fun setFilters(types: List<FilterType>) {
        pendingTypes = types
        if (glInited) applyPendingFiltersIfNeeded()
    }

    private fun applyPendingFiltersIfNeeded() {
        // 기존 필터 release
        filters.forEach { it.release() }
        filters.clear()

        // 새 필터 생성 + init
        pendingTypes.forEach { t ->
            val f = FilterFactory.create(t, quad)
            f.init()
            filters += f
        }
    }

    fun draw(oesTexId: Int, texM: FloatArray) {
        if (!glInited) return

        // OES -> 2D Texture로 변경
        val out0 = pingPong.write()
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, out0.fboId)
        GLES20.glViewport(0, 0, out0.w, out0.h)
        drawOesTo2D(oesTexId, texM)

        pingPong.swap()
        var input2D = pingPong.read().texId

        // 필터 적용
        for (f in filters) {
            val out = pingPong.write()
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, out.fboId)
            GLES20.glViewport(0, 0, out.w, out.h)
            f.draw(input2D)

            pingPong.swap()
            input2D = pingPong.read().texId
        }

        // 화면 표시
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glViewport(0, 0, viewW, viewH)
        draw2DToScreen(input2D)
    }

    private fun drawOesTo2D(oesTexId: Int, texM: FloatArray) {
        GLES20.glUseProgram(progOesTo2D)

        val aPos = GLES20.glGetAttribLocation(progOesTo2D, "aPos")
        val aTex = GLES20.glGetAttribLocation(progOesTo2D, "aTex")
        val uTexMatrix = GLES20.glGetUniformLocation(progOesTo2D, "uTexMatrix")
        val uOes = GLES20.glGetUniformLocation(progOesTo2D, "uOes")

        GLES20.glUniformMatrix4fv(uTexMatrix, 1, false, texM, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTexId)
        GLES20.glUniform1i(uOes, 0)

        quad.bind(aPos, aTex)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        quad.unbind(aPos, aTex)

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
    }

    private fun draw2DToScreen(tex2D: Int) {
        GLES20.glUseProgram(progBlit)

        val aPos = GLES20.glGetAttribLocation(progBlit, "aPos")
        val aTex = GLES20.glGetAttribLocation(progBlit, "aTex")
        val uTex = GLES20.glGetUniformLocation(progBlit, "uTex")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex2D)
        GLES20.glUniform1i(uTex, 0)

        quad.bind(aPos, aTex)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        quad.unbind(aPos, aTex)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    companion object {
        private const val VS_OES = """
            attribute vec2 aPos;
            attribute vec2 aTex;
            uniform mat4 uTexMatrix;
            varying vec2 vTex;
            void main() {
                gl_Position = vec4(aPos, 0.0, 1.0);
                vec4 tc = uTexMatrix * vec4(aTex, 0.0, 1.0);
                vTex = tc.xy;
            }
        """
        private const val FS_OES_TO_2D = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vTex;
            uniform samplerExternalOES uOes;
            void main() {
                gl_FragColor = texture2D(uOes, vTex);
            }
        """
        private const val VS_2D = """
            attribute vec2 aPos;
            attribute vec2 aTex;
            varying vec2 vTex;
            void main() {
                gl_Position = vec4(aPos, 0.0, 1.0);
                vTex = aTex;
            }
        """
        private const val FS_BLIT = """
            precision mediump float;
            varying vec2 vTex;
            uniform sampler2D uTex;
            void main() {
                gl_FragColor = texture2D(uTex, vTex);
            }
        """
    }
}
