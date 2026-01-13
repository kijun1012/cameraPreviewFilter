package com.kijun.retrica.camera.opengl.filter

import com.kijun.retrica.camera.opengl.common.FullscreenQuad

object FilterFactory {
    fun create(type: FilterType, quad: FullscreenQuad): GLFilter2D {
        return when (type) {
            FilterType.GRAYSCALE -> GrayScaleFilter(quad)
            FilterType.VIGNETTE -> VignetteFilter(quad)
        }
    }
}