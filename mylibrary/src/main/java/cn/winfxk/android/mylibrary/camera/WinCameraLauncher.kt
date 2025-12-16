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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import kotlin.jvm.java

/**
 * 高清相机启动器 (HD Camera Launcher)
 * * 自动处理权限申请和界面跳转。
 */
class WinCameraLauncher(private val activity: ComponentActivity, private val onResult: (File, Uri) -> Unit, private val onCancel: () -> Unit = {}) {
    private val cameraActivityLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val path = result.data?.getStringExtra(WinCameraActivity.EXTRA_OUTPUT_PATH)
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    val authority = "${activity.packageName}.winfxk.camera.provider"
                    val uri = FileProvider.getUriForFile(activity, authority, file)
                    onResult(file, uri)
                }
            }
        } else onCancel()
    }

    private val permissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) launchInternal() else {
            Toast.makeText(activity, "需要相机权限才能拍摄", Toast.LENGTH_SHORT).show()
            onCancel()
        }
    }

    /**
     * 启动高清相机
     * 调用此方法时会自动检查权限
     */
    fun launch() {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun launchInternal() {
        val intent = Intent(activity, WinCameraActivity::class.java)
        cameraActivityLauncher.launch(intent)
    }
}