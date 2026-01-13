package com.kijun.retrica.camera.common

import android.content.Context
import com.kijun.retrica.camera.opengl.OpenGLPreviewRenderer

// 해당 타입에 따라 renderer 생성. VULKAN, CANVAS 등
enum class RenderType { OPENGL }

object RendererFactory {
    fun create(context: Context, type: RenderType): RenderView {
        return when (type) {
            RenderType.OPENGL -> OpenGLPreviewRenderer(context)
        }
    }
}