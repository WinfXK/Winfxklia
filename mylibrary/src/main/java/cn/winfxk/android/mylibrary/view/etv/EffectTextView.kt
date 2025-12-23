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
 * Created Date: 2025/12/23 18:10 */

package cn.winfxk.android.mylibrary.view.etv

import android.R
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatTextView

typealias OnEffectFinishedListener = EffectTextView.() -> Unit

class EffectTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {
    var config = ParticleConfig()
    var onEffectFinishedListener: OnEffectFinishedListener? = null
    private val activeTasks = mutableListOf<TransitionTask>()
    private var lastTargetText: String = text.toString()
    private val animators = mutableListOf<ValueAnimator>()
    fun setEffectText(newText: CharSequence) {
        val oldStr = lastTargetText
        val newStr = newText.toString()
        lastTargetText = newStr
        if (! hasWindowFocus() || width <= 0) {
            text = newStr
            onEffectFinishedListener?.invoke(this)
            return
        }
        val task = TransitionTask(this, oldStr, newStr, TextPaint(paint))
        activeTasks.add(task)
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = config.duration
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener {
            task.update(it.animatedValue as Float)
            invalidate()
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                animators.remove(animator)
            }
        })
        animators.add(animator)
        animator.start()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (! hasWindowFocus) cancelAllAnimations()
    }

    override fun onDetachedFromWindow() {
        cancelAllAnimations()
        super.onDetachedFromWindow()
    }

    private fun cancelAllAnimations() {
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
        val iterator = activeTasks.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            task.draw(canvas)
            if (task.isFinished) {
                iterator.remove()
                onEffectFinishedListener?.invoke(this)
            }
        }
    }
}