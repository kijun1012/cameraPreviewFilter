package com.kijun.retrica.camera.opengl.filter

import android.opengl.GLES20
import com.kijun.retrica.camera.opengl.common.FullscreenQuad
import com.kijun.retrica.camera.opengl.common.GlProgram

class GrayScaleFilter(private val quad: FullscreenQuad) : GLFilter2D {
    private var program = 0
    private var aPos = -1
    private var aTex = -1
    private var uTex = -1

    override fun init() {
        program = GlProgram.build(VS_2D, FS_GRAY)
        aPos = GLES20.glGetAttribLocation(program, "aPos")
        aTex = GLES20.glGetAttribLocation(program, "aTex")
        uTex = GLES20.glGetUniformLocation(program, "uTex")
    }

    override fun draw(inputTex2D: Int) {
        GLES20.glUseProgram(program)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTex2D)
        GLES20.glUniform1i(uTex, 0)

        quad.bind(aPos, aTex)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        quad.unbind(aPos, aTex)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    override fun release() {
        GlProgram.delete(program)
        program = 0
    }

    companion object {
        private const val VS_2D = """
            attribute vec2 aPos;
            attribute vec2 aTex;
            varying vec2 vTex;
            void main() {
                gl_Position = vec4(aPos, 0.0, 1.0);
                vTex = aTex;
            }
        """
        private const val FS_GRAY = """
            precision mediump float;
            varying vec2 vTex;
            uniform sampler2D uTex;
            void main() {
                vec4 c = texture2D(uTex, vTex);
                float g = dot(c.rgb, vec3(0.299, 0.587, 0.114));
                gl_FragColor = vec4(vec3(g), c.a);
            }
        """
    }
}