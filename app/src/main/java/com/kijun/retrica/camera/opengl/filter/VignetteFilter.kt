package com.kijun.retrica.camera.opengl.filter

import android.opengl.GLES20
import com.kijun.retrica.camera.opengl.common.FullscreenQuad
import com.kijun.retrica.camera.opengl.common.GlProgram

class VignetteFilter(private val quad: FullscreenQuad) : GLFilter2D {
    private var program = 0
    private var aPos = -1
    private var aTex = -1
    private var uTex = -1
    private var uCenter = -1
    private var uRadius = -1
    private var uSoftness = -1
    private var uStrength = -1

    var centerX = 0.5f
    var centerY = 0.5f
    var radius = 0.65f
    var softness = 0.35f
    var strength = 0.85f

    override fun init() {
        program = GlProgram.build(VS_2D, FS_VIGNETTE)
        aPos = GLES20.glGetAttribLocation(program, "aPos")
        aTex = GLES20.glGetAttribLocation(program, "aTex")
        uTex = GLES20.glGetUniformLocation(program, "uTex")
        uCenter = GLES20.glGetUniformLocation(program, "uCenter")
        uRadius = GLES20.glGetUniformLocation(program, "uRadius")
        uSoftness = GLES20.glGetUniformLocation(program, "uSoftness")
        uStrength = GLES20.glGetUniformLocation(program, "uStrength")
    }

    override fun draw(inputTex2D: Int) {
        GLES20.glUseProgram(program)

        GLES20.glUniform2f(uCenter, centerX, centerY)
        GLES20.glUniform1f(uRadius, radius)
        GLES20.glUniform1f(uSoftness, softness)
        GLES20.glUniform1f(uStrength, strength)

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
        private const val FS_VIGNETTE = """
            precision mediump float;
            varying vec2 vTex;
            uniform sampler2D uTex;
            uniform vec2 uCenter;
            uniform float uRadius;
            uniform float uSoftness;
            uniform float uStrength;

            void main() {
                vec4 c = texture2D(uTex, vTex);
                float d = distance(vTex, uCenter);
                float t = smoothstep(uRadius, uRadius - uSoftness, d); 
                float vig = mix(1.0 - uStrength, 1.0, t);
                gl_FragColor = vec4(c.rgb * vig, c.a);
            }
        """
    }
}