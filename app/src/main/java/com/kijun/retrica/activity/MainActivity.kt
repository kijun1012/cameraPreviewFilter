package com.kijun.retrica.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kijun.retrica.R
import com.kijun.retrica.camera.common.CameraController
import com.kijun.retrica.camera.common.RenderType
import com.kijun.retrica.camera.common.RendererFactory
import com.kijun.retrica.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // 라이브러리 변경시에는 여기서 type만 바꿔주면 된다.
    private val renderer by lazy { RendererFactory.create(this, RenderType.OPENGL) }
    private val cameraController by lazy { CameraController(this, this) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraController.bind(renderer)
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

    private fun permissionGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED


    private fun checkPermission() {
        if (permissionGranted()) {
            cameraController.bind(renderer)
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