package com.kijun.retrica.camera.common

import android.content.Context
import com.kijun.retrica.camera.opengl.OpenGLPreviewRenderer

enum class RenderBackend { OPENGL }

object RendererFactory {
    fun create(context: Context, backend: RenderBackend): PreviewRenderer {
        return when (backend) {
            RenderBackend.OPENGL -> OpenGLPreviewRenderer(context)
        }
    }
}