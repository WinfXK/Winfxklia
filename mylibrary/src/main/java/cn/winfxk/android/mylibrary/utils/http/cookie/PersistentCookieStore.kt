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
* Created PCUser: Winfx 
* Web: http://winfxk.com
* Created Date: 2026/6/4  08:47 */
package cn.winfxk.android.mylibrary.utils.http.cookie

import android.content.Context
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import okhttp3.Cookie
import okhttp3.HttpUrl
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * 持久化缓存 CookieStore
 * 将 Cookie 存储在 /data/data/包名/files/HttpCookies.json 中
 */
@Suppress("UNUSED")
class PersistentCookieStore(context: Context) : ICookieStore {
    private val file = File(context.filesDir, "HttpCookies.json")
    private val cache = ConcurrentHashMap<String, MutableMap<String, Cookie>>()

    init {
        loadFromDisk()
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val hostCookies = cache[host] ?: mutableMapOf()
        var modified = false
        cookies.forEach { cookie ->
            if (cookie.persistent) {
                val key = "${cookie.name};${cookie.domain};${cookie.path}"
                hostCookies[key] = cookie
                modified = true
            }
        }
        if (modified) {
            cache[host] = hostCookies
            saveToDisk()
        }
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val hostCookies = cache[host] ?: return emptyList()
        val now = System.currentTimeMillis()
        var modified = false
        val validCookies = mutableListOf<Cookie>()
        val iterator = hostCookies.values.iterator()
        while (iterator.hasNext()) {
            val cookie = iterator.next()
            if (cookie.expiresAt <= now) {
                iterator.remove()
                modified = true
            } else validCookies.add(cookie)
        }
        if (modified) saveToDisk()
        return validCookies.filter { it.matches(url) }
    }

    @Synchronized
    override fun clearAllCookies() {
        cache.clear()
        if (file.exists()) file.delete()
    }

    /**
     * 将缓存序列化并异步保存至本地文件
     */
    private fun saveToDisk() {
        try {
            val jsonObject = JSONObject()
            cache.forEach { (host, map) ->
                val list = map.values.map { it.toString() }
                jsonObject[host] = list
            }
            file.writeText(jsonObject.toJSONString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 从本地文件加载所有 Cookie 到内存中
     */
    private fun loadFromDisk() {
        try {
            if (! file.exists()) return
            val jsonText = file.readText()
            if (jsonText.isBlank()) return
            val jsonObject = JSON.parseObject(jsonText)
            jsonObject.keys.forEach { host ->
                val stringList = jsonObject.getJSONArray(host)?.toJavaList(String::class.java) ?: emptyList()
                val cookieMap = mutableMapOf<String, Cookie>()
                val dummyUrl = HttpUrl.Builder().scheme("http").host(host).build()
                stringList.forEach { cookieStr ->
                    Cookie.parse(dummyUrl, cookieStr)?.let { cookie ->
                        val key = "${cookie.name};${cookie.domain};${cookie.path}"
                        cookieMap[key] = cookie
                    }
                }
                if (cookieMap.isNotEmpty()) cache[host] = cookieMap
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}