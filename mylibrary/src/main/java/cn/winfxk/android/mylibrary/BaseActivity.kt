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
* Created by IntelliJ IDEA
* Author： Winfxk
* Web: http://winfxk.com
* Created Date: 2025/11/04 15:25
*/

package cn.winfxk.android.mylibrary

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import cn.winfxk.android.mylibrary.utils.tab.Tabable
import cn.winfxk.android.mylibrary.view.ViewInitialize
import com.winfxk.lib.utils.className
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseActivity : AppCompatActivity(), ViewInitialize, Tabable {
    val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
    val scope: CoroutineScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    override val tag: String by lazy { className }
    open val contentView: View? = null
    /**
     * 在主线程执行任务
     */
    open fun handler(task: Runnable) = handler.post(task)
    open fun Master(task: Runnable) = handler.post(task)
    /**
     * 是否启用全屏沉浸式模式。如果为 true，将自动隐藏状态栏和导航栏，并允许内容延伸到刘海区域。
     */
    open val enableFullScreen: Boolean = true
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    val Int.dp: Int get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), resources.displayMetrics).toInt()

    val Float.dp: Float get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)

    /**
     * 返回此页面启动时**必需**的权限清单。
     * 如果非空，BaseActivity 会在 initializeView() 之前自动请求这些权限。
     * 默认为空列表，跳过启动时检查。
     */
    open fun getRequiredPermissions(): List<String> = emptyList()

    /**
     * 如果 getRequiredPermissions() 中的权限被拒绝，是否允许继续运行。
     * false: (默认) 强制要求权限，将显示默认弹窗并调用 finish() 关闭页面。
     * true: (风险自担) 允许继续，将**继续执行** initializeView()
     *
     * 注意：此设置仅对 `getRequiredPermissions()` 自动请求生效。
     */
    open val allowContinueOnPermissionFailure: Boolean = false

    /**
     * (可选钩子) 当 getRequiredPermissions() 中的必需权限被拒绝时调用。
     * 你可以在此方法中显示自定义提示 (例如 Snackbar)。
     */
    open fun onPermissionsDenied(deniedPermissions: List<String>) {
    }


    // 权限请求的回调
    private var onPermissionsResultCallback: ((Map<String, Boolean>) -> Unit)? = null

    private val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        onPermissionsResultCallback?.invoke(results)
        onPermissionsResultCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BaseActivity.resources == null) BaseActivity.resources = resources;
        if (enableFullScreen) setFullScreenImmersive()
        if (contentView == null) setContentView(getLayoutId())
        else setContentView(contentView)
        val requiredPermissions = getRequiredPermissions()
        if (requiredPermissions.isEmpty()) performInitialization()
        else requestAppPermissionsInternal(
            permissionsToRequest = requiredPermissions,
            allowContinueOnFailure = allowContinueOnPermissionFailure,
            onGranted = { performInitialization() },
            onDenied = { deniedList -> onPermissionsDenied(deniedList) }
        )
    }

    private fun performInitialization() = initializeView()

    /**
     * 设置全屏沉浸式模式（隐藏状态栏、导航栏，并延伸到刘海屏）
     */
    private fun setFullScreenImmersive() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

    /**
     * 权限请求的私有实现
     */
    private fun requestAppPermissionsInternal(
        permissionsToRequest: List<String>,
        allowContinueOnFailure: Boolean,
        onGranted: () -> Unit,
        onDenied: ((deniedPermissions: List<String>) -> Unit)?
    ) {
        onPermissionsResultCallback = { results ->
            val allGranted = results.all { it.value }
            if (allGranted) onGranted.invoke()
            else {
                val deniedList = results.filter { ! it.value }.map { it.key }
                onDenied?.invoke(deniedList)
                if (allowContinueOnPermissionFailure) onGranted.invoke()
                else showDefaultDenialDialogAndFinish()
            }
        }
        requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }

    /**
     * 显示一个默认的权限被拒绝的对话框，点击后关闭 Activity。
     */
    private fun showDefaultDenialDialogAndFinish() {
        AlertDialog.Builder(this)
            .setTitle("权限请求")
            .setMessage("应用需要必要的权限才能继续运行。请在应用设置中授予所需权限。")
            .setCancelable(false)
            .setPositiveButton("退出应用") { _, _ -> finish() }
            .show()
    }


    /**
     * 手动请求一个或多个权限。
     *
     * @param permissionsToRequest 需要请求的权限列表 (例如: listOf(Manifest.permission.CAMERA))
     * @param allowContinueOnFailure 如果为 false，当用户拒绝权限时，将弹窗提示并关闭 Activity。
     * @param onGranted 所有权限都被授予时的回调。
     * @param onDenied 任何一个权限被拒绝时的回调，返回被拒绝的权限列表。
     */
    protected fun requestAppPermissions(
        permissionsToRequest: List<String>,
        allowContinueOnFailure: Boolean = true,
        onGranted: () -> Unit,
        onDenied: ((deniedPermissions: List<String>) -> Unit)? = null
    ) {
        requestAppPermissionsInternal(
            permissionsToRequest,
            allowContinueOnFailure,
            onGranted,
            onDenied
        )
    }

    /**
     * 手动请求单个权限的重载方法。
     */
    protected fun requestAppPermission(
        permission: String,
        allowContinueOnFailure: Boolean = true,
        onGranted: () -> Unit,
        onDenied: (() -> Unit)? = null
    ) {
        requestAppPermissionsInternal(
            listOf(permission),
            allowContinueOnFailure,
            onGranted,
            onDenied = { _ -> onDenied?.invoke() }
        )
    }

    /**
     * 隐藏软键盘
     */
    protected fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /**
     * 启动一个新的 Activity
     *
     * @param activityClass 要启动的 Activity 的 Class
     * @param extras (可选) 需要传递的 Bundle
     * @param finishCurrent (可选) 是否关闭当前 Activity
     */
    protected fun <T : Activity> launchActivity(
        activityClass: Class<T>,
        finishCurrent: Boolean = false,
        extras: Bundle? = null
    ) {
        val intent = Intent(this, activityClass)
        extras?.let { intent.putExtras(it) }
        startActivity(intent)
        if (finishCurrent) finish()
    }

    companion object {
        var resources: Resources? = null
            private set
    }
}

