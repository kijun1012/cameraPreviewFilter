package com.kijun.retrica

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import com.kijun.retrica.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
//        enableEdgeToEdge()
        setContentView(binding.root)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        checkPermission()

        binding.btnCapture.setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    private fun takePhoto() {

    }

    private fun startCamera() {

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission() {
        requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    companion object {
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}


