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
* Created by IntelliJ IDEA
* Author： Winfxk
* Web: http://winfxk.com
* Created Date: 2025/11/04 15:44
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
import cn.winfxk.android.mylibrary.R
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
@Suppress("unused")
@SuppressLint("InflateParams")
/**
 * 支持从 URL 加载图片的 ImageView (无第三方库基础实现)
 *
 * 包含一个简单的内存缓存 (LruCache)。
 * 不包含磁盘缓存或高级错误处理。
 */
class ImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

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
     */
    fun setImageUrl(url: String, placeholderResId: Int? = R.drawable.winfxklia_imageloading) {
        currentUrl = url
        placeholderResId?.also { setImageResource(it) }
        val cachedBitmap = getBitmapFromCache(url)
        if (cachedBitmap != null) {
            setImageBitmap(cachedBitmap)
            return
        }
        networkExecutor.submit {
            try {
                val bitmap = downloadBitmap(url)
                if (bitmap != null) {
                    addBitmapToCache(url, bitmap)
                    mainThreadHandler.post { if (currentUrl == url) setImageBitmap(bitmap) }
                } else handleError(url)
            } catch (e: Exception) {
                Log.e("UrlImageView", "Error downloading image: $url", e)
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
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("UrlImageView", "downloadBitmap failed", e)
            null
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * (私有) 处理下载错误
     */
    private fun handleError(url: String) {
        mainThreadHandler.post { if (currentUrl == url) setImageResource(R.drawable.winfxklia_imageloading_error) }
    }
}
