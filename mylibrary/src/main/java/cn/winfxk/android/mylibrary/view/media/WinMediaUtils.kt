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
* Created Date: 2026/06/02 14:07
*/

package cn.winfxk.android.mylibrary.view.media

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.winfxk.lib.sid.SnowflakeID
import com.winfxk.lib.utils.tab.Tabable
import java.io.File
import java.io.FileOutputStream

/**
 * 媒体文件处理核心工具类
 * * 负责将各种系统 Uri 统一转换为内部 Cache 目录下的物理 File
 */
object WinMediaUtils : Tabable {
    private val filename by lazy { SnowflakeID(1, 1) }
    /**
     * 获取统一的媒体缓存目录
     */
    fun getMediaCacheDir(context: Context): File {
        val dir = File(context.cacheDir, "win_media_cache")
        if (! dir.exists() || ! dir.isDirectory) dir.mkdirs()
        return dir
    }

    /**
     * 将系统提供的 Uri（相册、文件管理器）复制为私有目录的临时 File
     * 并智能推断其正确的扩展名
     */
    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "file"
            val ist = contentResolver.openInputStream(uri) ?: return null
            val fileName = "MEDIA_${filename.nextId()}.$extension"
            val tempFile = File(getMediaCacheDir(context), fileName)
            FileOutputStream(tempFile).use { os -> ist.use { it.copyTo(os) } }
            tempFile
        } catch (e: Exception) {
            Log.e(tag, "uriToFile: 在转换 Uri 为 File 时出现异常！", e)
            null
        }
    }

    /**
     * 为相机生成一个空的临时图片文件用于写入
     */
    fun createCacheImageFile(context: Context): File {
        return File.createTempFile("IMG_${filename.nextId()}_", ".jpg", getMediaCacheDir(context))
    }
}