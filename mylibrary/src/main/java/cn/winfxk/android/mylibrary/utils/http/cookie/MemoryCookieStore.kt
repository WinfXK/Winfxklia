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
* Created Date: 2026/6/4  08:48 */
package cn.winfxk.android.mylibrary.utils.http.cookie

import okhttp3.Cookie
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * 内存缓存 CookieStore
 * 随应用生命周期销毁而销毁
 */
@Suppress("UNUSED")
class MemoryCookieStore : ICookieStore {
    private val cache = ConcurrentHashMap<String, MutableMap<String, Cookie>>()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val hostCookies = cache[host] ?: mutableMapOf()
        cookies.forEach { hostCookies["${it.name};${it.domain};${it.path}"] = it }
        cache[host] = hostCookies
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val hostCookies = cache[host] ?: return emptyList()
        val now = System.currentTimeMillis()
        val validCookies = hostCookies.values.filter { it.expiresAt > now }
        if (validCookies.size != hostCookies.size) cache[host] = validCookies.associateBy { "${it.name};${it.domain};${it.path}" }.toMutableMap()
        return validCookies.filter { it.matches(url) }
    }

    @Synchronized
    override fun clearAllCookies() {
        cache.clear()
    }
}