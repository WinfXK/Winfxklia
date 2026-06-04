package cn.winfxk.android.mylibrary.http.cookie

import okhttp3.CookieJar

interface ICookieStore : CookieJar {
    fun clearAllCookies()
}