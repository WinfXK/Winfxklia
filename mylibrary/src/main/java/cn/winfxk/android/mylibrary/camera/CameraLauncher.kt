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
* Created Date: 2025/12/12 18:00
*/

package cn.winfxk.android.mylibrary.camera

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import cn.winfxk.android.mylibrary.tip.Toast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 智能相机启动器 (Smart Camera Launcher)
 * * 特性：
 * 1. 存储于 Cache 目录，随系统清理。
 * 2. 使用 Lambda 回调，语法简洁。
 * 3. 自动处理文件创建、权限和 Uri 生成。
 *
 * @param activity 宿主 Activity
 * @param onResult 成功回调 (File: 物理文件, Uri: 用于加载的路径)
 * @param onError 错误回调 (可选，默认打印堆栈)
 * @param onCancel 取消回调 (可选)
 */
class CameraLauncher(
    private val activity: ComponentActivity,
    private val onResult: (File, Uri) -> Unit,
    private val onError: (Throwable) -> Unit = {
        Toast.makeText(activity, "拍摄图像时出现异常！\n${it.message}").show()
        it.printStackTrace()
    },
    private val onCancel: () -> Unit = {}
) {
    private var currentPhotoFile: File? = null
    private var currentPhotoUri: Uri? = null
    private val takePictureLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val file = currentPhotoFile
                val uri = currentPhotoUri
                if (file != null && uri != null && file.exists() && file.length() > 0) onResult(file, uri)
                else onError(IllegalStateException("拍摄成功，但是保存可能出现异常（保存的文件为空）！"))
            } else onCancel()
        }

    /**
     * 启动相机
     */
    fun start() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity.packageManager) == null) {
            onError(IllegalStateException("在此设备上找不到相机!"))
            return
        }
        try {
            val photoFile = createCacheImageFile()
            currentPhotoFile = photoFile
            val authority = "${activity.packageName}.winfxk.camera.provider"
            val photoUri: Uri = FileProvider.getUriForFile(
                activity,
                authority,
                photoFile
            )
            currentPhotoUri = photoUri
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            takePictureLauncher.launch(takePictureIntent)
        } catch (ex: Exception) {
            onError(ex)
        }
    }

    /**
     * 在 Cache 目录下创建临时文件
     * 路径示例: /data/user/0/com.xxx/cache/camera_temp/IMG_20251212_180000.jpg
     */
    @Throws(IOException::class)
    private fun createCacheImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_${timeStamp}_"
        val baseCacheDir = activity.externalCacheDir ?: activity.cacheDir
        val storageDir = File(baseCacheDir, "camera_temp").apply {
            if (! exists() || ! isDirectory) mkdirs()
        }
        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }
}