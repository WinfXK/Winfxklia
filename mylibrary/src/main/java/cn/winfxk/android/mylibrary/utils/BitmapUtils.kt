/*
* Copyright Notice
* © [2024 - 2026] Winfxk. All rights reserved.
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
* Created Date: 2026/06/04 17:07 */
@file:Suppress("UNUSED")

package cn.winfxk.android.mylibrary.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import cn.winfxk.android.mylibrary.http.download
import cn.winfxk.android.mylibrary.utils.BitmapUtils.decodeSampledBitmapFromFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import java.io.File

object BitmapUtils : Tablabel {
    /**
     * 从给定的文件路径高效地解码出一个经过采样缩放的 Bitmap。
     * 该方法会在 IO 协程中执行，自动计算合适的 `inSampleSize`，有效避免加载大图时产生 OOM（内存溢出）。
     *
     * @param path 本地图片文件的绝对路径
     * @param reqWidth 期望加载的图片宽度
     * @param reqHeight 期望加载的图片高度
     * @return 成功解码返回缩放后的 [Bitmap]，如果文件不存在或解码失败则返回 null
     */
    suspend fun decodeSampledBitmapFromFile(path: String, reqWidth: Int, reqHeight: Int): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            Log.e(tab, "解码文件图片失败: $path", e)
            null
        }
    }

    /**
     * 从给定的文件对象高效地解码出一个经过采样缩放的 Bitmap。
     * 此为 [decodeSampledBitmapFromFile] 的重载方法。
     *
     * @param file 本地图片文件对象
     * @param reqWidth 期望加载的图片宽度
     * @param reqHeight 期望加载的图片高度
     * @return 成功解码返回缩放后的 [Bitmap]，如果文件不存在或解码失败则返回 null
     */
    suspend fun decodeSampledBitmapFromFile(file: File, reqWidth: Int, reqHeight: Int): Bitmap? = decodeSampledBitmapFromFile(file.absolutePath, reqWidth, reqHeight)

    /**
     * 从系统资源 ID 高效地解码出图片，支持自动降采样以节约内存。
     * 此方法会在 IO 协程中执行，不会阻塞主线程。
     *
     * @param res 当前上下文的资源获取类 [Resources] (例如 context.resources)
     * @param resId Drawable 或 Mipmap 资源的 ID
     * @param reqWidth 期望加载的图片宽度
     * @param reqHeight 期望加载的图片高度
     * @return 成功解码返回缩放后的 [Bitmap]，如果资源无效或解码失败则返回 null
     */
    suspend fun decodeSampledBitmapFromResource(res: Resources, @DrawableRes resId: Int, reqWidth: Int, reqHeight: Int): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(res, resId, options)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeResource(res, resId, options)
        } catch (e: Exception) {
            Log.e(tab, "解码资源图片失败: resId=$resId", e)
            null
        }
    }

    /**
     * 从网络 URL 安全、高效地加载并解码图片。
     * 下载与解码全过程都在安全的 IO 线程中完成，采用 OkHttp 进行下载，完成后会自动清理临时缓存文件。
     *
     * @param context 上下文对象，用于获取应用的缓存目录存放临时下载文件
     * @param urlString 目标图片的网络 URL 地址
     * @param reqWidth 期望加载的图片宽度，用于下载后控制解码时的内存占用
     * @param reqHeight 期望加载的图片高度
     * @param client [OkHttpClient] 实例。强烈建议传入全局复用的 OkHttpClient 实例以优化网络连接池性能，若不传则默认创建新实例。
     * @return 下载并解码成功后返回 [Bitmap]，下载失败或 URL 解析错误则返回 null
     */
    suspend fun decodeSampledBitmapFromUrl(
        context: Context,
        urlString: String,
        reqWidth: Int,
        reqHeight: Int,
        client: OkHttpClient = OkHttpClient()
    ): Bitmap? = withContext(Dispatchers.IO) {
        val tempFile = downloadUrlToTempFile(context, urlString, client) ?: return@withContext null
        val bitmap = decodeSampledBitmapFromFile(tempFile.absolutePath, reqWidth, reqHeight)
        tempFile.delete()
        bitmap
    }

    /**
     * 智能计算合适的缩放比例，降低解码时的内存消耗。
     *
     * @param options 已经通过 `inJustDecodeBounds = true` 获取了边缘信息的 BitmapFactory.Options
     * @param reqWidth 期望的输出宽度
     * @param reqHeight 期望的输出高度
     * @return 计算得出的 `inSampleSize` 采样率（2 的幂次方）
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        if (reqWidth <= 0 || reqHeight <= 0) return 1
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * 内部辅助方法：通过 OkHttp 下载指定的 URL 数据到临时文件中。
     */
    private suspend fun downloadUrlToTempFile(context: Context, urlString: String, client: OkHttpClient): File? = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        try {
            tempFile = File.createTempFile("image_dl_", ".tmp", context.cacheDir)
            val httpUrl = urlString.toHttpUrlOrNull()
            if (httpUrl == null) {
                Log.e(tab, "URL格式错误，无法解析: $urlString")
                tempFile.delete()
                return@withContext null
            }
            val success = httpUrl.newBuilder().download(
                client = client,
                targetFile = tempFile
            )
            if (success) return@withContext tempFile
            else Log.e(tab, "网络加载异常: OkHttp 下载失败 for $urlString")
        } catch (e: Exception) {
            Log.e(tab, "下载图片失败: $urlString", e)
        }
        tempFile?.delete()
        null
    }

    /**
     * 将当前的 Bitmap 精确缩放/拉伸到指定的绝对尺寸。
     * 注：此方法不同于降采样，它会对内存中已存在的 Bitmap 对象进行二次渲染。
     *
     * @param source 原始 [Bitmap] 对象
     * @param width 目标宽度
     * @param height 目标高度
     * @param filter 是否对图像进行双线性过滤。设为 true 可获得更平滑的缩放效果，但会稍微消耗性能
     * @return 缩放后的新 [Bitmap]
     */
    fun scaleBitmap(source: Bitmap, width: Int, height: Int, filter: Boolean = true): Bitmap = source.scale(width, height, filter)

    /**
     * 异步挂起方法：高效地从系统资源获取图片并精确缩放到指定尺寸。
     * 此操作综合了内存采样降级与最终的精确缩放，优先保障不发生 OOM。
     *
     * @param context 上下文对象
     * @param resId 资源 ID
     * @param reqWidth 目标期望宽度
     * @param reqHeight 目标期望高度
     * @return 经过采样+缩放处理后的目标 [Bitmap]，失败返回 null
     */
    suspend fun scaleBitmapFromResource(context: Context, @DrawableRes resId: Int, reqWidth: Int, reqHeight: Int): Bitmap? = withContext(Dispatchers.IO) {
        val sampledBitmap = decodeSampledBitmapFromResource(context.resources, resId, reqWidth, reqHeight) ?: return@withContext null
        val scaledBitmap = sampledBitmap.scale(reqWidth, reqHeight)
        if (scaledBitmap != sampledBitmap) sampledBitmap.recycle()
        scaledBitmap
    }

    /**
     * 异步挂起方法：将 Bitmap 数据以指定格式写入到本地文件中。
     *
     * @param bitmap 需要保存的源 [Bitmap]
     * @param file 目标存储的文件对象
     * @param format 图片压缩的格式，如 [Bitmap.CompressFormat.PNG] 或 JPEG 等
     * @param quality 压缩质量 (0-100)，仅对支持有损压缩的格式（如 JPEG）有效，PNG 会忽略此参数。默认为 90。
     * @return 保存成功返回 true，发生异常或失败返回 false
     */
    suspend fun saveBitmapToFile(bitmap: Bitmap, file: File, format: Bitmap.CompressFormat, quality: Int = 90): Boolean = withContext(Dispatchers.IO) {
        try {
            file.outputStream().use { bitmap.compress(format, quality, it) }
            true
        } catch (e: Exception) {
            Log.e(tab, "保存 Bitmap 到文件失败: ${file.absolutePath}", e)
            false
        }
    }

    /**
     * 智能将任意类型的 [Drawable] 提取或渲染转换为 [Bitmap]。
     * 若该 Drawable 本身封装了 Bitmap，则直接提取；
     * 否则将通过 Canvas 构建一个新的 Bitmap，兼容了纯色 (ColorDrawable) 等没有明确固有尺寸的 Drawable。
     *
     * @param drawable 待转换的 [Drawable] 对象
     * @return 转换或渲染出的 [Bitmap]
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 高效生成完美的圆形 Bitmap，常用于头像等圆图展示场景。
     *
     * @param source 原始 [Bitmap]
     * @return 居中裁剪并渲染后的正圆形 [Bitmap]
     */
    fun createCircularBitmap(source: Bitmap): Bitmap {
        val size = source.width.coerceAtMost(source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        val squaredBitmap = if (x == 0 && y == 0) source else Bitmap.createBitmap(source, x, y, size, size)
        val output = createBitmap(size, size)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)
        if (squaredBitmap != source) squaredBitmap.recycle()
        return output
    }

    /**
     * 高效生成带圆角的 Bitmap。
     *
     * @param source 原始 [Bitmap]
     * @param cornerRadius 目标圆角半径 (单位: px)
     * @return 携带圆角效果的全新 [Bitmap]
     */
    fun createRoundedCornerBitmap(source: Bitmap, cornerRadius: Float): Bitmap {
        val output = createBitmap(source.width, source.height)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val rectF = RectF(0f, 0f, source.width.toFloat(), source.height.toFloat())
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
        return output
    }

    /**
     * 为给定的 Bitmap 四周补充额外的透明内边距 (Padding)。
     * 适用于将图片置于更大画布上，从而调整 UI 占位而不会拉伸图片自身的场景。
     *
     * @param source 原始 [Bitmap]
     * @param padding 四周额外增加的透明像素大小 (单位: px)
     * @return 宽度和高度各增加 padding * 2 的带有透明底板的 [Bitmap]
     */
    fun addTransparentPadding(source: Bitmap, padding: Int): Bitmap {
        val newWidth = source.width + padding * 2
        val newHeight = source.height + padding * 2
        val output = createBitmap(newWidth, newHeight)
        Canvas(output).drawBitmap(source, padding.toFloat(), padding.toFloat(), null)
        return output
    }
}