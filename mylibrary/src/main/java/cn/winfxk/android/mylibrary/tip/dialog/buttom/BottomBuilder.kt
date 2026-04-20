/* * Copyright Notice
* © [2024 - 2026] Winfxk. All rights reserved.
* The software, its source code, and all related documentation are the intellectual property of Winfxk. Any reproduction or distribution of this software or any part thereof must be clearly attributed to Winfxk and the original author. Unauthorized copying, reproduction, or distribution without proper attribution is strictly prohibited.
* For inquiries, support, or to request permission for use, please contact us at:
* Email: admin@winfxk.cn
* QQ: 2508543202
* Visit our homepage for more information: http://Winfxk.cn
* * --------- Create message ---------
* Created by IntelliJ ID
* Author： Winfxk
* Web: http://winfxk.com
* Created Date: 2026/04/20 15:14
*/
package cn.winfxk.android.mylibrary.tip.dialog.buttom

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentActivity
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

@Suppress("UNUSED")
class BottomBuilder(private val activity: FragmentActivity) : BottomSheetDialogFragment() {
    private val buttons = mutableListOf<DialogButton>()
    private var titleView: TextView? = null
    private var messageView: TextView? = null
    private var customContainer: LinearLayout? = null
    private var buttonContainer: SmartFlowLayout? = null
    private val dp150 by lazy { dp2px(150) }
    private val dp38 by lazy { dp2px(38) }
    private val dp24 by lazy { dp2px(24) }
    private val dp20 by lazy { dp2px(20) }
    private val dp16 by lazy { dp2px(16) }
    private val dp10 by lazy { dp2px(10) }
    private val dp8 by lazy { dp2px(8) }
    private val dp6 by lazy { dp2px(6) }
    private val dp4 by lazy { dp2px(4) }
    private val dp2 by lazy { dp2px(2) }

    /**
     * 弹窗标题
     */
    var title: String = ""
        set(value) {
            field = value
            titleView?.text = value
            titleView?.visibility = if (value.isBlank()) View.GONE else View.VISIBLE
        }

    /**
     * 弹窗的文本内容
     */
    var message: String = ""
        set(value) {
            field = value
            messageView?.text = value
            messageView?.visibility = if (value.isBlank()) View.GONE else View.VISIBLE
            refreshCustomView()
        }

    /**
     * 弹窗的自定义 View 属性
     */
    var customView: View? = null
        set(value) {
            field = value
            refreshCustomView()
        }

    /**
     *  设置自定义 View
     */
    fun setView(view: View) {
        this.customView = view
    }

    /**
     * 控制是否可以通过下滑关闭弹窗。
     */
    var isSwipeToDismissEnabled: Boolean = false
        set(value) {
            field = value
            updateBehavior()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    /**
     * 新增一个按钮
     * @param text 按钮显示的文字
     * @param textColor 字体颜色，默认黑色
     * @param underline 是否在文本下方绘制下划线
     * @param onClick 点击事件回调
     */
    fun addButton(
        text: String,
        textColor: Int = Color.BLACK,
        underline: Boolean = false,
        onClick: () -> Unit = empOnClick
    ) {
        buttons.add(DialogButton(text, textColor, underline, onClick))
        refreshButtons()
    }

    /**
     * 根据按钮的文本删除按钮
     * @param text 按钮的文本内容
     */
    fun removeButton(text: String) {
        val initialSize = buttons.size
        buttons.removeAll { it.text == text }
        if (buttons.size != initialSize) refreshButtons()
    }

    /**
     * 根据按钮索引删除按钮
     * @param index 按钮的索引
     */
    fun removeButton(index: Int) {
        if (index in buttons.indices) {
            buttons.removeAt(index)
            refreshButtons()
        }
    }

    /**
     * 清空所有按钮
     */
    fun clearButtons() {
        buttons.clear()
        refreshButtons()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.apply {
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(this, false)
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            if (android.os.Build.VERSION.SDK_INT < 35) {
                @Suppress("DEPRECATION")
                statusBarColor = Color.TRANSPARENT
            }
        }
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                sheet.setBackgroundColor(Color.TRANSPARENT)
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.isHideable = isSwipeToDismissEnabled
                behavior.isFitToContents = true
            }
        }
        return dialog
    }

    private fun updateBehavior() {
        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.isHideable = isSwipeToDismissEnabled
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val ctx = requireContext()
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(dp24, dp16, dp24, dp24)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.WHITE)
                cornerRadii = floatArrayOf(
                    dp24.toFloat(), dp24.toFloat(),
                    dp24.toFloat(), dp24.toFloat(),
                    0f, 0f, 0f, 0f
                )
            }
        }
        val dragHandle = View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp38, dp4).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp20
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp20.toFloat()
                setColor("#E0E0E0".toColorInt())
            }
        }
        root.addView(dragHandle)
        titleView = TextView(ctx).apply {
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, dp16)
            text = title
            visibility = if (title.isBlank()) View.GONE else View.VISIBLE
            root.addView(this)
        }
        val scrollView = SmartNestedScrollView(ctx).apply {
            isVerticalScrollBarEnabled = true
            layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            root.addView(this)
        }
        val scrollContent = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            scrollView.addView(this)
        }
        messageView = TextView(ctx).apply {
            textSize = 14f
            setTextColor("#555555".toColorInt())
            setLineSpacing(dp6.toFloat(), 1f)
            text = message
            visibility = if (message.isBlank()) View.GONE else View.VISIBLE
            scrollContent.addView(this)
        }
        customContainer = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            scrollContent.addView(this)
        }
        refreshCustomView()
        buttonContainer = SmartFlowLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp20
            }
            root.addView(this)
        }
        refreshButtons()
        return root
    }

    private fun refreshCustomView() {
        val container = customContainer ?: return
        container.removeAllViews()
        val view = customView
        if (view != null) {
            (view.parent as? ViewGroup)?.removeView(view)
            container.addView(view)
            container.visibility = View.VISIBLE
            val lp = container.layoutParams as? LinearLayout.LayoutParams
            lp?.topMargin = if (message.isNotBlank()) dp16 else 0
            container.layoutParams = lp
        } else container.visibility = View.GONE
    }

    private fun refreshButtons() {
        val container = buttonContainer ?: return
        val ctx = context ?: return
        container.removeAllViews()
        if (buttons.isEmpty()) {
            container.visibility = View.GONE
            return
        }
        container.visibility = View.VISIBLE
        buttons.forEach { btnData ->
            val button = TextView(ctx).apply {
                text = btnData.text
                setTextColor(btnData.textColor)
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(dp16, dp10, dp16, dp10)
                if (btnData.underline) paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                setOnClickListener { btnData.onClick() }
            }
            applyPremiumTouchEffect(button)
            container.addView(button)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun applyPremiumTouchEffect(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN                          -> {
                    v.animate().scaleX(0.92f).scaleY(0.92f).alpha(0.6f).setDuration(150).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(250).start()
                }
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        titleView = null
        messageView = null
        customContainer = null
        buttonContainer = null
    }

    fun show() = show(activity.supportFragmentManager, "BottomBuilderDialog")

    internal fun dp2px(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private inner class SmartFlowLayout(context: Context) : ViewGroup(context) {
        private val itemHorizontalSpacing = dp8
        private val itemVerticalSpacing = dp8
        private val lines = mutableListOf<List<View>>()
        private val lineHeights = mutableListOf<Int>()
        private val lineWidths = mutableListOf<Int>()

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            lines.clear()
            lineHeights.clear()
            lineWidths.clear()
            val widthSize = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
            var currentLine = mutableListOf<View>()
            var currentLineWidth = 0
            var currentLineHeight = 0
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child.isGone) continue
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                if (currentLineWidth + childWidth > widthSize && currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                    lineHeights.add(currentLineHeight)
                    lineWidths.add(currentLineWidth - itemHorizontalSpacing)
                    currentLine = mutableListOf()
                    currentLineWidth = 0
                    currentLineHeight = 0
                }
                currentLine.add(child)
                currentLineWidth += childWidth + itemHorizontalSpacing
                currentLineHeight = maxOf(currentLineHeight, childHeight)
            }
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
                lineHeights.add(currentLineHeight)
                lineWidths.add(currentLineWidth - itemHorizontalSpacing)
            }
            val totalHeight = paddingTop + paddingBottom + lineHeights.sum() + maxOf(0, lines.size - 1) * itemVerticalSpacing
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), totalHeight)
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            var currentY = paddingTop
            val width = r - l
            for (i in 0 until lines.size) {
                val line = lines[i]
                val lineHeight = lineHeights[i]
                val lineWidth = lineWidths[i]
                var currentX = width - paddingRight - lineWidth
                for (child in line) {
                    val childWidth = child.measuredWidth
                    val childHeight = child.measuredHeight
                    val topOffset = (lineHeight - childHeight) / 2
                    child.layout(currentX, currentY + topOffset, currentX + childWidth, currentY + topOffset + childHeight)
                    currentX += childWidth + itemHorizontalSpacing
                }
                currentY += lineHeight + itemVerticalSpacing
            }
        }
    }

    private inner class SmartNestedScrollView(context: Context) : NestedScrollView(context) {
        @SuppressLint("DrawAllocation")
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val realScreenHeight = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                windowManager.currentWindowMetrics.bounds.height()
            } else {
                val displayMetrics = android.util.DisplayMetrics()
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealMetrics(displayMetrics)
                displayMetrics.heightPixels
            }
            var staticHeight = 0
            val parentGroup = parent as? ViewGroup
            if (parentGroup != null) {
                staticHeight += parentGroup.paddingTop + parentGroup.paddingBottom
                for (i in 0 until parentGroup.childCount) {
                    val child = parentGroup.getChildAt(i)
                    if (child !== this && ! child.isGone) {
                        child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                        staticHeight += child.measuredHeight
                        val lp = child.layoutParams as? MarginLayoutParams
                        if (lp != null) staticHeight += lp.topMargin + lp.bottomMargin
                    }
                }
            } else staticHeight = dp150
            val maxHeight = if (realScreenHeight > staticHeight) realScreenHeight - staticHeight + dp2 else 500
            val maxSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
            super.onMeasure(widthMeasureSpec, maxSpec)
        }
    }

    companion object {
        private val empOnClick: () -> Unit = {}
    }
}