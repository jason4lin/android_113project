package com.example.a113project

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView
import android.graphics.Bitmap
import android.graphics.Color
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import kotlin.math.log
import kotlin.random.Random


class Camera : AppCompatActivity() {
    private lateinit var viewFinder: PreviewView

    private fun updateOverlay(bitmap: Bitmap) {
        val overlayView = findViewById<ImageView>(R.id.overlayView)
        overlayView.setImageBitmap(bitmap)
    }

//    private fun createThermalBitmap(temperatureData: Array<Float>): Bitmap {
//        val width = temperatureData.size / 32
//        val bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
//        val maxTemperature = temperatureData.max()
//        val minTemperature = temperatureData.min()
//
//
//        for (x in 0 until width) {
//            for (y in 0 until width) {
//                // 將溫度轉換為顏色值，這裡需要你自己的實現方式
//                val color = temperatureToColor(temperatureData[x*32+y],maxTemperature,minTemperature)
//                Log.i("fuck","$color")
//                bitmap.setPixel(x, y, color)
//            }
//        }
//
//        return applyGaussianBlur(bitmap)
//    }
    private fun createThermalBitmap(temperatureData: Array<Double>): Bitmap {
        // ... 其他代碼 ...
        val width = temperatureData.size / 32
        val bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
        val maxTemperature = temperatureData.max()
        val minTemperature = temperatureData.min()

        for (x in 0 until width) {
            for (y in 0 until width) {
                val temperature = temperatureData[x * 32 + y]
                val color = temperatureToColor(temperature, maxTemperature ?: 0.0, minTemperature ?: 0.0)
                bitmap.setPixel(x, y, color)
            }
        }

        return applyGaussianBlur(bitmap)
    }
    private fun temperatureToColor(temperature: Double, max: Double, min: Double, alpha: Double = 135.0): Int {
        val temperatureRange = max - min
        val normalizedTemperature = (temperature - min) / temperatureRange

        // 根據溫度調整色相值
        val hue = 240.0 * (1.0 - normalizedTemperature) // 可以調整這個公式以改變映射範圍

        val saturation = 1.0 // 飽和度
        val brightness = 1.0 // 亮度

        val color = Color.HSVToColor(floatArrayOf(hue.toFloat(), saturation.toFloat(), brightness.toFloat()))
        return Color.argb(alpha.toInt(), Color.red(color), Color.green(color), Color.blue(color))
    }

//
//    private fun temperatureToColor(temperature: Float, max: Float, min: Float, alpha: Float = 135f): Int {
//        val temperatureRange = max - min
//        val normalizedTemperature = (temperature - min) / temperatureRange
//
//        // 使用更複雜的顏色映射
//        val hue = calculateHue(normalizedTemperature)
//        val saturation = 1f // 飽和度
//        val brightness = 1f // 亮度
//
//        val color = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
//        return Color.argb(alpha.toInt(), Color.red(color), Color.green(color), Color.blue(color))
//    }

    private fun calculateHue(normalizedTemperature: Float): Float {
        // 根據normalizedTemperature計算色相
        // 這裡可以根據需要實現更複雜的顏色映射邏輯
        // 例如，使用彩虹映射，鐵紅映射等
        return 360f * (1f - normalizedTemperature)
    }

    private fun applyGaussianBlur(bitmap: Bitmap): Bitmap {
        val rs = RenderScript.create(this)
        val input = Allocation.createFromBitmap(rs, bitmap)
        val output = Allocation.createTyped(rs, input.type)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

        script.setRadius(1f) // 設置模糊半徑
        script.setInput(input)
        script.forEach(output)
        output.copyTo(bitmap)

        return bitmap
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

//        val temperatureData = Array(1024){39.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 39.0f; 39.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 39.0f; 39.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 39.0f; 39.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 39.0f; 38.0f; 39.0f; 38.0f; 38.0f; 38.0f; 39.0f; 39.0f; 39.0f; 38.0f; 38.0f; 38.0f; 38.0f; 39.0f; 39.0f; 39.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 38.0f; 38.0f; 39.0f; 39.0f; 38.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 38.0f; 38.0f; 39.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 38.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 38.0f; 38.0f; 39.0f; 39.0f; 38.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 39.0f; 38.0f; 38.0f; 39.0f; 39.0f; 38.0f; 39.0f; 38.0f; 38.0f}
//        val temperatureData = Array(1024) { Random.nextDouble(10.0, 60.0) }
        val temperatureData = arrayOf(39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 38.0, 39.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 38.0, 38.0, 38.0, 38.0, 38.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0, 39.0, 39.0, 38.0, 39.0, 38.0, 38.0)
        val thermalBitmap = createThermalBitmap(temperatureData)
        updateOverlay(thermalBitmap)

        viewFinder = findViewById(R.id.previewView)
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // 添加這行代碼
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // 如果權限沒有被授予，結束應用
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}