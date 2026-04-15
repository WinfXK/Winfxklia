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
* Created Date: 2026/04/15 16:52
*/

package cn.winfxk.android.mylibrary.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.LruCache
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toDrawable
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.utils.tab.Tabable
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

@Suppress("unused")
@SuppressLint("InflateParams")
class ImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), Tabable {
    override val tag: String by lazy { "MyImageView" }
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private val networkExecutor = Executors.newSingleThreadExecutor()
    private var currentUrl: String? = null

    companion object {
        private val imageCache: LruCache<String, Bitmap>

        init {
            val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
            val cacheSize = maxMemory / 8
            imageCache = object : LruCache<String, Bitmap>(cacheSize) {
                override fun sizeOf(key: String, bitmap: Bitmap): Int {
                    return bitmap.byteCount / 1024
                }
            }
        }

        private fun addBitmapToCache(key: String, bitmap: Bitmap) {
            if (getBitmapFromCache(key) == null)
                synchronized(imageCache) {
                    imageCache.put(key, bitmap)
                }
        }

        /**
         * 从缓存中获取 Bitmap
         */
        private fun getBitmapFromCache(key: String): Bitmap? = imageCache.get(key)
    }

    /**
     * 公共方法：从 URL 设置图片
     *
     * @param url 图片的完整 URL 字符串
     * @param placeholderResId 加载时显示的占位图资源 ID
     * @param smoothRendering 是否平滑渲染（过渡模糊）。设为 false 则适用于像素画风格（如我的世界贴图）
     */
    fun setImageUrl(
        url: String,
        placeholderResId: Int? = R.drawable.winfxklia_imageloading,
        smoothRendering: Boolean = true
    ) {
        currentUrl = url
        placeholderResId?.also { setImageResource(it) }
        val cachedBitmap = getBitmapFromCache(url)
        if (cachedBitmap != null) {
            applyBitmap(cachedBitmap, smoothRendering)
            return
        }
        networkExecutor.submit {
            try {
                val bitmap = downloadBitmap(url)
                if (bitmap != null) {
                    addBitmapToCache(url, bitmap)
                    mainThreadHandler.post {
                        if (currentUrl == url) applyBitmap(bitmap, smoothRendering)
                    }
                } else handleError(url)
            } catch (e: Exception) {
                Log.e(tag, "Error downloading image: $url", e)
                handleError(url)
            }
        }
    }

    private fun downloadBitmap(urlString: String): Bitmap? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000 // 10秒超时
            connection.readTimeout = 10000
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                BitmapFactory.decodeStream(connection.inputStream)
            } else null
        } catch (e: Exception) {
            Log.e(tag, "downloadBitmap failed", e)
            null
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * 从本地文件设置图片
     *
     * @param file 目标图片文件
     * @param placeholderResId 占位图
     * @param smoothRendering 是否平滑渲染（过渡模糊）。默认 true (正常显示)；设为 false 关闭抗锯齿，实现我的世界像素锐利风格
     */
    fun setImageFile(
        file: File,
        placeholderResId: Int? = R.drawable.winfxklia_imageloading,
        smoothRendering: Boolean = true
    ) {
        if (! file.exists()) return
        val reqWidth = (width.takeIf { it > 0 } ?: 200) * 2
        val reqHeight = (height.takeIf { it > 0 } ?: 200) * 2
        try {
            val bitmap = decodeSampledBitmap(file, reqWidth, reqHeight)
            if (bitmap != null) applyBitmap(bitmap, smoothRendering)
            else if (placeholderResId != null) setImageResource(placeholderResId)
            else setImageResource(R.drawable.winfxklia_empty)
        } catch (e: Exception) {
            Log.e(tag, "从文件${file}设置图标失败！", e)
            if (placeholderResId != null) setImageResource(placeholderResId)
            else setImageResource(R.drawable.winfxklia_empty)
        }
    }

    private fun applyBitmap(bitmap: Bitmap, smoothRendering: Boolean) {
        if (smoothRendering) setImageBitmap(bitmap)
        else {
            val drawable = bitmap.toDrawable(resources)
            drawable.isFilterBitmap = false
            setImageDrawable(drawable)
        }
    }

    private fun decodeSampledBitmap(file: File, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (! file.exists()) return null
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(file.absolutePath, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (
                halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth
            ) inSampleSize *= 2
        }
        return inSampleSize
    }

    private fun handleError(url: String) {
        mainThreadHandler.post {
            if (currentUrl == url) setImageResource(R.drawable.winfxklia_imageloading_error)
        }
    }
}