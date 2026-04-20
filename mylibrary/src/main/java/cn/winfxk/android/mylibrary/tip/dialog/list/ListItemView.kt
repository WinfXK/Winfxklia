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
* Created Date: 2025/12/16  15:01 */
package cn.winfxk.android.mylibrary.tip.dialog.list

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.utils.dp
import cn.winfxk.android.mylibrary.utils.sp
import com.winfxk.lib.utils.toARGB

typealias OnListBuilderButtonClick = ListItemView.() -> Unit

class ListItemView(private val context: Context) : View.OnClickListener {
    val button by lazy { createButton() }
    /**
     * 按钮文本
     */
    var text: String
        get() = button.text.toString()
        set(value) {
            button.text = value
        }
    /**
     * 按钮文本大小
     */
    var textSize: Float
        get() = button.textSize
        set(value) {
            button.textSize = value
        }
    /**
     * 按钮文本颜色
     */
    @get:ColorInt
    @setparam:ColorInt
    var textColor: Int
        get() = button.textColors.defaultColor
        set(value) {
            button.setTextColor(value.toARGB())
        }
    /**
     * 按钮背景
     */
    var background: Drawable
        get() = button.background
        set(value) {
            button.background = value
        }
    /**
     * 按钮背景颜色
     */
    @get:ColorInt
    @setparam:ColorInt
    var backgroundColor: Int
        get() = (button.background as? android.graphics.drawable.ColorDrawable)?.color ?: 0
        set(value) {
            val normalColor = value.toARGB()
            val pressedColor = android.graphics.Color.argb(
                android.graphics.Color.alpha(normalColor),
                (android.graphics.Color.red(normalColor) * 0.9f).toInt(),
                (android.graphics.Color.green(normalColor) * 0.9f).toInt(),
                (android.graphics.Color.blue(normalColor) * 0.9f).toInt()
            )
            val normalDrawable = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 10f.dp
                setColor(normalColor)
            }
            val pressedDrawable = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 10f.dp
                setColor(pressedColor)
            }
            val stateListDrawable = android.graphics.drawable.StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
                addState(intArrayOf(), normalDrawable)
            }
            button.background = stateListDrawable
        }
    /**
     * 点击事件
     */
    var onClick: OnListBuilderButtonClick? = null

    private fun createButton(): Button {
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
        layoutParams.setMargins(0, padding, 0, 1)
        val button = Button(context)
        button.isAllCaps = false
        button.text = ""
        button.setTextColor(0x3333FF.toARGB())
        button.textSize = 9f.sp
        button.typeface = Typeface.DEFAULT_BOLD
        button.background = AppCompatResources.getDrawable(context, R.drawable.winfxklia_listdialog_button)
        button.setLayoutParams(layoutParams)
        button.setHeight(height)
        button.setOnClickListener(this)
        button.setPadding(0, 0, 0, 0)
        return button
    }

    override fun onClick(v: View?) {
        onClick?.invoke(this)
    }

    companion object {
        private val height by lazy { 50.dp };
        private val padding by lazy { 10.dp }
    }
}