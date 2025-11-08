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
* Created PCUser: Winfx 
* Web: http://winfxk.com
* Created Date: 2025/11/4  16:09 */
package cn.winfxk.android.mylibrary.tip.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.StyleRes
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.view.ViewInitialize
import com.winfxk.lib.utils.className
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel


typealias BuilderListener = (BaseBuilder) -> Unit

@Suppress("unused")
@SuppressLint("InflateParams")
abstract class BaseBuilder(context: Context, @field:StyleRes val theme: Int = themes.random()) : Dialog(context, theme), ViewInitialize,
    DialogInterface.OnDismissListener {
    protected open val iconAnim: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.winfxklia_progress_dialog_icon); }
    protected open val alphaShow: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.winfxklia_alpha_show); }
    protected open val alphaHide: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.winfxklia_alpha_hide); }
    protected val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
    private val activityScope = MainScope()
    protected val scope: CoroutineScope get() = activityScope
    protected open val closeListener = ArrayList<BuilderListener>();
    @Volatile open var isShow = false; protected set;
    /**
     * 判断是在异步线程
     */
    protected open val isThread get() = Looper.myLooper() != Looper.getMainLooper()

    init {
        iconAnim.fillAfter = true;
        alphaShow.fillAfter = true
        alphaHide.fillAfter = true
        window?.setWindowAnimations(theme)
        super.setOnDismissListener(this)
        setCancelable(false)
        setContentView(getLayoutId())
    }
    /**
     * 显示Dialog并且检查是否是初次
     */
    override fun show() {
        if (isShow) throw BuilderException("不能重复启动同一个${className}实例")
        isShow = true;
        super.show()
    }
    /**
     * 执行初始化操作
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView()
    }
    /**
     * 关闭弹窗，并且取消所有协程任务
     */
    override fun dismiss() {
        super.dismiss()
        activityScope.cancel()
    }

    @Deprecated("已弃用")
    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
    }
    /**
     * 在主线程执行操作
     */
    protected fun runOnUI(task: () -> Unit) {
        if (isThread) handler.post(task)
        else task()
    }

    @Deprecated("已弃用")
    override fun onDismiss(dialog: DialogInterface?) {
        closeListener.forEach { it.invoke(this) }
    }
    /**
     * 添加关闭事件的监听器
     */
    fun addCloseListener(listener: BuilderListener) {
        closeListener.add(listener)
    }
    /**
     * 清空关闭事件的监听器
     */
    fun clearCloseListener() {
        closeListener.clear();
    }
    /**
     * 删除某个关闭事件的监听器
     */
    fun removeCloseListener(listener: BuilderListener) {
        closeListener.remove(listener)
    }

    companion object : BuilderListener {
        val themes: IntArray = intArrayOf(R.style.winfxklia_alert_dialog1, R.style.winfxklia_alert_dialog2)
        val emptyListener: BuilderListener by lazy { this }
        override fun invoke(p1: BaseBuilder) {
        }
    }
}