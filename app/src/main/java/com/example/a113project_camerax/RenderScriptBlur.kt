import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

/**
 * RenderScriptBlur 是一個輔助類別，使用 RenderScript 對 Bitmap 應用模糊效果。
 */
object RenderScriptBlur {

    /**
     * 使用 RenderScript 對給定的 Bitmap 應用模糊效果。
     *
     * @param context 應用程式的上下文。
     * @param bitmap 要進行模糊處理的輸入 Bitmap。
     * @param radius 模糊效果的半徑。
     * @return 模糊後的 Bitmap。
     */
    fun blur(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
        // 創建縮放後的輸入位圖，這裡直接使用原始大小
        val inputBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, false)
        // 創建輸出位圖
        val outputBitmap = Bitmap.createBitmap(inputBitmap)

        // 創建 RenderScript 實例
        val rs = RenderScript.create(context)
        // 創建 ScriptIntrinsicBlur 實例
        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        // 創建輸入和輸出的 RenderScript Allocation
        val input = Allocation.createFromBitmap(rs, inputBitmap)
        val output = Allocation.createFromBitmap(rs, outputBitmap)

        // 設置模糊半徑
        blurScript.setRadius(radius)
        // 設置輸入位圖
        blurScript.setInput(input)
        // 執行模糊操作
        blurScript.forEach(output)

        // 將模糊後的結果複製到輸出位圖
        output.copyTo(outputBitmap)
        // 釋放 RenderScript 資源
        rs.destroy()

        return outputBitmap
    }
}
