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
* Created PCUser: Winfx 
* Web: http://winfxk.com
* Created Date: 2026/6/4  09:18 */
package cn.winfxk.android.mylibrary.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.text.TextPaint
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatTextView
import cn.winfxk.android.mylibrary.view.textview.AnimatorAdapter
import cn.winfxk.android.mylibrary.view.textview.ParticleConfig
import cn.winfxk.android.mylibrary.view.textview.TransitionTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

typealias OnEffectFinishedListener = TextView.() -> Unit

/**
 * 粒子消散/渐显特效文本控件
 */
@Suppress("UNUSED")
class TextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr), CoroutineScope {
    private val supervisorJob = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main.immediate + supervisorJob
    var onEffectFinishedListener: OnEffectFinishedListener? = null
    internal val activeTasks = mutableListOf<TransitionTask>()
    internal var lastTargetText: String = text.toString()
    internal val animators = mutableListOf<ValueAnimator>()
    private var prepareJob: Job? = null
    var config = ParticleConfig()
        private set
    /**
     * 优雅的 DSL 配置入口
     */
    inline fun updateConfig(block: ParticleConfig.() -> Unit) {
        config.block()
    }

    fun setEffectText(newText: CharSequence) {
        val oldStr = lastTargetText
        val newStr = newText.toString()
        if (oldStr == newStr) return
        lastTargetText = newStr
        if (! hasWindowFocus() || width <= 0) {
            text = newStr
            onEffectFinishedListener?.invoke(this)
            return
        }
        prepareJob?.cancel()
        prepareJob = launch {
            val textPaint = TextPaint(paint)
            val task = withContext(Dispatchers.Default) {
                TransitionTask.build(
                    textView = this@TextView,
                    oldText = oldStr,
                    newText = newStr,
                    tPaint = textPaint
                )
            }
            activeTasks.add(task)
            startTaskAnimation(task)
        }
    }

    private fun startTaskAnimation(task: TransitionTask) {
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = config.duration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                task.update(it.animatedValue as Float)
                invalidate()
            }
        }
        animator.addListener(AnimatorAdapter(this@TextView, animator, task))
        animators.add(animator)
        animator.start()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (! hasWindowFocus) cancelAllAnimations()
    }

    override fun onDetachedFromWindow() {
        cancelAllAnimations()
        supervisorJob.cancelChildren()
        super.onDetachedFromWindow()
    }

    private fun cancelAllAnimations() {
        prepareJob?.cancel()
        animators.forEach { it.cancel() }
        animators.clear()
        activeTasks.clear()
        text = lastTargetText
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (activeTasks.isEmpty()) {
            super.onDraw(canvas)
            return
        }
        for (i in activeTasks.indices) activeTasks[i].draw(canvas)
    }
}