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
* Created Date: 2026/1/19  10:20 */
package cn.winfxk.android.mylibrary.http.tool

import android.content.Context
import android.util.Log
import com.winfxk.lib.utils.className
import com.winfxk.lib.utils.tag
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class MyCallback<T : Context>(
    private val context: T,
    private val error: (T.(MyCallbackFailure) -> Unit)? = null,
    private val success: T.(MyCallbackResponse) -> Unit) : Callback {
    val tag by lazy { context.tag + "-" + className }

    override fun onFailure(call: Call, e: IOException) {
        Log.e(tag, "网络请求失败！", e)
        error?.invoke(context, MyCallbackFailure(call, e))
    }

    override fun onResponse(call: Call, response: Response) {
        success.invoke(context, MyCallbackResponse(call, response))
    }
}