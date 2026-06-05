package cn.winfxk.android.mylibrary.utils.http.cookie

import okhttp3.CookieJar

interface ICookieStore : CookieJar {
    fun clearAllCookies()
}