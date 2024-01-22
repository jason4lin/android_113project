import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

/**
 * BlurBuilder 是一個輔助類別，用於使用 RenderScript 對 Bitmap 應用模糊效果。
 */
object BlurBuilder {
    private const val BITMAP_SCALE = 0.4f // 縮放比例，控制輸入位圖的大小
    private const val BLUR_RADIUS = 7.5f // 模糊半徑，控制模糊程度

    /**
     * 使用 RenderScript 對給定的 Bitmap 應用模糊效果。
     *
     * @param context 應用程式的上下文。
     * @param image 要進行模糊處理的輸入 Bitmap。
     * @param blurRadius 模糊效果的半徑。
     * @return 模糊後的 Bitmap。
     */
    fun blur(context: Context, image: Bitmap, blurRadius: Float): Bitmap {
        // 計算縮放後的位圖寬度和高度
        val width = Math.round(image.width * BITMAP_SCALE)
        val height = Math.round(image.height * BITMAP_SCALE)

        // 創建縮放後的輸入位圖
        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
        // 創建輸出位圖
        val outputBitmap = Bitmap.createBitmap(inputBitmap)

        // 創建 RenderScript 實例
        val rs = RenderScript.create(context)
        // 創建 ScriptIntrinsicBlur 實例
        val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        // 創建輸入和輸出的 RenderScript Allocation
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)

        // 設置模糊半徑
        theIntrinsic.setRadius(blurRadius)
        // 設置輸入位圖
        theIntrinsic.setInput(tmpIn)
        // 執行模糊操作
        theIntrinsic.forEach(tmpOut)
        // 將模糊後的結果複製到輸出位圖
        tmpOut.copyTo(outputBitmap)

        return outputBitmap
    }
}
