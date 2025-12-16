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
* Created Date: 2025/12/12 17:30
*/

package cn.winfxk.android.lib

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import cn.winfxk.android.mylibrary.BaseActivity
import cn.winfxk.android.mylibrary.camera.CameraLauncher
import cn.winfxk.android.mylibrary.camera.WinCameraLauncher
import coil.load

class MainActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var btnCamera: Button

    /**
     * 核心步骤 1：初始化 CameraLauncher
     * 必须在 Activity 的 onCreate 之前或之中初始化（因为它内部注册了 ActivityResultLauncher）
     */
    private val cameraLauncher = WinCameraLauncher(this, { a, b ->
        ivPreview.load(b) {
            crossfade(true) // 淡入淡出效果
            placeholder(android.R.drawable.ic_menu_gallery) // 占位图
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ivPreview = findViewById(R.id.iv_preview)
        btnCamera = findViewById(R.id.btn_camera)
        btnCamera.setOnClickListener {
            cameraLauncher.launch()
        }
    }

}