//package com.example.a113project_camerax
//
//// 引入需要的包
//import android.Manifest
//import android.content.ContentValues
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.provider.MediaStore
//import android.view.View
//import android.widget.Button
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageCapture
//import androidx.camera.core.ImageCaptureException
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import java.io.File
//import java.text.SimpleDateFormat
//import java.util.Locale
//import android.media.MediaScannerConnection
//import android.os.Environment
//import android.util.Log
//import android.view.WindowInsetsController
//import androidx.core.view.WindowInsetsCompat
//import androidx.core.view.WindowInsetsControllerCompat
//import android.widget.ImageView
//
//
//
//class MainActivity : AppCompatActivity() {
//    // 宣告預覽視圖變數
//    private lateinit var previewView: PreviewView
//    // 宣告一個可以為null的ImageCapture變數
//    private var imageCapture: ImageCapture? = null
//
//    val temperatureData = arrayOf(39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 38.0, 38.0)
//
//    companion object {
//        private const val REQUEST_CODE_PERMISSIONS = 10
//        // 定義需要的權限列表
//        private val REQUIRED_PERMISSIONS = arrayOf(
//            Manifest.permission.CAMERA, // 相機
//            Manifest.permission.RECORD_AUDIO, // 麥克風
//            Manifest.permission.ACCESS_FINE_LOCATION, // 精確位置
//            Manifest.permission.ACCESS_COARSE_LOCATION, // 大略位置
//            Manifest.permission.BLUETOOTH // 藍牙
//
//        )
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // 初始化預覽視圖元件
//        previewView = findViewById(R.id.previewView)
//
//        // 啟用沉浸模式函數
//        enterImmersiveMode()
//
//        // 檢查是否已經授予所有必需的權限
//        if (allPermissionsGranted()) {
//            // 如果權限已授予，則啟動相機
//            startCamera()
//        } else {
//            // 如果權限未授予，則請求權限
//            ActivityCompat.requestPermissions(
//                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
//            )
//        }
//
//        // 設置拍照按鈕的點擊事件
//        findViewById<Button>(R.id.camera_capture_button).setOnClickListener {
//            takePhoto()
//        }
//    }
//
//    // 檢查是否已授予所有必需的權限的函數
//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
//    }
//
//    // 啟動相機的函數
//    private fun startCamera() {
//        // 獲取相機的提供者
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//
//            // 創建預覽
//            val preview = Preview.Builder().build()
//
//            // 將預覽的表面提供者設置為預覽視圖的表面提供者
//            preview.setSurfaceProvider(previewView.surfaceProvider)
//
//            // 初始化 ImageCapture
//            imageCapture = ImageCapture.Builder().build()
//
//            // 選擇後置相機
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                // 解綁所有綁定到相機的用例
//                cameraProvider.unbindAll()
//
//                // 將相機綁定到生命周期和用例
//                cameraProvider.bindToLifecycle(
//                    this, cameraSelector, preview, imageCapture
//                )
//
//                // 獲取 overlay 的 ImageView
//                val overlayImageView = findViewById<ImageView>(R.id.overlayImageView)
//                // 設置 overlay 的可見性
//                overlayImageView.visibility = View.VISIBLE  // 或 View.INVISIBLE，取決於你的需求
//
//            } catch (exc: Exception) {
//                // 如果綁定失敗，顯示錯誤訊息
//                Toast.makeText(this, "Failed to bind camera preview: ${exc.localizedMessage}", Toast.LENGTH_SHORT).show()
//            }
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//
//    // 拍照的函數
//    private fun takePhoto() {
//        // 如果imageCapture為null，則直接返回
//        val imageCapture = imageCapture ?: return
//
//        // 創建文件以保存圖像
//        val photoFile = File(
//            getOutputDirectory(),
//            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
//                .format(System.currentTimeMillis()) + ".jpg"
//        )
//
//        // 創建包含文件和元數據的輸出選項對象
//        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
//
//        // 執行拍照
//        imageCapture.takePicture(
//            outputOptions,
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                    // 將文件的Uri提取出來
//                    val savedUri = Uri.fromFile(photoFile)
//                    val msg = "Photo capture succeeded: $savedUri"
//                    // 顯示Toast訊息
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                    // 用Log輸出訊息
//                    Log.d("CameraXApp", msg)
//
//                    // 觸發媒體掃描器掃描文件
//                    MediaScannerConnection.scanFile(
//                        baseContext,
//                        arrayOf(photoFile.toString()),
//                        null,
//                        null
//                    )
//                }
//
//                override fun onError(exc: ImageCaptureException) {
//                    val msg = "Photo capture failed: ${exc.message}"
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                }
//            }
//        )
//    }
//
//    // 獲取輸出目錄的函數
//    private fun getOutputDirectory(): File {
//        val mediaDir = externalMediaDirs.firstOrNull()?.let {
//            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
//        }
//        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
//    }
//
////舊的寫法
////    // 當窗口焦點變化時，重新啟用沉浸模式的函數
////    override fun onWindowFocusChanged(hasFocus: Boolean) {
////        super.onWindowFocusChanged(hasFocus)
////        if (hasFocus) {
////            enterImmersiveMode()
////        }
////    }
////    // 啟用immersive mode函數
////    private fun enterImmersiveMode() {
////        window.decorView.systemUiVisibility = (
////                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
////                        // 設置內容顯示在系統欄下方，這樣當系統欄隱藏和顯示時內容不會重新調整大小。
////                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
////                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
////                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
////                        // 隱藏導航欄和狀態欄
////                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
////                        or View.SYSTEM_UI_FLAG_FULLSCREEN
////                )
////    }
//// 當窗口焦點變化時，重新啟用沉浸模式的函數
//override fun onWindowFocusChanged(hasFocus: Boolean) {
//    super.onWindowFocusChanged(hasFocus)
//    if (hasFocus) {
//        enterImmersiveMode()
//    }
//}
//    // 啟用immersive mode函數
//    private fun enterImmersiveMode() {
//        window.decorView.windowInsetsController?.let { controller ->
//            controller.hide(WindowInsetsCompat.Type.systemBars())
//            controller.systemBarsBehavior =
//                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        }
//    }
//
//
//
//    // 設定並綁定相機預覽的函數
//    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
//        val preview: Preview = Preview.Builder().build()
//        val cameraSelector: CameraSelector = CameraSelector.Builder()
//            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//            .build()
//
//        // 設置預覽視圖的表面提供者
//        preview.setSurfaceProvider(previewView.surfaceProvider)
//
//        try {
//            // 解綁之前的用例並綁定新的用例到相機生命週期
//            cameraProvider.unbindAll()
//            cameraProvider.bindToLifecycle(
//                this, cameraSelector, preview
//            )
//        } catch (exc: Exception) {
//            // 如果綁定失敗，顯示錯誤訊息
//            Toast.makeText(this, "Failed to bind camera preview: ${exc.localizedMessage}", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // 處理權限請求的結果的函數
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            // 將權限請求結果與權限列表配對
//            val permissionResults = permissions.zip(grantResults.toTypedArray())
//            // 篩選出未授予的權限
//            val deniedPermissions =
//                permissionResults.filter { it.second != PackageManager.PERMISSION_GRANTED }
//
//            if (deniedPermissions.isEmpty()) {
//                // 如果權限都已授予，啟動相機
//                startCamera()
//            } else {
//                // 識別並顯示未授予的權限
//                val deniedPermissionsNames = deniedPermissions.joinToString { it.first }
//                Toast.makeText(
//                    this,
//                    "未授予的權限：$deniedPermissionsNames", Toast.LENGTH_SHORT
//                ).show()
//                // 由於缺少必要權限，關閉應用
//                finish()
//            }
//        }
//    }
//}

package com.example.a113project_camerax

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import android.widget.Button
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import android.view.WindowInsetsController
import androidx.core.view.WindowInsetsControllerCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.BitmapFactory
import java.io.IOException


class MainActivity : AppCompatActivity() {

    // 声明控件变量
    private lateinit var previewView: PreviewView
    private var imageCapture: ImageCapture? = null

    // 模拟温度数据数组
    val temperatureData = arrayOf(
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,
        39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0,
        39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0,

        )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化预览视图
        previewView = findViewById(R.id.previewView)

        // 进入沉浸模式
        enterImmersiveMode()

        // 检查并请求权限
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // 設置拍照按鈕的點擊事件
        findViewById<Button>(R.id.camera_capture_button).setOnClickListener {
            takePhoto()
        }

        // 再次检查权限
        checkPermissions()
    }

    // 检查所有必要权限是否被授予
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // 启动相机
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // 創建預覽
            val preview = Preview.Builder().build()

            // 將預覽的表面提供者設置為預覽視圖的表面提供者
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // 初始化 ImageCapture(创建图像捕获实例)
            imageCapture = ImageCapture.Builder().build()

            // 选择后置摄像头
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 解綁所有綁定到相機的用例
                cameraProvider.unbindAll()

                // 將相機綁定到生命周期和用例
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                // 显示覆盖图像（热图）
                val overlayImageView = findViewById<ImageView>(R.id.overlayImageView)
                // 設置 overlay 的可見性
                overlayImageView.visibility = View.VISIBLE

                // 生成并设置热图位图
                val heatMapBitmap = generateHeatMapBitmap(temperatureData, 32, 32)
                overlayImageView.setImageBitmap(heatMapBitmap)
                // 应用模糊效果
                applyBlur(overlayImageView, 1f)

            } catch (exc: Exception) {
                // 如果綁定失敗，顯示錯誤訊息
                Toast.makeText(this, "Failed to bind camera preview: ${exc.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }


//    private fun takePhoto() {
//        val imageCapture = imageCapture ?: return
//
//        // 創建保存原始照片的文件
//        val originalPhotoFile = File(
//            getOutputDirectory(),
//            "original_${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
//                .format(System.currentTimeMillis())}.jpg"
//        )
//        val originalOutputOptions = ImageCapture.OutputFileOptions.Builder(originalPhotoFile).build()
//
//        // 創建保存經過處理的 Bitmap 照片的文件
//        val processedPhotoFile = File(
//            getOutputDirectory(),
//            "processed_${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
//                .format(System.currentTimeMillis())}.jpg"
//        )
//        val processedOutputOptions = ImageCapture.OutputFileOptions.Builder(processedPhotoFile).build()
//
//        // 執行拍照
//        imageCapture.takePicture(
//            originalOutputOptions,
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                    // 保存原始照片後的處理
//                    processAndSaveBitmap(originalPhotoFile, processedPhotoFile)
//                }
//
//                override fun onError(exc: ImageCaptureException) {
//                    Log.e("CameraXApp", "Photo capture failed: ${exc.message}", exc)
//                    val msg = "Photo capture failed: ${exc.message}"
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                }
//            }
//        )
//    }
//
//    // 處理並保存 Bitmap
//    private fun processAndSaveBitmap(originalFile: File, processedFile: File) {
//        // 假設你有一個名為 processBitmap 的方法處理原始照片並返回處理後的 Bitmap
//        val processedBitmap: Bitmap = processBitmap(originalFile)
//
//        // 保存處理後的 Bitmap 到文件
//        saveBitmapToFile(processedBitmap, processedFile)
//
//        // 同樣進行掃描，通知系統掃描新創建的文件，以便它能夠在相冊或媒體庫中顯示
//        MediaScannerConnection.scanFile(
//            baseContext,
//            arrayOf(originalFile.toString(), processedFile.toString()),
//            null,
//            null
//        )
//    }
//
//    // 假設你有一個名為 processBitmap 的方法處理原始照片並返回處理後的 Bitmap
//    private fun processBitmap(originalFile: File): Bitmap {
//        // 在這裡執行處理原始照片的邏輯，返回處理後的 Bitmap
//        // 這裡僅為示例，實際邏輯應根據你的需求進行實現
//        return BitmapFactory.decodeFile(originalFile.path)
//    }
//
//    // 保存 Bitmap 到文件
//    private fun saveBitmapToFile(bitmap: Bitmap, outputFile: File) {
//        try {
//            val fileOutputStream = FileOutputStream(outputFile)
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
//            fileOutputStream.flush()
//            fileOutputStream.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//
//

    // 拍照的函數
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        // 创建保存照片的文件
        val photoFile = File(
            getOutputDirectory(),
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )
        // 創建包含文件和元數據的輸出選項對象
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()





        // 执行拍照
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
                // 拍照失败处理
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraXApp", "Photo capture failed: ${exc.message}", exc)
                    val msg = "Photo capture failed: ${exc.message}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // 生成热图的位图
    private fun generateHeatMapBitmap(data: Array<Double>, width: Int, height: Int): Bitmap {
        val maxTemperature = data.maxOrNull() ?: 1.0
        val minTemperature = data.minOrNull() ?: 0.0

        // 创建位图
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // 遍历温度数据并绘制矩形
        for (i in data.indices) {
            val x = i % width
            val y = i / width

            val normalizedValue = (data[i] - minTemperature) / (maxTemperature - minTemperature)
            val color = interpolateColor(normalizedValue)

            paint.color = color
            canvas.drawRect(x.toFloat(), y.toFloat(), (x + 1).toFloat(), (y + 1).toFloat(), paint)
        }

        return bitmap
    }

    // 根据归一化值插值计算颜色
    private fun interpolateColor(value: Double): Int {
        val blue = (255 * (1 - value)).toInt()
        val red = (255 * value).toInt()
        return Color.rgb(red, 0, blue)
    }

    // 应用模糊效果
    private fun applyBlur(imageView: ImageView, radius: Float) {
        val bitmapDrawable = imageView.drawable as? BitmapDrawable
        bitmapDrawable?.let {
            var blurredBitmap = Bitmap.createBitmap(
                bitmapDrawable.bitmap.width, bitmapDrawable.bitmap.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(blurredBitmap)
            canvas.drawBitmap(bitmapDrawable.bitmap, 0f, 0f, null)
            // 使用BlurBuilder中的模糊方法
            blurredBitmap = BlurBuilder.blur(this, blurredBitmap, radius)
            // 设置ImageView显示模糊后的位图
            imageView.setImageBitmap(blurredBitmap)
        }
    }

    // 请求所需权限
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
        )
    }

    // 获取文件输出目录
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: filesDir
    }
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

    // 检查权限
    private fun checkPermissions() {
        if (!allPermissionsGranted()) {
            requestPermissions()
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        // 所需权限数组
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH
        )
    }
}
