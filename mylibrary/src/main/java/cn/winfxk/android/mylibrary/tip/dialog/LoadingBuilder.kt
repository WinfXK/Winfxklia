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
* Created Date: 2026/02/06 11:15 */
package cn.winfxk.android.mylibrary.tip.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.view.etv.EffectTextView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.winfxk.lib.utils.to.toBigDecimal
import com.winfxk.lib.utils.toScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@SuppressLint("SetTextI18n")
class LoadingBuilder(context: Context) : BaseBuilder(context) {
    override fun getLayoutId(): Int = R.layout.winfxklia_loadingbuilder
    private val textView1 by lazy { findViewById<TextView>(R.id.textView1) }
    private val textView2 by lazy { findViewById<TextView>(R.id.textView2) }
    private val textView3 by lazy { findViewById<EffectTextView>(R.id.textView3) }
    private val textView4 by lazy { findViewById<TextView>(R.id.textView4) }
    val array by lazy { context.resources.getStringArray(R.array.winfxklia_loading_motto).toMutableList() }
    private val progressBar by lazy { findViewById<LinearProgressIndicator>(R.id.progressBar) }

    @Volatile
    var max = 0L
        @Synchronized set(value) {
            field = value
            reloadView()
        }

    @Volatile
    var current = 0L
        @Synchronized set(value) {
            field = value
            runOnUI {
                if (max > 0) {
                    val progressRatio = value.toBigDecimal().divide(max.toBigDecimal(), 4, RoundingMode.HALF_UP)
                    progressBar.progress = (progressRatio.multiply(BigDecimal(defMax))).toInt()
                } else progressBar.progress = 0
            }
            reloadView()
        }

    @get:Synchronized
    @set:Synchronized
    @Volatile
    private var next: Boolean = true

    @Synchronized
    private fun reloadView() {
        runOnUI {
            textView1.text = "$current/$max"
            textView2.text = if (max == 0L) "0%" else "${
                (BigDecimal(current).divide(BigDecimal(max), 4, RoundingMode.HALF_UP).multiply(b100)).toScale()
            }%"
        }
    }

    var title: String
        get() = textView4.text.toString()
        set(value) {
            textView4.text = value
        }

    override fun initializeView() {
        progressBar.max = defMax
        reloadView()
    }

    override fun show() {
        reloadView()
        super.show()
        textView3.onEffectFinishedListener = {
            next = true
        }
        scope.launch {
            while (true) {
                while (! next) delay(1000)
                delay(3000)
                next = false
                if (array.isNotEmpty()) textView3.setEffectText(array.random())
            }
        }
    }

    companion object {
        private val b100 = BigDecimal(100)
        private const val defMax = 10000
    }
}