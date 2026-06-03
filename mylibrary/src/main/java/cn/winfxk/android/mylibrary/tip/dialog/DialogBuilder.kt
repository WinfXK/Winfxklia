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
* Created Date: 2026/6/3  08:22 */
package cn.winfxk.android.mylibrary.tip.dialog

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt

/**
 * 智能表单构建器 (DSL)
 */
@Suppress("UNUSED")
class DialogBuilder(val context: Context) {
    private val dataFetchers = mutableMapOf<String, () -> String>()
    internal var onConfirmCallback: ((DialogResult, MyBuilder) -> Unit)? = null
    internal var confirmText = "确定"
    internal var cancelText = "取消"
    internal var showCancel = true
    internal val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun dpToPx(dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()

    private fun getThemeColor(attr: Int, default: Int = Color.BLACK): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return if (typedValue.type in TypedValue.TYPE_FIRST_COLOR_INT .. TypedValue.TYPE_LAST_COLOR_INT) {
            typedValue.data
        } else default
    }

    /**
     * 通用的字段包裹层，自带 Label 和间距
     */
    private fun addFieldContainer(label: String, child: android.view.View) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dpToPx(8f), 0, dpToPx(8f))
        }
        if (label.isNotEmpty()) {
            val labelView = TextView(context).apply {
                text = label
                textSize = 14f
                setTextColor(getThemeColor(android.R.attr.textColorSecondary, Color.GRAY))
                setPadding(dpToPx(4f), 0, 0, dpToPx(4f))
            }
            layout.addView(labelView)
        }
        layout.addView(child)
        container.addView(layout)
    }

    /**
     * 添加基础文本输入框
     */
    fun input(id: String, label: String, hint: String = "", prefill: String = "", inputType: Int = InputType.TYPE_CLASS_TEXT) {
        val editText = EditText(context).apply {
            this.hint = hint
            this.setText(prefill)
            this.inputType = inputType
            this.textSize = 16f
            this.setPadding(dpToPx(12f), dpToPx(12f), dpToPx(12f), dpToPx(12f))
            this.setTextColor(getThemeColor(android.R.attr.textColorPrimary, Color.BLACK))
            this.setHintTextColor(getThemeColor(android.R.attr.textColorHint, Color.GRAY))
            val isDarkMode = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            this.background = GradientDrawable().apply {
                cornerRadius = dpToPx(8f).toFloat()
                setColor(if (isDarkMode) "#1AFFFFFF".toColorInt() else "#0D000000".toColorInt())
            }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        addFieldContainer(label, editText)
        dataFetchers[id] = { editText.text.toString() }
    }

    /**
     * 添加密码输入框
     */
    fun password(id: String, label: String, hint: String = "", prefill: String = "") {
        input(id, label, hint, prefill, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
    }

    /**
     * 添加数字输入框
     */
    fun number(id: String, label: String, hint: String = "", prefill: String = "") {
        input(id, label, hint, prefill, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
    }

    /**
     * 添加单选下拉框 (采用极简的点击弹层，规避原生 Spinner 难看的问题)
     */
    fun select(id: String, label: String, options: List<String>, defaultIndex: Int = 0) {
        var currentIndex = if (defaultIndex in options.indices) defaultIndex else 0
        val valueView = TextView(context).apply {
            text = options.getOrNull(currentIndex) ?: ""
            textSize = 16f
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(12f), dpToPx(12f), dpToPx(12f), dpToPx(12f))
            setTextColor(getThemeColor(android.R.attr.textColorPrimary, Color.BLACK))
            val isDarkMode = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            this.background = GradientDrawable().apply {
                cornerRadius = dpToPx(8f).toFloat()
                setColor(if (isDarkMode) "#1AFFFFFF".toColorInt() else "#0D000000".toColorInt())
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle(label)
                    .setSingleChoiceItems(options.toTypedArray(), currentIndex) { dialog, which ->
                        currentIndex = which
                        this.text = options[which]
                        dialog.dismiss()
                    }
                    .show()
            }
        }
        addFieldContainer(label, valueView)
        dataFetchers[id] = { options.getOrNull(currentIndex) ?: "" }
    }

    /**
     * 配置确认按钮及其回调
     */
    fun onConfirm(text: String = "确定", action: (DialogResult, MyBuilder) -> Unit) {
        this.confirmText = text
        this.onConfirmCallback = action
    }

    /**
     * 隐藏取消按钮
     */
    fun hideCancel() {
        this.showCancel = false
    }

    internal fun buildResult() = DialogResult(dataFetchers.mapValues { it.value.invoke() })
}