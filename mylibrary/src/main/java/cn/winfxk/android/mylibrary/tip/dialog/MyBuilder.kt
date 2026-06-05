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
* Created Date: 2026/06/02 17:07 */
package cn.winfxk.android.mylibrary.tip.dialog

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.databinding.WinfxkliaDialogSmartBinding

typealias BuilderListener = DialogButton.() -> Unit

/**
 * 现代化的智能弹窗 (基于组合模式，拒绝 API 污染)
 */
class MyBuilder(val context: Context, builder: MyBuilder.() -> Unit = {}) {
    private val handler = Handler(Looper.getMainLooper())
    private val dialog = AppCompatDialog(context).apply { supportRequestWindowFeature(Window.FEATURE_NO_TITLE) }
    private val binding: WinfxkliaDialogSmartBinding by lazy { WinfxkliaDialogSmartBinding.inflate(LayoutInflater.from(context)) }
    private val buttons = mutableListOf<DialogButton>()
    val isShowing: Boolean get() = dialog.isShowing

    /**
     * 弹窗宽度占屏幕宽度的比例 (1.0f 为满宽无边距)
     */
    @Volatile
    var dialogWidthRatio: Float = 1.0f
        set(value) {
            field = value
            runOnUI { updateWindowWidth() }
        }

    @Volatile
    var type: DialogType = DialogType.Message
        set(value) {
            field = value
            runOnUI { updateIconState() }
        }

    @Volatile
    var title: CharSequence = ""
        set(value) {
            field = value
            runOnUI {
                binding.title.text = value
                binding.title.isVisible = value.isNotEmpty()
            }
        }

    @Volatile
    var message: CharSequence = ""
        set(value) {
            field = value
            runOnUI {
                binding.message.text = value
                binding.message.isVisible = value.isNotEmpty()
            }
        }


    init {
        title = "提示"
        dialog.setContentView(binding.root)
        setCancelable(false)
        updateWindowWidth()
        initTransitions()
        builder(this)
        applyData()
    }

    private fun updateWindowWidth() {
        dialog.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setLayout((context.resources.displayMetrics.widthPixels * dialogWidthRatio).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
            setWindowAnimations(android.R.style.Animation_Dialog)
        }
    }

    private fun initTransitions() {
        val layoutTransition = LayoutTransition()
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.dialogRoot.layoutTransition = layoutTransition
    }

    private fun applyData() {
        this.title = title
        this.message = message
        this.type = type
        renderButtons()
    }

    private fun updateIconState() {
        when (type) {
            DialogType.Loading -> {
                binding.icon.isVisible = false
                binding.progressBar.isVisible = true
            }
            DialogType.Empty   -> {
                binding.icon.isVisible = false
                binding.progressBar.isVisible = false
            }
            else               -> {
                binding.progressBar.isVisible = false
                binding.icon.isVisible = true
                type.iconRes?.let { binding.icon.setImageResource(it) }
            }
        }
    }

    private fun runOnUI(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) action() else handler.post(action)
    }

    /**
     * 显示弹窗
     */
    fun show() {
        if (! isShowing) runOnUI {
            renderButtons()
            dialog.show()
        }
    }

    /**
     * 关闭弹窗
     */
    fun dismiss() {
        if (isShowing) runOnUI { dialog.dismiss() }
    }

    /**
     * 设置是否可取消
     */
    fun setCancelable(cancelable: Boolean) {
        dialog.setCancelable(cancelable)
    }

    /**
     * 添加按钮
     */
    fun addButton(text: String, block: BuilderListener = emptyListener) {
        val config = DialogButton(text).apply(block)
        buttons.add(config)
        if (isShowing) runOnUI { renderButtons() }
    }

    /**
     * 清空按钮
     */
    fun clearButtons() {
        buttons.clear()
        if (isShowing) runOnUI { renderButtons() }
    }

    /**
     * 获取自定义容器
     */
    fun getCustomContainer() = binding.view

    /**
     * 清空当前所有自定义视图
     */
    fun clearCustomView() {
        runOnUI { binding.view.removeAllViews() }
    }

    /**
     * 动态添加自定义视图
     */
    fun addCustomView(view: View) {
        runOnUI { binding.view.addView(view) }
    }

    /**
     * 构建单个按钮的 View
     */
    private fun createButtonView(config: DialogButton): TextView {
        return TextView(context).apply {
            text = config.text
            textSize = 16f
            gravity = Gravity.CENTER
            val defaultTextColor = context.getColor(R.color.winfxklia_textColor1)
            setTextColor(config.textColor ?: defaultTextColor)
            val bgDrawable = GradientDrawable().apply {
                cornerRadius = dpToPx(8f)
                setColor(config.backgroundColor ?: getThemeColor(default = "#1A000000".toColorInt()))
            }
            background = bgDrawable
            setPadding(dpToPx(16f).toInt(), dpToPx(12f).toInt(), dpToPx(16f).toInt(), dpToPx(12f).toInt())
            setOnClickListener {
                config.onClick?.invoke(this@MyBuilder)
                if (config.isDismissOnClick) dismiss()
            }
        }
    }

    /**
     * 智能渲染按钮布局
     */
    private fun renderButtons() {
        binding.buttonView.removeAllViews()
        if (buttons.isEmpty()) {
            binding.buttonView.isVisible = false
            return
        }
        binding.buttonView.isVisible = true
        if (buttons.size <= 2) {
            binding.buttonView.orientation = LinearLayout.HORIZONTAL
            buttons.forEachIndexed { index, config ->
                val btn = createButtonView(config)
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                if (index > 0) params.marginStart = dpToPx(12f).toInt()
                binding.buttonView.addView(btn, params)
            }
        } else {
            val flowLayout = DialogFlowLayout(context).apply {
                itemSpacing = dpToPx(12f).toInt()
                lineSpacing = dpToPx(12f).toInt()
            }
            buttons.forEach { config ->
                val btn = createButtonView(config)
                flowLayout.addView(btn, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
            binding.buttonView.addView(flowLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun getThemeColor(attr: Int = android.R.attr.colorButtonNormal, default: Int = Color.BLACK): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return if (typedValue.type in TypedValue.TYPE_FIRST_COLOR_INT .. TypedValue.TYPE_LAST_COLOR_INT) typedValue.data else default
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }

    companion object {
        val emptyListener: BuilderListener = {}
    }
}