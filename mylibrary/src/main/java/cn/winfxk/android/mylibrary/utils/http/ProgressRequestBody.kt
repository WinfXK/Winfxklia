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
package cn.winfxk.android.mylibrary.utils.http

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.buffer

/**
 * 自定义带挂起进度回调的 RequestBody。
 * 完美衔接 OkHttp 同步机制与 Kotlin 协程系统。
 */
class ProgressRequestBody(
    private val delegate: RequestBody,
    val onProgress: suspend (now: Long, max: Long) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(this, sink)
        val bufferedSink = countingSink.buffer()
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }
}
