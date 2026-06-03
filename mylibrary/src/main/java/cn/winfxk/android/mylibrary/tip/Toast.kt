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
* Created by AI Assistant
* Author： Winfxk
* Web: http://winfxk.com
* Created Date: 2026/06/03 09:32
*/
package cn.winfxk.android.mylibrary.tip

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import cn.winfxk.android.mylibrary.R
import java.lang.ref.WeakReference
import java.util.LinkedList

@Suppress("unused")
@SuppressLint("InflateParams")
class Toast private constructor(private val context: Context) {
    private val view: View by lazy { LayoutInflater.from(context).inflate(R.layout.winfxklia_toast, null) }
    private val imageView: ImageView by lazy { view.findViewById(R.id.imageView1) }
    private val imageView1: ImageView by lazy { view.findViewById(R.id.imageView2) }
    private val textView: TextView by lazy { view.findViewById(R.id.textView1) }
    var time: Int = LENGTH_SHORT
    private var task: ToastTask? = null

    init {
        LifecycleTracker.init(context)
    }

    /**
     * 显示 Toast
     */
    fun show() {
        val targetActivity = getActivity(context) ?: LifecycleTracker.currentActivity
        if (targetActivity == null) {
            Log.w("WinfxkToast", "当前不在前台 Activity 环境中，回退至原生 Toast")
            android.widget.Toast.makeText(context, textView.text, time).show()
            return
        }
        val durationMs = if (time == LENGTH_LONG) 3500L else 2000L
        task = ToastTask(WeakReference(targetActivity), view, durationMs)
        ToastManager.enqueue(task !!)
    }

    /**
     * 提前取消显示
     */
    fun cancel() {
        task?.let { ToastManager.cancel(it) }
    }

    fun setText(string: String?) {
        textView.text = string
    }

    fun setImage(drawable: Drawable?) {
        imageView.setImageDrawable(drawable)
        imageView1.setImageDrawable(drawable)
    }

    fun setImage(bm: Bitmap?) {
        imageView.setImageBitmap(bm)
        imageView1.setImageBitmap(bm)
    }

    fun setImage(resId: Int) {
        imageView.setImageResource(resId)
        imageView1.setImageResource(resId)
    }

    fun setImage(uri: Uri?) {
        imageView.setImageURI(uri)
        imageView1.setImageURI(uri)
    }

    fun setTextColor(r: Int, g: Int, b: Int) {
        val drawable = textView.background as GradientDrawable
        drawable.setColor(Color.rgb(255 - r, 255 - g, 255 - b))
        textView.background = drawable
        textView.setTextColor(Color.rgb(r, g, b))
    }

    fun setRandColor() {
        setTextColor((0 .. 255).random(), (0 .. 255).random(), (0 .. 255).random())
    }

    fun setTextBackgColor(r: Int, g: Int, b: Int) {
        val drawable = textView.background as GradientDrawable
        drawable.setColor(Color.rgb(r, g, b))
        textView.background = drawable
        textView.setTextColor(Color.rgb(255 - r, 255 - g, 255 - b))
    }

    fun setRandBackgColor() {
        setTextBackgColor((0 .. 255).random(), (0 .. 255).random(), (0 .. 255).random())
    }

    fun setRandImage() {
        val r = Images1.random()
        var r2 = Images1.random()
        while (r == r2) r2 = Images1.random()
        this.imageView.setImageResource(r)
        this.imageView1.setImageResource(r2)
    }

    private fun getActivity(context: Context?): Activity? {
        return when (context) {
            is Activity       -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else              -> null
        }
    }

    companion object {
        internal val Images1: IntArray = intArrayOf(
            R.drawable.winfxklia_toast_icon1, R.drawable.winfxklia_toast_icon2,
            R.drawable.winfxklia_toast_icon3, R.drawable.winfxklia_toast_icon4,
            R.drawable.winfxklia_toast_icon5, R.drawable.winfxklia_toast_icon6,
            R.drawable.winfxklia_toast_icon7, R.drawable.winfxklia_toast_icon8,
            R.drawable.winfxklia_toast_icon9, R.drawable.winfxklia_toast_icon10,
            R.drawable.winfxklia_toast_icon11, R.drawable.winfxklia_toast_icon12,
            R.drawable.winfxklia_toast_icon13, R.drawable.winfxklia_toast_icon14,
            R.drawable.winfxklia_toast_icon15, R.drawable.winfxklia_toast_icon16,
            R.drawable.winfxklia_toast_icon17, R.drawable.winfxklia_toast_icon18,
            R.drawable.winfxklia_toast_icon19, R.drawable.winfxklia_toast_icon20
        )
        const val LENGTH_SHORT: Int = 0
        const val LENGTH_LONG: Int = 1

        @JvmStatic
        fun makeText(context: Context, string: Any?, duration: Int = LENGTH_SHORT): Toast {
            val toast = Toast(context)
            toast.setText(string.toString())
            toast.time = duration
            toast.setRandImage()
            return toast
        }
    }
}

/**
 * 内部任务类，持有弱引用防止内存泄漏
 */
private data class ToastTask(
    val activityRef: WeakReference<Activity>,
    val view: View,
    val durationMs: Long
)

/**
 * 智能队列管理器 (单例)
 */
@SuppressLint("StaticFieldLeak")
private object ToastManager {
    private val queue = LinkedList<ToastTask>()
    private var isShowing = false
    private val handler = Handler(Looper.getMainLooper())
    private var currentTask: ToastTask? = null
    private val dismissRunnable = Runnable { dismissCurrent() }

    fun enqueue(task: ToastTask) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post { enqueue(task) }
            return
        }
        queue.offer(task)
        showNextIfNeeded()
    }

    fun cancel(task: ToastTask) {
        if (currentTask == task) {
            handler.removeCallbacks(dismissRunnable)
            dismissCurrent()
        } else queue.remove(task)
    }

    private fun showNextIfNeeded() {
        if (isShowing || queue.isEmpty()) return
        val task = queue.poll() ?: return
        val activity = task.activityRef.get()
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            showNextIfNeeded()
            return
        }
        isShowing = true
        currentTask = task
        val rootView = activity.findViewById<FrameLayout>(android.R.id.content)
        val view = task.view
        (view.parent as? ViewGroup)?.removeView(view)
        val bottomMarginPx = (100 * activity.resources.displayMetrics.density).toInt()
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            bottomMargin = bottomMarginPx
        }
        view.alpha = 0f
        view.translationY = 50f
        rootView.addView(view, params)
        view.animate().alpha(1f).translationY(0f).setDuration(300)
            .setInterpolator(DecelerateInterpolator()).withEndAction {
                if (currentTask == task) {
                    handler.removeCallbacks(dismissRunnable)
                    handler.postDelayed(dismissRunnable, task.durationMs)
                }
            }.start()
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                handler.removeCallbacks(dismissRunnable)
                view.removeOnAttachStateChangeListener(this)
                if (currentTask == task) {
                    currentTask = null
                    isShowing = false
                    showNextIfNeeded()
                }
            }
        })
    }

    private fun dismissCurrent() {
        val task = currentTask ?: return
        val view = task.view
        view.animate().alpha(0f).translationY(50f).setDuration(250)
            .setInterpolator(AccelerateInterpolator()).withEndAction {
                if (currentTask == task) {
                    currentTask = null
                    isShowing = false
                    (view.parent as? ViewGroup)?.removeView(view)
                    showNextIfNeeded()
                } else (view.parent as? ViewGroup)?.removeView(view)
            }.start()
    }
}

/**
 * 自动生命周期追踪器
 */
@SuppressLint("StaticFieldLeak")
private object LifecycleTracker : Application.ActivityLifecycleCallbacks {
    private var isInitialized = false
    private var activityReference: WeakReference<Activity>? = null
    val currentActivity: Activity? get() = activityReference?.get()

    fun init(context: Context) {
        if (isInitialized) return
        val app = context.applicationContext as? Application ?: return
        app.registerActivityLifecycleCallbacks(this)
        isInitialized = true
    }

    override fun onActivityResumed(activity: Activity) {
        activityReference = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivity == activity) activityReference = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}