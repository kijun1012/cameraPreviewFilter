package com.kijun.retrica.camera.common

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

class CameraController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
) {

    fun bind(renderer: RenderView) {
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val ucg = UseCaseGroup.Builder()

            if (renderer is PreviewConsumer) {
                val preview = Preview.Builder().build()

                preview.setSurfaceProvider { req ->
                    renderer.onSurfaceRequest(req, mainExecutor)
                }

                ucg.addUseCase(preview)
            }

            if (renderer is AnalysisConsumer) {
                TODO()
                // using canvas or yuv buffer
                // cameraX ImageAnalysis use case
            }

            // 카메라 선택
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // 바인딩되어 있는 use case 해제. 충돌 방지.
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, ucg.build())

        }, mainExecutor)
    }
}