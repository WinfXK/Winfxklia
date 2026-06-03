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
 * Created by IntelliJ IDEA
 * Author： Winfxk
 * Web: http://winfxk.com
 * Created Date: 2026/06/02 15:23
 */

package cn.winfxk.android.mylibrary.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.util.Log
import android.util.LruCache
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withRotation
import cn.winfxk.android.mylibrary.utils.Tablabel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class ImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), Tablabel {
    private val viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var currentJob: Job? = null
    private var currentUrl: String? = null
    private var isLoading = false
    private var isError = false
    private var rotationAngle = 0f
    private var viewCenterX = 0f
    private var viewCenterY = 0f
    private var loadingRadius = 0f
    private var errorCrossSize = 0f
    private val loadingRect = RectF()
    private val loadingPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
        }
    }

    private val errorPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
            color = "#F44336".toColorInt()
        }
    }

    private val errorBgPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = "#1AF44336".toColorInt()
        }
    }

    private val loadingAnimator by lazy {
        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 1200
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                rotationAngle = it.animatedValue as Float
                invalidate()
            }
        }
    }

    companion object {
        private val memoryCache: LruCache<String, Bitmap>
        private const val DISK_CACHE_DIR_NAME = "winfxk_image_cache"

        init {
            val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
            val cacheSize = maxMemory / 8
            memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
                override fun sizeOf(key: String, bitmap: Bitmap): Int {
                    return bitmap.byteCount / 1024
                }
            }
        }

        private fun addBitmapToMemory(key: String, bitmap: Bitmap) {
            if (memoryCache.get(key) == null) {
                synchronized(memoryCache) {
                    memoryCache.put(key, bitmap)
                }
            }
        }

        private fun String.toMD5(): String {
            val md = MessageDigest.getInstance("MD5")
            val digested = md.digest(toByteArray())
            return digested.joinToString("") { "%02x".format(it) }
        }
    }

    /**
     * 智能从 URL 加载图片，完全由内部接管状态绘制
     *
     * @param url 图片的完整 URL
     * @param smoothRendering 是否平滑渲染(抗锯齿)。设为 false 适用于像素画(如我的世界贴图)
     * @param useFadeIn 是否使用渐显动画过渡
     */
    @Suppress("UNUSED")
    fun setImageUrl(
        url: String,
        smoothRendering: Boolean = true,
        useFadeIn: Boolean = true
    ) {
        if (url.isBlank()) {
            handleError()
            return
        }
        currentUrl = url
        currentJob?.cancel()
        val cachedBitmap = memoryCache.get(url)
        if (cachedBitmap != null) {
            applyBitmap(cachedBitmap, smoothRendering, false)
            return
        }
        startLoadingUI()
        currentJob = viewScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                val diskFile = getDiskCacheFile(url)
                if (diskFile.exists() && diskFile.length() > 0) {
                    val diskBitmap = decodeSampledBitmap(diskFile, getTargetWidth(), getTargetHeight())
                    if (diskBitmap != null) {
                        addBitmapToMemory(url, diskBitmap)
                        return@withContext diskBitmap
                    }
                }
                downloadToDiskAndDecode(url, diskFile)
            }
            if (currentUrl == url) {
                if (bitmap != null) applyBitmap(bitmap, smoothRendering, useFadeIn)
                else handleError()
            }
        }
    }

    /**
     * 从本地文件智能加载图片
     */
    @Suppress("UNUSED")
    fun setImageFile(
        file: File,
        smoothRendering: Boolean = true,
        useFadeIn: Boolean = true
    ) {
        if (! file.exists()) {
            handleError()
            return
        }
        currentJob?.cancel()
        startLoadingUI()
        currentJob = viewScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                decodeSampledBitmap(file, getTargetWidth(), getTargetHeight())
            }
            if (bitmap != null) applyBitmap(bitmap, smoothRendering, useFadeIn) else handleError()
        }
    }

    private fun downloadToDiskAndDecode(urlString: String, diskFile: File): Bitmap? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { input ->
                    FileOutputStream(diskFile).use { input.copyTo(it) }
                }
                val bitmap = decodeSampledBitmap(diskFile, getTargetWidth(), getTargetHeight())
                if (bitmap != null) addBitmapToMemory(urlString, bitmap) else diskFile.delete()
                bitmap
            } else null
        } catch (e: Exception) {
            Log.e(tab, "网络图片下载失败: $urlString", e)
            if (diskFile.exists()) diskFile.delete()
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun decodeSampledBitmap(file: File, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (! file.exists()) return null
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            Log.e(tab, "图片解码失败！文件：${file.absolutePath}", e)
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) inSampleSize *= 2
        }
        return inSampleSize
    }

    private fun applyBitmap(bitmap: Bitmap, smoothRendering: Boolean, fadeIn: Boolean) {
        stopLoadingUI()
        isError = false
        if (smoothRendering) setImageBitmap(bitmap)
        else {
            val drawable = bitmap.toDrawable(resources)
            drawable.isFilterBitmap = false
            setImageDrawable(drawable)
        }
        if (fadeIn) {
            alpha = 0f
            animate().alpha(1f).setDuration(300).start()
        } else alpha = 1f
    }

    private fun getDiskCacheFile(url: String): File {
        val cacheDir = File(context.cacheDir, DISK_CACHE_DIR_NAME)
        if (! cacheDir.exists()) cacheDir.mkdirs()
        return File(cacheDir, url.toMD5())
    }

    private fun getTargetWidth(): Int = (width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels) / 2
    private fun getTargetHeight(): Int = (height.takeIf { it > 0 } ?: resources.displayMetrics.heightPixels) / 2

    private fun startLoadingUI() {
        isError = false
        setImageDrawable(null)
        isLoading = true
        loadingAnimator.start()
        invalidate()
    }

    private fun stopLoadingUI() {
        isLoading = false
        loadingAnimator.cancel()
        invalidate()
    }

    private fun handleError() {
        stopLoadingUI()
        isError = true
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return
        viewCenterX = w / 2f
        viewCenterY = h / 2f
        loadingRadius = (w.coerceAtMost(h)) / 6f
        errorCrossSize = loadingRadius / 2.5f
        loadingRect.set(
            viewCenterX - loadingRadius,
            viewCenterY - loadingRadius,
            viewCenterX + loadingRadius,
            viewCenterY + loadingRadius
        )
        loadingPaint.shader = SweepGradient(
            viewCenterX,
            viewCenterY,
            intArrayOf(Color.TRANSPARENT, "#4CAF50".toColorInt()),
            null
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isLoading) canvas.withRotation(rotationAngle, viewCenterX, viewCenterY) {
            drawArc(loadingRect, 0f, 300f, false, loadingPaint)
        } else if (isError) {
            canvas.drawCircle(viewCenterX, viewCenterY, loadingRadius, errorBgPaint)
            canvas.drawLine(viewCenterX - errorCrossSize, viewCenterY - errorCrossSize, viewCenterX + errorCrossSize, viewCenterY + errorCrossSize, errorPaint)
            canvas.drawLine(viewCenterX + errorCrossSize, viewCenterY - errorCrossSize, viewCenterX - errorCrossSize, viewCenterY + errorCrossSize, errorPaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        currentJob?.cancel()
        loadingAnimator.cancel()
    }
}