package com.kijun.retrica.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.kijun.retrica.R
import com.kijun.retrica.camera.common.RenderBackend
import com.kijun.retrica.camera.common.RendererFactory
import com.kijun.retrica.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val renderer by lazy { RendererFactory.create(this, RenderBackend.OPENGL) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT)
                .show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.layoutMain.addView(renderer.view)

        checkPermission()
    }

    private fun startCamera() {
        val mainExecutor = ContextCompat.getMainExecutor(this)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // 프리뷰 객체 초기화 및 surface provider 설정.
            val preview = Preview.Builder().build()

            // renderer 로 preview의 surface 제공.
            preview.setSurfaceProvider { req ->
                renderer.onSurfaceRequest(req, mainExecutor)
            }

            // 카메라 선택
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // 바인딩되어 있는 use case 해제. 충돌 방지.
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview)

        }, mainExecutor)
    }

    private fun permissionGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED


    private fun checkPermission() {
        if (permissionGranted()) {
            startCamera()
            return
        }
        requestPermissionLauncher.launch(PERMISSION_CAMERA)
    }

    override fun onResume() {
        super.onResume()
        renderer.onResume()
    }

    override fun onPause() {
        renderer.onPause()
        super.onPause()
    }

    companion object {
        private const val PERMISSION_CAMERA = Manifest.permission.CAMERA
    }
}