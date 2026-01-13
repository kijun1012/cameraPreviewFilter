package com.kijun.retrica.camera.common

import android.view.View
import androidx.camera.core.SurfaceRequest
import com.kijun.retrica.camera.opengl.filter.FilterType
import com.kijun.retrica.util.FpsMonitor
import java.util.concurrent.Executor


interface RenderView {
    val view: View
    fun onResume()
    fun onPause()
    fun setFilters(types: List<FilterType>) {}
    fun setFpsMonitor(fpsMonitor: FpsMonitor) {}
}

// preview용 consumer. cameraX preivew가 주는 surface 연결.
interface PreviewConsumer {
    fun onSurfaceRequest(request: SurfaceRequest, executor: Executor)
}

// analysis용 consumer.
interface AnalysisConsumer {}