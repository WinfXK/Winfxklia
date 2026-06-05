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
* Created by IntelliJ IDEA
* Author： Winfxk
* Web: http://winfxk.com
* Created Date: 2026/06/05 09:36
*/

package cn.winfxk.android.mylibrary

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import cn.winfxk.android.mylibrary.tip.Toast
import cn.winfxk.android.mylibrary.tip.dialog.BuilderListener
import cn.winfxk.android.mylibrary.tip.dialog.DialogType
import cn.winfxk.android.mylibrary.tip.dialog.MyBuilder
import cn.winfxk.android.mylibrary.utils.Tablabel
import kotlinx.coroutines.CoroutineScope

@Suppress("UNUSED")
abstract class BaseActivity : AppCompatActivity(), Tablabel {
    val scope: CoroutineScope get() = lifecycleScope
    open val binding: ViewBinding? = null
    protected open val rootView: View? get() = binding?.root
    private var onPermissionsResultCallback: ((Map<String, Boolean>) -> Unit)? = null
    @get:LayoutRes
    protected open val contentView: Int? = null
    private val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        onPermissionsResultCallback?.invoke(results)
        onPermissionsResultCallback = null
    }
    /**
     * 是否启用 Edge-to-Edge 沉浸式模式
     * 开启后，内容将延伸至状态栏和导航栏区域，你需要手动处理内边距 (Insets) 以防内容被遮挡。
     */
    open val isEdgeToEdge: Boolean = true

    /**
     * 真正的全屏模式。开启后会隐藏状态栏和导航栏（适合游戏、全屏视频播放等）。
     */
    open val isFullScreen: Boolean = false

    /**
     * 返回此页面启动时**必需**的权限清单。
     * 如果非空，BaseActivity 会在 `init()` 之前自动请求这些权限。
     * 默认为空列表，跳过启动时检查。
     */
    open fun getRequiredPermissions(): List<String> = emptyList()

    /**
     * 如果 [getRequiredPermissions] 中的权限被拒绝，是否允许继续运行页面逻辑。
     * - false: (默认) 强制要求权限，被拒绝时将显示默认弹窗并销毁页面 (finish)。
     * - true: (风险自担) 允许继续，将执行 `init()`。
     */
    open val allowContinueOnPermissionFailure: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowDisplayMode()
        setupContentView()
        val requiredPermissions = getRequiredPermissions()
        if (requiredPermissions.isEmpty()) init()
        else requestAppPermissionsInternal(
            permissionsToRequest = requiredPermissions,
            allowContinueOnFailure = allowContinueOnPermissionFailure,
            onGranted = { init() },
            onDenied = { deniedList -> onPermissionsDenied(deniedList) }
        )
    }

    /**
     * 页面初始化入口。
     */
    protected abstract fun init()

    private fun setupContentView() {
        binding?.root?.let {
            setContentView(it)
            return
        }
        rootView?.let {
            setContentView(it)
            return
        }
        contentView?.let {
            setContentView(it)
            return
        }
    }

    private fun setupWindowDisplayMode() {
        if (! isEdgeToEdge && ! isFullScreen) return
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (isFullScreen) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else if (isEdgeToEdge) {
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
                @Suppress("DEPRECATION")
                window.isStatusBarContrastEnforced = false
            }
        }

        // 允许内容延伸进刘海屏区域 (Android 9+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    /**
     * (可选钩子) 当启动时请求的必需权限被拒绝时调用。
     * 子类可以重写此方法以显示自定义提示。
     */
    open fun onPermissionsDenied(deniedPermissions: List<String>) {
    }

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
                if (allowContinueOnFailure) onGranted.invoke()
                else showDefaultDenialDialogAndFinish()
            }
        }
        requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }

    /**
     * 手动请求一个或多个权限 (业务逻辑中动态调用)。
     *
     * @param permissionsToRequest 需要请求的权限列表
     * @param allowContinueOnFailure 如果为 false，当用户拒绝权限时，将弹窗提示并关闭 Activity。
     * @param onGranted 所有权限都被授予时的回调。
     * @param onDenied 任何一个权限被拒绝时的回调，返回被拒绝的权限列表。
     */
    protected fun requestAppPermissions(
        permissionsToRequest: List<String>,
        allowContinueOnFailure: Boolean = true,
        onGranted: () -> Unit,
        onDenied: ((deniedPermissions: List<String>) -> Unit)? = null
    ) = requestAppPermissionsInternal(permissionsToRequest, allowContinueOnFailure, onGranted, onDenied)

    /**
     * 手动请求单个权限的快捷方法。
     */
    protected fun requestAppPermission(
        permission: String,
        allowContinueOnFailure: Boolean = true,
        onGranted: () -> Unit,
        onDenied: (() -> Unit)? = null
    ) {
        requestAppPermissionsInternal(listOf(permission), allowContinueOnFailure, onGranted, onDenied = { _ -> onDenied?.invoke() })
    }

    /**
     * 默认的权限拒绝提示窗。
     */
    private fun showDefaultDenialDialogAndFinish() {
        val builder = MyBuilder(this)
        builder.title = "缺少必要权限"
        builder.message = "应用需要相关核心权限才能继续运行。请在系统设置中授予权限后重试。"
        builder.type = DialogType.Ask
        builder.addButton("退出") { finish() }
        builder.show()
    }

    /**
     * 隐藏软键盘。
     */
    protected fun hideKeyboard() {
        val view = this.currentFocus ?: return
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * 强制显示软键盘。
     * 注意：调用前最好先让目标 View 获取焦点 (view.requestFocus())。
     */
    protected fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * 更加现代、高级的启动 Activity 方法。
     * @param finishCurrent 是否关闭当前 Activity
     * @param block 允许你在 lambda 中优雅地配置 Intent (例如传参、添加 Flag)
     */
    inline fun <reified T : Activity> launchActivity(finishCurrent: Boolean = false, block: Intent.() -> Unit = {}) {
        val intent = Intent(this, T::class.java)
        intent.block()
        startActivity(intent)
        if (finishCurrent) finish()
    }

    /**
     * 判断当前系统是否处于深色模式 (Dark Mode)。
     */
    protected fun isNightMode(): Boolean {
        return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * 获取屏幕真实宽度（像素）
     */
    protected fun getScreenWidth(): Int {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)
        return metrics.widthPixels
    }

    /**
     * 获取屏幕真实高度（像素）
     */
    protected fun getScreenHeight(): Int {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)
        return metrics.heightPixels
    }

    /**
     * 快速让 View 变为 VISIBLE
     */
    protected fun View.visible() {
        this.visibility = View.VISIBLE
    }

    /**
     * 快速让 View 变为 GONE
     */
    protected fun View.gone() {
        this.visibility = View.GONE
    }

    /**
     * 快速让 View 变为 INVISIBLE
     */
    protected fun View.invisible() {
        this.visibility = View.INVISIBLE
    }

    /**
     * 显示自定义 Toast。
     */
    fun toast(message: String) {
        Toast.makeText(this, message).show()
    }

    /**
     * 显示自定义弹窗 (Dialog)。
     */
    fun tip(
        message: String,
        type: DialogType = DialogType.Info,
        title: String = "提示",
        listener: BuilderListener = MyBuilder.emptyListener
    ) {
        val builder = MyBuilder(this)
        builder.title = title
        builder.message = message
        builder.type = type
        builder.addButton("确定", block = listener)
        builder.show()
    }

    /**
     * 复制文本到系统剪贴板。
     */
    fun copyText(text: String) {
        copyText(this, text)
        toast("已复制到剪贴板")
    }

    companion object {
        /**
         * 复制纯文本到剪贴板。
         *
         * @param context 上下文
         * @param text 要复制的文本内容
         */
        fun copyText(context: Context, text: String) {
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)
        }
    }
}