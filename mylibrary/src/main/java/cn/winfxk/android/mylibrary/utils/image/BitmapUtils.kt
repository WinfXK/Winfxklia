/*
* Copyright Notice
* © [2024 - 2025] Winfxk. All rights reserved.
* The software, its source code, and all related documentation are the intellectual property of Winfxk. Any reproduction or distribution of this software or any part thereof must be clearly attributed to Winfxk and the original author. Unauthorized copying, reproduction, or distribution without proper attribution is strictly prohibited.
* For inquiries, support, or to request permission for use, please contact us at:
* Email: admin@winfxk.cn
* QQ: 2508543202
* Visit our homepage for more information: http://Winfxk.cn
*
* --------- Create message ---------
* Created by IntelliJ ID
* Author： Winfxk
* Web: http://winfxk.com
* Created Date: 2025/11/08 16:39 */
package cn.winfxk.android.mylibrary.utils.image


import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.WorkerThread
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

/**
 * 一个用于处理 Bitmap 的强大工具单例对象。
 * 包含高效采样加载、精确缩放、保存、形状变换（圆形、圆角）等功能。
 */
object BitmapUtils {
    private const val TAG = "BitmapUtils"

    /**
     * 从给定的文件路径高效地解码出一个经过缩放的 Bitmap。
     * 此方法会进行等比例缩放，以确保生成的 Bitmap 的较短边不小于请求的尺寸，同时保持原始宽高比。
     *
     * 重要：这是一个耗时操作，请确保在后台线程（如 Coroutine 的 Dispatchers.IO）中调用此方法。
     *
     * @param path 图片文件的绝对路径。
     * @param reqWidth 期望的宽度（单位：像素）。
     * @param reqHeight 期望的高度（单位：像素）。
     * @return 解码成功后的 Bitmap 对象，如果文件不存在或解码失败，则返回 null。
     */
    @WorkerThread
    fun decodeSampledBitmapFromFile(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
    }

    /**
     * @see decodeSampledBitmapFromFile
     */
    @WorkerThread
    fun decodeSampledBitmapFromFile(file: File, reqWidth: Int, reqHeight: Int): Bitmap? = decodeSampledBitmapFromFile(file.absolutePath, reqWidth, reqHeight)

    /**
     * 从给定的资源 ID 高效地解码出一个经过缩放的 Bitmap。
     *
     * @param res Resources 对象 (通常是 context.resources)。
     * @param resId Drawable 资源 ID。
     * @param reqWidth 期望的宽度（单位：像素）。
     * @param reqHeight 期望的高度（单位：像素）。
     * @return 解码成功后的 Bitmap 对象，如果解码失败，则返回 null。
     */
    @WorkerThread
    fun decodeSampledBitmapFromResource(res: Resources, @DrawableRes resId: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(res, resId, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(res, resId, options)
    }

    /**
     * 从网络 URL 高效地解码出一个经过缩放的 Bitmap，此实现无需第三方库。
     *
     * 实现原理：
     * 1. 将网络图片流下载到一个临时的私有缓存文件中。
     * 2. 调用已有的、高效的 decodeSampledBitmapFromFile 方法从临时文件进行采样解码。
     * 3. 无论成功与否，最后都会自动删除该临时文件。
     *
     * @param context Context 对象，用于获取应用的缓存目录。
     * @param urlString 图片的 URL 地址。
     * @param reqWidth 期望的宽度（单位：像素）。
     * @param reqHeight 期望的高度（单位：像素）。
     * @return 解码成功后的 Bitmap 对象，如果网络请求或解码失败，则返回 null。
     */
    @WorkerThread
    fun decodeSampledBitmapFromUrl(context: Context, urlString: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val tempFile = downloadUrlToTempFile(context, urlString) ?: return null
        val bitmap = decodeSampledBitmapFromFile(tempFile.absolutePath, reqWidth, reqHeight)
        tempFile.delete()
        return bitmap
    }

    /**
     * 计算 BitmapFactory.Options.inSampleSize 的值。
     *
     * @param options 包含图片原始尺寸（outWidth, outHeight）的 BitmapFactory.Options 对象。
     * @param reqWidth 期望的宽度（单位：像素）。
     * @param reqHeight 期望的高度（单位：像素）。
     * @return 计算出的整数 inSampleSize 值。
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        if (reqWidth <= 0 || reqHeight <= 0) return 1
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) inSampleSize *= 2
        }
        return inSampleSize
    }

    /**
     * 将网络流下载到临时文件。
     * 优化点：确保在任何失败情况下（IO、网络非200等）都能删除 tempFile，防止垃圾文件。
     * @return 成功则返回临时文件对象，失败则返回 null。
     */
    @Throws(Exception::class)
    private fun downloadUrlToTempFile(context: Context, urlString: String): File? {
        val tempFile: File
        try {
            tempFile = File.createTempFile("image_", ".tmp", context.cacheDir)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to create temp file for image download", e)
            return null
        }
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000 // 15秒连接超时
            connection.readTimeout = 20000  // 20秒读取超时
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                return tempFile
            } else {
                Log.e(TAG, "Download failed: Server responded with code ${connection.responseCode} for $urlString")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download image from $urlString", e)
        } finally {
            connection?.disconnect()
        }
        tempFile.delete()
        return null
    }

    /**
     * 将 Bitmap 精确缩放到指定尺寸。
     *
     * @param source 源 Bitmap。
     * @param width 目标宽度。
     * @param height 目标高度。
     * @param filter 是否使用双线性过滤（true可获更高质量）。
     * @return 缩放后的新 Bitmap。
     * @see Bitmap.createScaledBitmap
     */
    fun scaleBitmap(source: Bitmap, width: Int, height: Int, filter: Boolean = true): Bitmap = source.scale(width, height, filter)

    /**
     * 从资源中加载并精确缩放到指定尺寸。
     * 这是一个高效的实现，它首先采样加载一个接近的尺寸，然后再精确缩放。
     *
     * @param context Context 对象。
     * @param resId Drawable 资源 ID。
     * @param reqWidth 目标宽度。
     * @param reqHeight 目标高度。
     * @return 缩放后的新 Bitmap，如果解码失败则返回 null。
     */
    @WorkerThread
    fun scaleBitmapFromResource(context: Context, @DrawableRes resId: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        val sampledBitmap = decodeSampledBitmapFromResource(context.resources, resId, reqWidth, reqHeight) ?: return null
        val scaledBitmap = sampledBitmap.scale(reqWidth, reqHeight)
        if (scaledBitmap != sampledBitmap) sampledBitmap.recycle()
        return scaledBitmap
    }


    /**
     * 将 Bitmap 保存到文件。
     *
     * @param bitmap 要保存的 Bitmap。
     * @param file 目标文件。
     * @param format 压缩格式 (JPEG, PNG, WEBP)。
     * @param quality 压缩质量 (0-100)。
     * @return true 如果保存成功，false 如果发生错误。
     */
    fun saveBitmapToFile(bitmap: Bitmap, file: File, format: Bitmap.CompressFormat, quality: Int = 90): Boolean {
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            bitmap.compress(format, quality, out)
            return true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save bitmap to ${file.absolutePath}", e)
            return false
        } finally {
            try {
                out?.close()
            } catch (_: IOException) {
            }
        }
    }

    /**
     * 将 Drawable 转换为 Bitmap。
     *
     * @param drawable Drawable 对象。
     * @return 转换后的 Bitmap。
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val bitmap = createBitmap(drawable.intrinsicWidth.coerceAtLeast(1), drawable.intrinsicHeight.coerceAtLeast(1))
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 将 Bitmap 裁剪为圆形。
     *
     * @param source 源 Bitmap。
     * @return 圆形的新 Bitmap。
     */
    fun createCircularBitmap(source: Bitmap): Bitmap {
        val size = source.width.coerceAtMost(source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
        val output = createBitmap(size, size)
        val canvas = Canvas(output)
        val paint = Paint()
        val shader = BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
        paint.isAntiAlias = true
        val r = size / 2f
        canvas.drawCircle(r, r, r, paint)
        if (squaredBitmap != source) squaredBitmap.recycle()
        return output
    }

    /**
     * 为 Bitmap 添加圆角。
     *
     * @param source 源 Bitmap。
     * @param cornerRadius 圆角半径 (px)。
     * @return 带圆角的新 Bitmap。
     */
    fun createRoundedCornerBitmap(source: Bitmap, cornerRadius: Float): Bitmap {
        val output = createBitmap(source.width, source.height)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, source.width, source.height)
        val rectF = RectF(rect)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, rect, rect, paint)
        return output
    }

    /**
     * 为 Bitmap 添加透明边距（内边距）。
     * 创建一个比源 Bitmap 大的新 Bitmap，并将源 Bitmap 绘制在中心，
     * 从而在四周形成指定大小的透明区域。
     *
     * @param source 源 Bitmap。
     * @param padding 要在四周添加的透明边距（单位：像素）。
     * @return 带有透明边距的新 Bitmap。
     */
    fun addTransparentPadding(source: Bitmap, padding: Int): Bitmap {
        val newWidth = source.width + padding * 2
        val newHeight = source.height + padding * 2
        val output = createBitmap(newWidth, newHeight)
        val canvas = Canvas(output)
        val left = padding.toFloat()
        val top = padding.toFloat()
        canvas.drawBitmap(source, left, top, null)
        return output
    }
}