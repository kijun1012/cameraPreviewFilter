package com.kijun.retrica.camera.common

import android.view.View
import androidx.camera.core.SurfaceRequest
import java.util.concurrent.Executor


interface PreviewRenderer : PreviewRenderView, PreviewSurfaceProvider

interface PreviewRenderView {
    val view: View
    fun onResume()
    fun onPause()
}

interface PreviewSurfaceProvider {
    fun onSurfaceRequest(request: SurfaceRequest, executor: Executor)
}