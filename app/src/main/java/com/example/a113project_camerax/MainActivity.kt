package com.example.a113project_camerax

// 引入需要的包
import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import android.view.WindowInsetsController
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


class MainActivity : AppCompatActivity() {
    // 宣告預覽視圖變數
    private lateinit var previewView: PreviewView
    // 宣告一個可以為null的ImageCapture變數
    private var imageCapture: ImageCapture? = null

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10     
        // 定義需要的權限列表
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA, // 相機
            Manifest.permission.RECORD_AUDIO, // 麥克風
            Manifest.permission.ACCESS_FINE_LOCATION, // 精確位置
            Manifest.permission.ACCESS_COARSE_LOCATION, // 大略位置
            Manifest.permission.BLUETOOTH // 藍牙

        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化預覽視圖元件
        previewView = findViewById(R.id.previewView)

        // 啟用沉浸模式函數
        enterImmersiveMode()

        // 檢查是否已經授予所有必需的權限
        if (allPermissionsGranted()) {
            // 如果權限已授予，則啟動相機
            startCamera()
        } else {
            // 如果權限未授予，則請求權限
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // 設置拍照按鈕的點擊事件
        findViewById<Button>(R.id.camera_capture_button).setOnClickListener {
            takePhoto()
        }
    }

    // 檢查是否已授予所有必需的權限的函數
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // 啟動相機的函數
    private fun startCamera() {
        // 獲取相機的提供者
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // 創建預覽
            val preview = Preview.Builder().build()

            // 將預覽的表面提供者設置為預覽視圖的表面提供者
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // 初始化 ImageCapture
            imageCapture = ImageCapture.Builder().build()

            // 選擇後置相機
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 解綁所有綁定到相機的用例
                cameraProvider.unbindAll()
                // 將相機綁定到生命周期和用例
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                // 如果綁定失敗，顯示錯誤訊息
                Toast.makeText(this, "Failed to bind camera preview: ${exc.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // 拍照的函數
    private fun takePhoto() {
        // 如果imageCapture為null，則直接返回
        val imageCapture = imageCapture ?: return

        // 創建文件以保存圖像
        val photoFile = File(
            getOutputDirectory(),
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        // 創建包含文件和元數據的輸出選項對象
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // 執行拍照
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // 將文件的Uri提取出來
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    // 顯示Toast訊息
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    // 用Log輸出訊息
                    Log.d("CameraXApp", msg)

                    // 觸發媒體掃描器掃描文件
                    MediaScannerConnection.scanFile(
                        baseContext,
                        arrayOf(photoFile.toString()),
                        null,
                        null
                    )
                }

                override fun onError(exc: ImageCaptureException) {
                    val msg = "Photo capture failed: ${exc.message}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // 獲取輸出目錄的函數
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

//舊的寫法
//    // 當窗口焦點變化時，重新啟用沉浸模式的函數
//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus) {
//            enterImmersiveMode()
//        }
//    }
//    // 啟用immersive mode函數
//    private fun enterImmersiveMode() {
//        window.decorView.systemUiVisibility = (
//                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                        // 設置內容顯示在系統欄下方，這樣當系統欄隱藏和顯示時內容不會重新調整大小。
//                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        // 隱藏導航欄和狀態欄
//                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        or View.SYSTEM_UI_FLAG_FULLSCREEN
//                )
//    }
// 當窗口焦點變化時，重新啟用沉浸模式的函數
override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus) {
        enterImmersiveMode()
    }
}
    // 啟用immersive mode函數
    private fun enterImmersiveMode() {
        window.decorView.windowInsetsController?.let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }



    // 設定並綁定相機預覽的函數
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder().build()
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        // 設置預覽視圖的表面提供者
        preview.setSurfaceProvider(previewView.surfaceProvider)

        try {
            // 解綁之前的用例並綁定新的用例到相機生命週期
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview
            )
        } catch (exc: Exception) {
            // 如果綁定失敗，顯示錯誤訊息
            Toast.makeText(this, "Failed to bind camera preview: ${exc.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // 處理權限請求的結果的函數
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // 將權限請求結果與權限列表配對
            val permissionResults = permissions.zip(grantResults.toTypedArray())
            // 篩選出未授予的權限
            val deniedPermissions =
                permissionResults.filter { it.second != PackageManager.PERMISSION_GRANTED }

            if (deniedPermissions.isEmpty()) {
                // 如果權限都已授予，啟動相機
                startCamera()
            } else {
                // 識別並顯示未授予的權限
                val deniedPermissionsNames = deniedPermissions.joinToString { it.first }
                Toast.makeText(
                    this,
                    "未授予的權限：$deniedPermissionsNames", Toast.LENGTH_SHORT
                ).show()
                // 由於缺少必要權限，關閉應用
                finish()
            }
        }
    }
}
