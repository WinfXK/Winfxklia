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
* Created Date: 2025/12/22  10:54 */
package cn.winfxk.android.mylibrary.tip.dialog

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import cn.winfxk.android.mylibrary.R
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.winfxk.lib.utils.to.toBigDecimal
import com.winfxk.lib.utils.toScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal

@SuppressLint("SetTextI18n")
class LoadingBuilder(context: Context) : BaseBuilder(context) {
    override fun getLayoutId(): Int = R.layout.winfxklia_loadingbuilder
    private val textView1 by lazy { findViewById<TextView>(R.id.textView1) }
    private val textView2 by lazy { findViewById<TextView>(R.id.textView2) }
    private val textView3 by lazy { findViewById<TextView>(R.id.textView3) }
    private val textView4 by lazy { findViewById<TextView>(R.id.textView4) }
    private val array by lazy { context.resources.getStringArray(R.array.winfxklia_loading_motto).toMutableList() }
    private val progressBar by lazy { findViewById<LinearProgressIndicator>(R.id.progressBar) }
    private val overAnimator by lazy {
        object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                next = true;
            }
        }
    }
    private val animator by lazy {
        object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                startFadeInTypewriter(textView3, array.random())
            }
        }
    }
    @Volatile var max = 0L
        @Synchronized set(value) {
            field = value
            reloadView()
        }
    @Volatile var current = 0L
        @Synchronized set(value) {
            field = value
            val ro = max.toBigDecimal() / defMax.toBigDecimal();
            progressBar.progress = (value.toBigDecimal() / ro).toInt()
            reloadView()
        }
    @get:Synchronized
    @set:Synchronized
    @Volatile private var next: Boolean = true;

    @Synchronized
    private fun reloadView() {
        runOnUI {
            textView1.text = "$current/$max"
            val nb = BigDecimal(current).divide(BigDecimal(max)).multiply(b100)
            textView2.text = if (max == 0L) "0%" else "${(nb).toScale()}%"
        }
    }

    var title: String
        get() = textView4.text.toString()
        set(value) {
            textView4.setText(value)
        }

    override fun initializeView() {
        progressBar.max = defMax;
    }

    override fun show() {
        super.show()
        scope.launch {
            while (true) {
                while (! next) delay(1000)
                delay(3000)
                next = false;
                textView3.animate().alpha(0f).setDuration(textView3AnimateDuration).setListener(animator).start()
            }
        }
    }

    private fun startFadeInTypewriter(textView: TextView, content: String) {
        val spannableString = SpannableString(content)
        val length = content.length
        val baseColor = textView.currentTextColor
        val red = Color.red(baseColor)
        val green = Color.green(baseColor)
        val blue = Color.blue(baseColor)
        for (i in 0 until length) {
            spannableString.setSpan(
                ForegroundColorSpan(Color.argb(0, red, green, blue)),
                i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        textView.text = spannableString
        textView.alpha = 1f
        val animator = ValueAnimator.ofFloat(0f, length.toFloat())
        animator.duration = length * charDuration
        animator.addUpdateListener { valueAnimator ->
            val progress = valueAnimator.animatedValue as Float
            val currentSpannable = SpannableString(content)
            for (i in 0 until length) {
                val alpha = when {
                    i < progress.toInt()  -> 255
                    i == progress.toInt() -> {
                        ((progress - i) * 255).toInt()
                    }
                    else                  -> 0
                }
                currentSpannable.setSpan(
                    ForegroundColorSpan(Color.argb(alpha, red, green, blue)),
                    i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            textView.text = currentSpannable
        }
        animator.addListener(overAnimator)
        animator.start()
    }

    companion object {
        private const val charDuration = 150L;
        private val b100 = BigDecimal(100);
        private const val defMax = 10000;
        private const val textView3AnimateDuration = 800L
    }
}