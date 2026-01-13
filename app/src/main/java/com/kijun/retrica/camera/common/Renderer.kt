package com.kijun.retrica.camera.common

import android.view.View
import androidx.camera.core.SurfaceRequest
import java.util.concurrent.Executor


interface RenderView {
    val view: View
    fun onResume()
    fun onPause()
}

// preview용 consumer. cameraX preivew가 주는 surface 연결.
interface PreviewConsumer {
    fun onSurfaceRequest(request: SurfaceRequest, executor: Executor)
}

// analysis용 consumer.
interface AnalysisConsumer {}