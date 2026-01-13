package com.kijun.retrica.camera.opengl

import android.content.Context
import android.view.View
import androidx.camera.core.SurfaceRequest
import com.kijun.retrica.camera.common.PreviewConsumer
import com.kijun.retrica.camera.common.RenderView
import java.util.concurrent.Executor

class OpenGLPreviewRenderer(context: Context) : RenderView, PreviewConsumer {
    private val glView = OpenGLPreviewView(context)
    override val view: View = glView

    override fun onSurfaceRequest(
        request: SurfaceRequest,
        executor: Executor
    ) {
        glView.renderer.onSurfaceRequest(request, executor)
    }

    override fun onResume() = glView.onResume()

    override fun onPause() = glView.onPause()

}