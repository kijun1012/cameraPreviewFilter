package com.kijun.retrica.camera.opengl

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.Surface
import androidx.camera.core.SurfaceRequest
import com.kijun.retrica.util.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.Executor
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLRenderer(private val glView: GLSurfaceView) : GLSurfaceView.Renderer {

    private var pendingRequest: SurfaceRequest? = null
    private var pendingExecutor: Executor? = null

    private var oesTexId = 0
    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null

    private var program = 0

    // 화면 위치와 텍스처 좌표
    private val quad = floatArrayOf(
        -1f, -1f, 1f, 1f, // 좌하단
        1f, -1f, 1f, 0f,  // 우하단
        -1f, 1f, 0f, 1f, // 좌상단
        1f, 1f, 0f, 0f   // 우상단.
    )

    // Open GL 용 변환
    private val quadBuf: FloatBuffer = ByteBuffer
        .allocateDirect(quad.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(quad).position(0) }

    // CameraX에게 Surface 제공하기 위한 함수.
    fun onSurfaceRequest(request: SurfaceRequest, executor: Executor) {
        pendingRequest?.willNotProvideSurface() // 이전 request 취소
        pendingRequest = request
        pendingExecutor = executor

        // Surface 제공은 GL thread에서
        glView.queueEvent { tryProvideSurface() }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        oesTexId = GLUtils.createOesTex()

        surfaceTexture = SurfaceTexture(oesTexId).apply {
            setOnFrameAvailableListener { glView.requestRender() }
        }

        program = GLUtils.buildProgram(VS, FS_OES)

        // 혹시 request가 먼저 들어왔으면 제공
        tryProvideSurface()
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        GLES20.glViewport(0, 0, w, h)
    }

    override fun onDrawFrame(gl: GL10?) {
        val st = surfaceTexture ?: return

        st.updateTexImage()
        GLES20.glUseProgram(program)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        val aPos = GLES20.glGetAttribLocation(program, "aPos")
        val aTex = GLES20.glGetAttribLocation(program, "aTex")
        val uTex = GLES20.glGetUniformLocation(program, "uTex")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTexId)
        GLES20.glUniform1i(uTex, 0)

        // apos 값 넣기
        quadBuf.position(0)
        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 4 * 4, quadBuf)

        // atex 값 넣기
        quadBuf.position(2)
        GLES20.glEnableVertexAttribArray(aTex)
        GLES20.glVertexAttribPointer(aTex, 2, GLES20.GL_FLOAT, false, 4 * 4, quadBuf)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(aPos)
        GLES20.glDisableVertexAttribArray(aTex)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
    }

    // surface 제공 함수.
    // request, executor, surfaceTexture가 모두 준비되어야 정상적으로 동작한다.
    private fun tryProvideSurface() {
        val req = pendingRequest ?: return
        val ex = pendingExecutor ?: return
        val st = surfaceTexture ?: return

        // CameraX가 원하는 해상도
        val size = req.resolution
        st.setDefaultBufferSize(size.width, size.height)

        surface?.release()
        surface = Surface(st)

        req.provideSurface(surface!!, ex) {}

        pendingRequest = null
        pendingExecutor = null
    }


    companion object {
        // Vertex Shader : 화면 위치를 텍스처 좌표로 변환
        private const val VS = """
            attribute vec2 aPos;
            attribute vec2 aTex;
            varying vec2 vTex;
            void main() {
                gl_Position = vec4(aPos, 0.0, 1.0);
                vTex = aTex;
            }
        """

        // Fragment Shader : 텍스처 좌표를 픽셀 컬러로 변환
        private const val FS_OES = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vTex;
            uniform samplerExternalOES uTex;
        
            void main() {
                vec4 c = texture2D(uTex, vTex);
        
                // 밝기(휘도) 기반 그레이스케일 (표준 가중치)
                float g = dot(c.rgb, vec3(0.299, 0.587, 0.114));
        
                gl_FragColor = vec4(vec3(g), c.a);
            }
        """
    }
}