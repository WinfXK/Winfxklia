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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.winfxk.lib.utils.tab.Tabable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 现代化全能媒体选择器 (Media Selector)
 * * 将所有 Launcher 的生命周期注册安全地固定在实例创建时。
 * * 巧妙地允许在方法调用时传入回调参数（约定优于配置，且无脑化）。
 * * 自动支持协程文件转存，所有操作结果均为物理 File。
 * * @param activity 需要在 Activity/Fragment 的 onCreate 及之前实例化该类
 */
class WinMediaSelector(private val activity: ComponentActivity) : Tabable {
    private var multiFileCallback: ((List<File>) -> Unit)? = null
    private var cancelCallback: (() -> Unit)? = null
    private var errorCallback: ((Throwable) -> Unit)? = null
    private var tempSystemCameraFile: File? = null
    private val pickMultiImagesLauncher = activity.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { handleUris(it) }
    private val pickSingleImageLauncher = activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> handleUris(if (uri != null) listOf(uri) else emptyList()) }
    private val pickMultiFilesLauncher = activity.registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { handleUris(it) }
    private val pickSingleFileLauncher = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> handleUris(if (uri != null) listOf(uri) else emptyList()) }
    private val systemCameraLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val file = tempSystemCameraFile
            if (file != null && file.exists() && file.length() > 0) multiFileCallback?.invoke(arrayListOf(file))
            else errorCallback?.invoke(IllegalStateException("拍摄成功，但是保存可能出现异常（文件为空）！"))
        } else cancelCallback?.invoke()
        clearCallbacks()
    }
    private val appCameraLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val path = result.data?.getStringExtra(WinCameraActivity.EXTRA_OUTPUT_PATH)
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    multiFileCallback?.invoke(arrayListOf(file))
                    clearCallbacks()
                    return@registerForActivityResult
                }
            }
            errorCallback?.invoke(IllegalStateException("拍摄成功，但找不到文件路径"))
        } else cancelCallback?.invoke()
        clearCallbacks()
    }
    private val cameraPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) appCameraLauncher.launch(Intent(activity, WinCameraActivity::class.java))
        else {
            Toast.makeText(activity, "需要相机权限才能拍摄", Toast.LENGTH_SHORT).show()
            errorCallback?.invoke(SecurityException("缺少相机权限"))
            clearCallbacks()
        }
    }

    /**
     * 核心 IO 处理器：自动将获取到的 Uri 在后台协程复制为 File 并调度至主线程回调
     */
    private fun handleUris(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            activity.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val files = uris.mapNotNull { WinMediaUtils.uriToFile(activity, it) }
                    withContext(Dispatchers.Main) {
                        if (files.isNotEmpty()) {
                            if (multiFileCallback != null) multiFileCallback?.invoke(files)
                        } else errorCallback?.invoke(IllegalStateException("无法处理所选文件或复制失败"))
                        clearCallbacks()
                    }
                } catch (e: Exception) {
                    Log.e("WinMediaSelector", "handleUris: 在处理所选文件时出现异常", e)
                    withContext(Dispatchers.Main) {
                        errorCallback?.invoke(e)
                        clearCallbacks()
                    }
                }
            }
        } else {
            cancelCallback?.invoke()
            clearCallbacks()
        }
    }

    private fun clearCallbacks() {
        multiFileCallback = null
        multiFileCallback = null
        cancelCallback = null
        errorCallback = null
    }

    /**
     * 调用系统相机拍照
     * @param onResult 拍照成功回调
     * @param onCancel 取消回调
     * @param onError 错误回调
     */
    @Suppress("UNUSED")
    fun takePhotoBySystem(onResult: ((List<File>)) -> Unit, onCancel: () -> Unit = {}, onError: (Throwable) -> Unit = { it.printStackTrace() }) {
        this.multiFileCallback = onResult
        this.cancelCallback = onCancel
        this.errorCallback = onError
        try {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(activity.packageManager) == null) throw IllegalStateException("在此设备上找不到相机应用!")
            tempSystemCameraFile = WinMediaUtils.createCacheImageFile(activity)
            val authority = "${activity.packageName}.winfxk.camera.provider"
            val photoUri = FileProvider.getUriForFile(activity, authority, tempSystemCameraFile !!)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            systemCameraLauncher.launch(takePictureIntent)
        } catch (e: Exception) {
            Log.e(tag, "takePhotoBySystem: 在调用系统相机时出现异常！", e)
            onError(e)
            clearCallbacks()
        }
    }

    /**
     * 调起应用内置的高清相机 (WinCameraActivity) 拍照
     * * 自动申请权限，全程无脑
     * @param onResult 回调，选择文件后调用
     * @param onCancel 取消回调，取消选择后调用
     * @param onError 错误回调，选择文件出现异常时调用
     */
    fun takePhotoByApp(onResult: ((List<File>)) -> Unit, onCancel: () -> Unit = {}, onError: (Throwable) -> Unit = { it.printStackTrace() }) {
        this.multiFileCallback = onResult
        this.cancelCallback = onCancel
        this.errorCallback = onError
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    /**
     * 选择相册图片
     * @param allowMultiple 允许多选，默认为 true。若为 true 则必须接收
     * @param onResultMulti 回调，选择文件后调用
     * @param onCancel 取消回调，取消选择后调用
     * @param onError 错误回调，选择文件出现异常时调用
     */
    @Suppress("UNUSED")
    fun pickImages(allowMultiple: Boolean = true,
                   onResultMulti: ((List<File>) -> Unit)? = null,
                   onCancel: () -> Unit = {},
                   onError: (Throwable) -> Unit = { it.printStackTrace() }) {
        this.multiFileCallback = onResultMulti
        this.cancelCallback = onCancel
        this.errorCallback = onError
        try {
            if (allowMultiple) pickMultiImagesLauncher.launch("image/*")
            else pickSingleImageLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e(tag, "pickImages: 在启动相册选择文件时出现异常！", e)
            onError(e)
            clearCallbacks()
        }
    }

    /**
     * 选择文件管理器中的文件
     * @param mimeTypes 限制格式，默认所有类型
     * @param allowMultiple 允许多选，默认为 true
     * @param onResultMulti 回调，选择文件后调用
     * @param onCancel 取消回调，取消选择后调用
     * @param onError 错误回调，发生错误后调用
     */
    @Suppress("UNUSED")
    fun pickFiles(mimeTypes: Array<String> = arrayOf("*/*"),
                  allowMultiple: Boolean = true,
                  onResultMulti: ((List<File>) -> Unit)? = null,
                  onCancel: () -> Unit = {},
                  onError: (Throwable) -> Unit = { it.printStackTrace() }) {
        this.multiFileCallback = onResultMulti
        this.cancelCallback = onCancel
        this.errorCallback = onError
        try {
            if (allowMultiple) pickMultiFilesLauncher.launch(mimeTypes)
            else pickSingleFileLauncher.launch(mimeTypes)
        } catch (e: Exception) {
            Log.e(tag, "pickFiles: 在启动文件选择器时发生错误", e)
            onError(e)
            clearCallbacks()
        }
    }
}