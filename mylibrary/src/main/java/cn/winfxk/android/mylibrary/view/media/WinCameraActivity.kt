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

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import cn.winfxk.android.mylibrary.databinding.WinfxkliaActivityWinCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

/**
 * 库内部的高清相机 Activity。
 * 外部不应直接通过 Intent 启动它，而应使用 [WinMediaSelector.takePhotoByApp]。
 */
class WinCameraActivity : AppCompatActivity(), Runnable, ImageCapture.OnImageSavedCallback {
    private lateinit var binding: WinfxkliaActivityWinCameraBinding
    private var imageCapture: ImageCapture? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val lf = AtomicReference<ListenableFuture<ProcessCameraProvider>?>(null);
    private val photoFile = AtomicReference<File?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = WinfxkliaActivityWinCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnCapture.setOnClickListener { takePhoto() }
        binding.btnClose.setOnClickListener { finish() }
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        lf.set(cameraProviderFuture)
        cameraProviderFuture.addListener(this, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = WinMediaUtils.createCacheImageFile(this)
        this.photoFile.set(photoFile)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun run() {
        val cameraProvider = lf.get()?.get() ?: throw RuntimeException("在获取资源时出现异常！异常的上下文冲突(cameraProvider)！")
        val preview = Preview.Builder().build().also { it.surfaceProvider = binding.viewFinder.surfaceProvider }
        val resolutionStrategy = ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY
        val resolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(resolutionStrategy)
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
            .build()
        imageCapture = ImageCapture.Builder()
            .setResolutionSelector(resolutionSelector)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setJpegQuality(100).build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        } catch (exc: Exception) {
            Toast.makeText(this, "相机启动失败: ${exc.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onError(exc: ImageCaptureException) {
        exc.printStackTrace()
        Toast.makeText(this@WinCameraActivity, "拍摄失败", Toast.LENGTH_SHORT).show()
    }

    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
        val photoFile = this.photoFile.get() ?: throw RuntimeException("在获取资源时出现异常！异常的上下文冲突（photoFile）！")
        val resultIntent = Intent();
        resultIntent.putExtra(EXTRA_OUTPUT_PATH, photoFile.absolutePath)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        const val EXTRA_OUTPUT_PATH = "extra_output_path"
    }
}