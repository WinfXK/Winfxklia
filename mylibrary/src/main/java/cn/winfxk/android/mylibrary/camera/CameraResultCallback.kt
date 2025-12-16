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
* Created Date: 2025/12/12 17:00
*/
package cn.winfxk.android.mylibrary.camera

import android.net.Uri
import java.io.File

/**
 * 拍照结果回调接口
 */
interface CameraResultCallback {
    /**
     * 拍照成功
     * @param file 图片文件（已写入磁盘）
     * @param uri 图片的 Content Uri (可用于直接加载)
     */
    fun onSuccess(file: File, uri: Uri)

    /**
     * 用户取消了拍照（点击了返回键）
     */
    fun onCancel()

    /**
     * 发生错误（如文件创建失败、相机应用未找到等）
     * @param t 异常信息
     */
    fun onError(t: Throwable)
}