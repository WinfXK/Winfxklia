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
* Created Date: 2026/06/03 11:23
*/
@file:Suppress("UNUSED")
package cn.winfxk.android.mylibrary.http

import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException

/**
 * URL路径安全拼接工具函数
 */
private fun HttpUrl.Builder.appendPath(path: String?): HttpUrl.Builder {
    if (path.isNullOrBlank()) return this
    path.trim('/').split("/").forEach { if (it.isNotEmpty()) addPathSegment(it) }
    return this
}

/**
 * 发起 Get 同步请求
 * @param client OkHttpClient 实例
 * @param path 可选，追加的路径例如 "/api/user/info"
 * @param params Get请求的 Query 参数
 * @param headers 附加的请求头
 */
suspend fun HttpUrl.Builder.get(
    client: OkHttpClient,
    path: String? = null,
    params: Map<String, String>? = null,
    headers: Map<String, String>? = null
): Response = withContext(Dispatchers.IO) {
    this@get.appendPath(path)
    params?.forEach { (k, v) -> addQueryParameter(k, v) }
    val requestBuilder = Request.Builder().url(this@get.build()).get()
    headers?.forEach { (k, v) -> requestBuilder.addHeader(k, v) }
    client.newCall(requestBuilder.build()).execute()
}

/**
 * 发起 Post 同步请求
 * 若未传入 body 但传入了 params，会自动构建 application/x-www-form-urlencoded 的 FormBody。
 */
suspend fun HttpUrl.Builder.post(
    client: OkHttpClient,
    path: String? = null,
    body: RequestBody? = null,
    params: Map<String, String>? = null,
    headers: Map<String, String>? = null
): Response = withContext(Dispatchers.IO) {
    this@post.appendPath(path)
    val finalBody = body ?: FormBody.Builder().apply {
        params?.forEach { (k, v) -> add(k, v) }
    }.build()
    val requestBuilder = Request.Builder().url(this@post.build()).post(finalBody)
    headers?.forEach { (k, v) -> requestBuilder.addHeader(k, v) }
    client.newCall(requestBuilder.build()).execute()
}

/**
 * 文件下载请求
 * @param targetFile 下载写入的目标文件
 * @param onProgress 进度回调：now(当前大小), max(总大小)
 * @param onOver 结束回调：包含是否成功、对应的文件以及可能的异常信息
 */
suspend fun HttpUrl.Builder.download(
    client: OkHttpClient,
    targetFile: File,
    path: String? = null,
    headers: Map<String, String>? = null,
    onProgress: suspend (now: Long, max: Long) -> Unit = { _, _ -> },
    onOver: suspend (success: Boolean, file: File, e: Exception?) -> Unit = { _, _, _ -> }
): Boolean = withContext(Dispatchers.IO) {
    this@download.appendPath(path)
    val requestBuilder = Request.Builder().url(this@download.build()).get()
    headers?.forEach { (k, v) -> requestBuilder.addHeader(k, v) }
    try {
        val response = client.newCall(requestBuilder.build()).execute()
        if (! response.isSuccessful) {
            onOver(false, targetFile, IOException("Http Request Failed: Code=${response.code}"))
            return@withContext false
        }
        val body = response.body
        val max = body.contentLength()
        var now = 0L
        body.byteStream().use { input ->
            targetFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != - 1) {
                    output.write(buffer, 0, bytesRead)
                    now += bytesRead
                    onProgress(now, max) // Suspend 挂起调用，安全且自由
                }
                output.flush()
            }
        }
        onOver(true, targetFile, null)
        true
    } catch (e: Exception) {
        onOver(false, targetFile, e)
        false
    }
}

/**
 * 单/多文件上传请求
 * @param files 待上传的文件列表 [UploadFile]
 * @param params 表单携带的其他文本参数
 */
suspend fun HttpUrl.Builder.upload(
    client: OkHttpClient,
    files: List<UploadFile>,
    path: String? = null,
    params: Map<String, String>? = null,
    headers: Map<String, String>? = null,
    onProgress: suspend (now: Long, max: Long) -> Unit = { _, _ -> },
    onOver: suspend (success: Boolean, response: Response?, e: Exception?) -> Unit = { _, _, _ -> }
): Response? = withContext(Dispatchers.IO) {
    this@upload.appendPath(path)
    val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
    params?.forEach { (k, v) -> multipartBuilder.addFormDataPart(k, v) }
    files.forEach { uploadFile ->
        val fileBody = uploadFile.file.asRequestBody(uploadFile.mediaType)
        multipartBuilder.addFormDataPart(uploadFile.paramName, uploadFile.fileName, fileBody)
    }
    val requestBody = ProgressRequestBody(multipartBuilder.build(), onProgress)
    val requestBuilder = Request.Builder().url(this@upload.build()).post(requestBody)
    headers?.forEach { (k, v) -> requestBuilder.addHeader(k, v) }
    try {
        val response = client.newCall(requestBuilder.build()).execute()
        if (response.isSuccessful) {
            onOver(true, response, null)
            response
        } else {
            onOver(false, response, IOException("Http Upload Failed: Code=${response.code}"))
            null
        }
    } catch (e: Exception) {
        onOver(false, null, e)
        null
    }
}


/**
 * 协程安全：将 Response 提取为文本
 */
suspend fun Response.asString(): String? = withContext(Dispatchers.IO) {
    use { if (isSuccessful) it.body.string() else null }
}

/**
 * 协程安全：将 Response 提取并解析为JSONObject
 */
suspend fun Response.asJSONObject(): JSONObject? = withContext(Dispatchers.IO) {
    use {
        if (! isSuccessful) return@withContext null
        val str = it.body.string()
        if (str.isBlank()) null else JSONObject.parseObject(str)
    }
}

/**
 * 协程安全：将 Response 提取并解析为JSONArray
 */
suspend fun Response.asJSONArray(): JSONArray? = withContext(Dispatchers.IO) {
    use {
        if (! isSuccessful) return@withContext null
        val str = it.body.string()
        if (str.isBlank()) null else JSONArray.parseArray(str)
    }
}