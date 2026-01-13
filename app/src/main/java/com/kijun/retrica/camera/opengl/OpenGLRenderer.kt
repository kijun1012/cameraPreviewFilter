package com.kijun.retrica.camera.opengl

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.Surface
import androidx.camera.core.SurfaceRequest
import com.kijun.retrica.camera.opengl.filter.FilterChain
import com.kijun.retrica.camera.opengl.filter.FilterType
import com.kijun.retrica.util.FpsMonitor
import com.kijun.retrica.util.GLUtils
import java.util.concurrent.Executor
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLRenderer(private val glView: GLSurfaceView) : GLSurfaceView.Renderer {

    private var pendingRequest: SurfaceRequest? = null
    private var pendingExecutor: Executor? = null

    private var oesTexId = 0
    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null

    private val texM = FloatArray(16)

    // ✅ OES->2D + filters + screen
    private val chain = FilterChain()

    var fpsMonitor: FpsMonitor? = null

    // CameraX에게 Surface 제공하기 위한 함수.
    fun onSurfaceRequest(request: SurfaceRequest, executor: Executor) {
        pendingRequest?.willNotProvideSurface() // 이전 request 취소
        pendingRequest = request
        pendingExecutor = executor

        // Surface 제공은 GL thread에서
        glView.queueEvent { tryProvideSurface() }
    }

    // filter 재설정
    fun setFilters(types: List<FilterType>) {
        chain.setFilters(types)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        oesTexId = GLUtils.createOesTex()

        surfaceTexture = SurfaceTexture(oesTexId).apply {
            setOnFrameAvailableListener { glView.requestRender() }
        }

        chain.initGL()

        // 혹시 request가 먼저 들어왔으면 제공
        tryProvideSurface()
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        GLES20.glViewport(0, 0, w, h)
        chain.onSizeChanged(w, h)
    }

    override fun onDrawFrame(gl: GL10?) {
        val st = surfaceTexture ?: return

        st.updateTexImage()
        st.getTransformMatrix(texM) // 회전, 크롭, 반전등의 transform 데이터를 가져와서 저장.

        chain.draw(oesTexId, texM) // 각 필터에서 그린다.
        fpsMonitor?.tickFps()
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
}