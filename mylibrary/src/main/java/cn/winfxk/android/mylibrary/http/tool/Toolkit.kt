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
* Created Date: 2026/1/19  09:57 */
package cn.winfxk.android.mylibrary.http.tool

import android.content.Context
import cn.winfxk.android.mylibrary.tip.dialog.MyBuilder
import cn.winfxk.android.mylibrary.tip.dialog.Type
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.winfxk.lib.utils.to.toArray
import com.winfxk.lib.utils.to.toJson
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 构建请求结构体
 */
fun <T : Context> Call.builder(
    context: T,
    error: T.(MyCallbackFailure) -> Unit,
    success: T.(MyCallbackResponse) -> Unit
) {
    enqueue(MyCallback(context, error, success))
}
/**
 * 构建请求结构体
 */
fun <T : Context> Call.builder(
    context: T,
    builder: MyBuilder? = null,
    success: T.(MyCallbackResponse) -> Unit) = builder(context, { builder?.to("网络异常：${it.e.message}", Type.Error) }, success)
/**
 * 根据返回ID修改builder文本
 */
fun Response.retip(builder: MyBuilder, message: String?) = builder.to(message ?: when (code) {
    401  -> "登录失败：用户名或密码错误 (401)"
    403  -> "登录失败：权限不足 (403)"
    404  -> "登录失败：服务器接口不存在 (404)"
    else -> "登录失败：服务器异常 (状态码: $code)"
}, Type.Error)
/**
 * 构建一般请求
 */
fun HttpUrl.Builder.post(client: OkHttpClient, build: HttpUrl.Builder.() -> Unit): Call {
    build.invoke(this)
    val request = Request.Builder().url(build()).get().build()
    return client.newCall(request)
}
/**
 * 检查返回结果是否正常
 * @param resp 返回的文本（手动传入，防止报错）
 * @param builder 关联的对话框
 * @param isrunning 需要复位的标识符
 */
fun Response.toJson(resp: String?, builder: MyBuilder?, isrunning: AtomicBoolean) = toJson(resp, builder, { isrunning.set(false) })
/**
 * 检查返回结果是否正常
 * @param resp 返回的文本（手动传入，防止报错）
 * @param builder 关联的对话框
 * @param returnt 操作中断时出发的操作
 */
fun Response.toJson(resp: String?, builder: MyBuilder?, returnt: () -> Unit): JSONObject? {
    if (! this.isSuccessful) {
        if (builder != null) retip(builder, resp)
        returnt.invoke()
        return null
    }
    if (resp.isNullOrBlank()) {
        builder?.to("服务器返回内容为空！", Type.Error)
        returnt.invoke()
        return null
    }
    val json = try {
        resp.toJson()
    } catch (_: Exception) {
        null
    }
    if (json.isNullOrEmpty()) {
        builder?.to("解析数据失败！\n$resp", Type.Error)
        returnt.invoke()
        return null
    }
    return json;
}

/**
 * 检查返回结果是否正常
 * @param resp 返回的文本（手动传入，防止报错）
 * @param builder 关联的对话框
 * @param isrunning 需要复位的标识符
 */
fun Response.toArray(resp: String?, builder: MyBuilder?, isrunning: AtomicBoolean) = toArray(resp, builder, { isrunning.set(false) })
/**
 * 检查返回结果是否正常
 * @param resp 返回的文本（手动传入，防止报错）
 * @param builder 关联的对话框
 * @param returnt 操作中断时出发的操作
 */
fun Response.toArray(resp: String?, builder: MyBuilder?, returnt: () -> Unit): JSONArray? {
    if (! this.isSuccessful) {
        if (builder != null) retip(builder, resp)
        returnt.invoke()
        return null
    }
    if (resp.isNullOrBlank()) {
        builder?.to("服务器返回内容为空！", Type.Error)
        returnt.invoke()
        return null
    }
    val json = try {
        resp.toArray()
    } catch (_: Exception) {
        null
    }
    if (json.isNullOrEmpty()) {
        builder?.to("解析数据失败！\n$resp", Type.Error)
        returnt.invoke()
        return null
    }
    return json;
}