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
* Created Date: 2025/11/05 15:43
*/
package cn.winfxk.android.mylibrary.http

import android.content.Context
import android.os.Handler
import android.util.Log
import cn.winfxk.android.mylibrary.utils.tab.Tabable
import com.winfxk.lib.config.Config
import com.winfxk.lib.utils.className
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer
import java.io.File
import java.io.IOException
import kotlin.collections.iterator


typealias OnProgress = ((Long, Long) -> Unit);
typealias OnError = ((Exception) -> Unit);

/**
 * Http/s 工具类 (支持持久化 Cookie 和进度回调)
 *
 * @author Winfxk
 */
class Http(val context: Context) : Tabable {
    private val client: OkHttpClient
    private val config by lazy { Config(File(context.filesDir, "Cookies.json")) }
    override val tag: String by lazy { className }

    /**
     * 增加一个 Handler，用于将 IO 线程的回调抛到主线程
     */
    private val mainHandler by lazy { Handler(context.mainLooper) }

    companion object {
        private const val COOKIE_PREFIX = "http_cookie_"
        private const val DEFAULT_BUFFER_SIZE = 4096
    }

    /**
     * CookieJar 的实现，用于持久化 Cookie 到 Config 文件
     */
    private class ConfigCookieJar(private val config: Config) : CookieJar {

        /**
         * 从服务器响应中保存 Cookie
         */
        @Synchronized
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val key = COOKIE_PREFIX + url.host
            val currentCookieStrings = config.getAs<List<String>>(key) ?: emptyList()
            val now = System.currentTimeMillis()
            val currentCookies = currentCookieStrings
                .mapNotNull { Cookie.parse(url, it) }.filter { it.expiresAt > now }
                .associateBy { "${it.name};${it.domain};${it.path}" }.toMutableMap()
            for (newCookie in cookies) {
                if (! newCookie.persistent) continue
                val cookieKey = "${newCookie.name};${newCookie.domain};${newCookie.path}"
                currentCookies[cookieKey] = newCookie
            }
            val newCookieStrings = currentCookies.values.map { it.toString() }
            config[key] = newCookieStrings
            config.save()
        }

        /**
         * 为请求加载 Cookie
         */
        @Synchronized
        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val key = COOKIE_PREFIX + url.host
            val cookieStrings = config.getAs<List<String>>(key) ?: emptyList()
            if (cookieStrings.isEmpty()) return emptyList()
            val now = System.currentTimeMillis()
            val validCookies = mutableListOf<Cookie>()
            val newCookieStrings = mutableListOf<String>()
            var modified = false
            for (cookieString in cookieStrings) {
                val cookie = Cookie.parse(url, cookieString)
                if (cookie == null) {
                    modified = true
                    continue
                }
                if (cookie.expiresAt < now) {
                    modified = true
                    continue
                }
                validCookies.add(cookie)
                newCookieStrings.add(cookieString)
            }

            if (modified) {
                config[key] = newCookieStrings
                config.save()
            }
            return validCookies.filter { it.matches(url) }
        }
    }

    init {
        val cookieJar = ConfigCookieJar(config)
        client = OkHttpClient.Builder().cookieJar(cookieJar).followRedirects(true).build()
    }

    /**
     * 手动清除所有已保存的 Cookie
     */
    @Synchronized
    fun clearAllCookies() {
        val config = this.config
        val keysToRemove = config.getAll().keys.filter { it.startsWith(COOKIE_PREFIX) }
        keysToRemove.forEach { config.remove(it) }
        config.save()
    }

    /**
     * 发起通用 HTTP 请求 (GET/POST)
     *
     * @param url 请求的 URL
     * @param method 请求方法 ("GET" 或 "POST", 默认 "GET", 不区分大小写)
     * @param params 请求参数 (Map<String, Any>?).
     * - GET: 将被编码为 URL 查询参数.
     * - POST: 将被编码为 application/x-www-form-urlencoded 表单.
     */
    fun get(url: String, method: String = "GET", params: Map<String, Any>? = null): String? {
        val requestBuilder = Request.Builder()
        val httpUrl = url.toHttpUrlOrNull()
        if (httpUrl == null) {
            Log.i(tag, "HttpUtils Request Error: Invalid URL=$url")
            return null
        }
        val urlBuilder = httpUrl.newBuilder()
        if (method.equals("GET", ignoreCase = true) && params != null)
            for ((key, value) in params) urlBuilder.addQueryParameter(key, value.toString())
        requestBuilder.url(urlBuilder.build())
        when (method.uppercase()) {
            "GET"  -> requestBuilder.get()
            "POST" -> {
                val formBodyBuilder = FormBody.Builder()
                if (params != null) for ((key, value) in params) formBodyBuilder.add(key, value.toString())
                requestBuilder.post(formBodyBuilder.build())
            }
            else   -> {
                Log.i(tag, "HttpUtils Request Error: Unsupported method '$method'")
                return null
            }
        }
        val request = requestBuilder.build()
        client.newCall(request).execute().use { response ->
            if (! response.isSuccessful) {
                Log.i(tag, "HttpUtils Request Error: Code=${response.code} URL=$url")
                return null
            }
            return response.body?.string()
        }
    }

    /**
     *  功能2：根据 URL 下载文件到指定的 File
     *
     * @param url 下载地址
     * @param targetFile 目标文件（将写入此文件）
     * @param onProgress 进度监听 (当前大小, 总大小)
     * @param onError 异常监听
     * @param runOnMainThread (推荐) 是否在主线程执行 onProgress 回调，以便安全更新 UI
     * @return 下载成功返回 true，失败（网络错误、IO 错误）返回 false。
     */
    fun download(
        url: String,
        targetFile: File,
        onProgress: OnProgress? = null,
        onError: OnError? = null,
        runOnMainThread: Boolean = true
    ): Boolean {
        val request = Request.Builder().url(url).get().build()
        try {
            client.newCall(request).execute().use { response ->
                if (! response.isSuccessful) {
                    val e = IOException("HttpUtils Download Error: Code=${response.code} URL=$url")
                    Log.i(tag, e.message ?: "Unknown HTTP Error")
                    onError?.invoke(e)
                    return false
                }
                val body = response.body ?: throw IOException("Response body is null")
                val totalSize = body.contentLength()
                var currentProgress = 0L
                body.byteStream().use { iis ->
                    targetFile.outputStream().use { oos ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytesRead: Int
                        while (iis.read(buffer).also { bytesRead = it } != - 1) {
                            oos.write(buffer, 0, bytesRead)
                            currentProgress += bytesRead
                            if (onProgress != null) {
                                if (runOnMainThread) mainHandler.post { onProgress.invoke(currentProgress, totalSize) }
                                else onProgress.invoke(currentProgress, totalSize)
                            }
                        }
                        oos.flush()
                    }
                }
                return true
            }
        } catch (e: Exception) {
            Log.i(tag, "HttpUtils Download Exception: ${e.message} URL=$url")
            e.printStackTrace()
            onError?.invoke(e)
            return false
        }
    }

    /**
     *   功能3：上传文件到指定的 URL
     *
     * @param url 上传地址
     * @param file 要上传的文件
     * @param paramName 文件在表单中的参数名（例如 "file", "upload"），默认为 "file"
     * @param onProgress 进度监听 (当前大小, 总大小)
     * @param onError 异常监听
     * @param runOnMainThread (推荐) 是否在主线程执行 onProgress 回调，以便安全更新 UI
     * @return 成功时返回服务器的响应体（String），失败或发生 IO 异常时返回 null。
     */
    fun upload(
        url: String,
        file: File,
        paramName: String = "file",
        onProgress: OnProgress? = null,
        onError: OnError? = null,
        runOnMainThread: Boolean = true
    ): String? {
        try {
            val fileBody = file.asRequestBody("application/octet-stream".toMediaType())
            val requestBodyWithProgress = if (onProgress != null) CountingRequestBody(fileBody, onProgress, runOnMainThread, mainHandler) else fileBody
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(paramName, file.name, requestBodyWithProgress).build()
            val request = Request.Builder().url(url).post(requestBody).build()
            client.newCall(request).execute().use { response ->
                if (! response.isSuccessful) {
                    val e = IOException("HttpUtils Upload Error: Code=${response.code} URL=$url")
                    Log.i(tag, e.message ?: "Unknown HTTP Error")
                    onError?.invoke(e)
                    return null
                }
                return response.body?.string()
            }
        } catch (e: Exception) {
            Log.i(tag, "HttpUtils Upload Exception: ${e.message} URL=$url")
            e.printStackTrace()
            onError?.invoke(e)
            return null
        }
    }

    /**
     * 内部辅助类：一个自定义的 RequestBody，用于包装原始请求体并计算上传进度。
     */
    private class CountingRequestBody(
        private val delegate: RequestBody,
        private val onProgress: (Long, Long) -> Unit,
        private val runOnMainThread: Boolean,
        private val mainHandler: Handler
    ) : RequestBody() {
        override fun contentType(): MediaType? = delegate.contentType()
        override fun contentLength(): Long = try {
            delegate.contentLength()
        } catch (_: IOException) {
            - 1
        }

        override fun writeTo(sink: BufferedSink) {
            val totalSize = contentLength()
            var currentProgress = 0L
            val countingSink = object : ForwardingSink(sink) {
                override fun write(source: Buffer, byteCount: Long) {
                    super.write(source, byteCount)
                    currentProgress += byteCount
                    if (runOnMainThread) mainHandler.post { onProgress.invoke(currentProgress, totalSize) }
                    else onProgress.invoke(currentProgress, totalSize)
                }
            }
            val bufferedSink = countingSink.buffer()
            delegate.writeTo(bufferedSink)
            bufferedSink.flush()
        }
    }
}