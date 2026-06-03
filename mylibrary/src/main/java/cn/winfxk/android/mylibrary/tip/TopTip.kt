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
* Created Date: 2026/06/03 09:44
*/
package cn.winfxk.android.mylibrary.tip

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
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
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.utils.Tablabel
import java.lang.ref.WeakReference
import java.util.LinkedList

@Suppress("unused")
@SuppressLint("InflateParams")
class TopTip private constructor(private val context: Context): Tablabel {
    val iconView: ImageView by lazy { view.findViewById(R.id.toptip_icon) }
    val textView: TextView by lazy { view.findViewById(R.id.toptip_text) }
    var time: Int = LENGTH_SHORT
    private var task: TopTipTask? = null
    val view: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.winfxklia_top_tip, null).apply {
            val bgContainer = findViewById<RelativeLayout>(R.id.toptip_background)
            val gradientDrawable = GradientDrawable().apply {
                setColor("#F5F5F7".toColorInt())
                cornerRadius = 30f
                setStroke(1, "#E0E0E0".toColorInt())
            }
            bgContainer.background = gradientDrawable
        }
    }

    init {
        TopTipLifecycleTracker.init(context)
    }

    fun show() {
        val targetActivity = getActivity(context) ?: TopTipLifecycleTracker.currentActivity
        if (targetActivity == null) {
            Log.w(tab, "当前不在前台 Activity 环境中，回退至原生 Toast")
            android.widget.Toast.makeText(context, textView.text, time).show()
            return
        }
        val durationMs = if (time == LENGTH_LONG) 4000L else 2500L
        task = TopTipTask(WeakReference(targetActivity), view, durationMs)
        TopTipManager.enqueue(task !!)
    }

    fun cancel() {
        task?.let { TopTipManager.cancel(it) }
    }

    fun setText(string: String?) {
        textView.text = string
    }

    fun setIcon(resId: Int) {
        iconView.setImageResource(resId)
    }

    fun setIcon(drawable: Drawable?) {
        iconView.setImageDrawable(drawable)
    }

    fun setIcon(bm: Bitmap?) {
        iconView.setImageBitmap(bm)
    }

    fun setIcon(uri: Uri?) {
        iconView.setImageURI(uri)
    }

    /**
     * 设置背景颜色和文字颜色
     */
    fun setColors(bgColor: Int, textColor: Int) {
        val bgContainer = view.findViewById<RelativeLayout>(R.id.toptip_background)
        val drawable = bgContainer.background as? GradientDrawable ?: GradientDrawable().apply { cornerRadius = 30f }
        drawable.setColor(bgColor)
        bgContainer.background = drawable
        textView.setTextColor(textColor)
    }

    fun setRandImage() {
        val r = Toast.Images1.random()
        this.iconView.setImageResource(r)
    }

    private fun getActivity(context: Context?): Activity? {
        return when (context) {
            is Activity       -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else              -> null
        }
    }

    companion object {
        const val LENGTH_SHORT: Int = 0
        const val LENGTH_LONG: Int = 1

        @JvmStatic
        fun makeText(context: Context, string: Any?, duration: Int = LENGTH_SHORT): TopTip {
            val tip = TopTip(context)
            tip.setText(string.toString())
            tip.time = duration
            tip.setRandImage()
            return tip
        }
    }
}

private data class TopTipTask(
    val activityRef: WeakReference<Activity>,
    val view: View,
    val durationMs: Long
)

/**
 * 顶部横幅管理器
 */
@SuppressLint("StaticFieldLeak")
private object TopTipManager {
    private val queue = LinkedList<TopTipTask>()
    private var isShowing = false
    private val handler = Handler(Looper.getMainLooper())
    private var currentTask: TopTipTask? = null
    private val dismissRunnable = Runnable { dismissCurrent() }

    fun enqueue(task: TopTipTask) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post { enqueue(task) }
            return
        }
        queue.offer(task)
        showNextIfNeeded()
    }

    fun cancel(task: TopTipTask) {
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
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL }
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val viewHeight = view.measuredHeight.toFloat()
        val startY = if (viewHeight > 0) - viewHeight - 100f else - 300f // 从屏幕外部开始滑入
        view.translationY = startY
        view.alpha = 0f
        rootView.addView(view, params)
        view.setOnClickListener {
            handler.removeCallbacks(dismissRunnable)
            dismissCurrent()
        }
        view.animate().alpha(1f).translationY(0f).setDuration(400)
            .setInterpolator(DecelerateInterpolator(1.5f)).withEndAction {
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
        val viewHeight = if (view.height > 0) view.height.toFloat() else 300f
        view.animate().alpha(0f).translationY(- viewHeight - 50f)
            .setDuration(250).setInterpolator(AccelerateInterpolator()).withEndAction {
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
 * 独立的 LifecycleTracker
 */
@SuppressLint("StaticFieldLeak")
private object TopTipLifecycleTracker : Application.ActivityLifecycleCallbacks {
    private var isInitialized = false
    private var activityReference: WeakReference<Activity>? = null

    val currentActivity: Activity?
        get() = activityReference?.get()

    fun init(context: Context) {
        if (isInitialized) return
        val app = context.applicationContext as? Application ?: return
        app.registerActivityLifecycleCallbacks(this)
        isInitialized = true
    }

    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        activityReference = WeakReference(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        if (currentActivity == activity) {
            activityReference = null
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}