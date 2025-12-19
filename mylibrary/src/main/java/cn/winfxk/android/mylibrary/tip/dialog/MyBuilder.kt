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
* Created Date: 2025/11/4  16:26 */
package cn.winfxk.android.mylibrary.tip.dialog

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.view.ImageView
import com.winfxk.lib.utils.toARGB
import androidx.core.view.isEmpty

typealias ClickListener = (Button, MyBuilder) -> Unit

@Suppress("unused")
@SuppressLint("InflateParams")
open class MyBuilder(context: Context, type: Type = Type.Info) : BaseBuilder(context) {
    protected open val progressBar: ProgressBar by lazy { findViewById(R.id.progressBar) }
    protected open val icon: ImageView by lazy { findViewById(R.id.imageView1) }
    protected open val titleView: TextView by lazy { findViewById(R.id.textView1) }
    protected open val messageView: TextView by lazy { findViewById(R.id.textView3) }
    protected open val buttonView: LinearLayout by lazy { findViewById(R.id.line2) }
    protected open val buttons = ArrayList<Button>();
    protected open val clickListener = ArrayList<ClickListener>();
    @Volatile protected open var lastSetMessageTime = 0L;
    /**
     * 设置下次点击按钮后不要关闭弹窗
     */
    @Volatile open var notClose = false;
    /**
     * 设置标题类型
     */
    open var type: Type = type
        set(value) {
            field = value
            runOnUI {
                val animDuration = 300L
                when (value) {
                    Type.Empty    -> {
                        ObjectAnimator.ofFloat(progressBar, "alpha", 0f).setDuration(animDuration).start()
                        ObjectAnimator.ofFloat(icon, "alpha", 0f).setDuration(animDuration).start()
                    }
                    Type.Progress -> {
                        ObjectAnimator.ofFloat(icon, "alpha", 0f).setDuration(animDuration).start()
                        ObjectAnimator.ofFloat(progressBar, "alpha", 1f).setDuration(animDuration).start()
                    }
                    else          -> {
                        ObjectAnimator.ofFloat(progressBar, "alpha", 0f).setDuration(animDuration).start()
                        if (icon.alpha < 0.1f) ObjectAnimator.ofFloat(icon, "alpha", 1f).setDuration(animDuration).start()
                        else icon.startAnimation(iconAnim)
                        icon.setImageResource(value.id)
                    }
                }
            }
        }

    /**
     * 设置弹窗正文文本
     */
    open var message: String
        get() = messageView.text.toString()
        @Synchronized
        set(value) {
            val time = System.currentTimeMillis();
            runOnUI {
                messageView.text = value;
                if (time - lastSetMessageTime > 500) messageView.startAnimation(alphaShow)
            }
            lastSetMessageTime = time;
        }
    /**
     * 设置标题文本
     */
    open var title: String
        get() = titleView.text.toString()
        set(value) {
            val time = System.currentTimeMillis();
            runOnUI {
                titleView.text = value
                if (time - lastSetMessageTime > 500) titleView.startAnimation(alphaShow)
            }
        }
    /**
     * 添加一个按钮
     */
    open fun addButton(text: String, @ColorInt color: Int? = null, listener: BuilderListener = emptyListener) {
        runOnUI {
            val button = makeButton(text, color);
            button.setOnClickListener {
                listener(this)
                clickListener.forEach { it.invoke(button, this) }
                if (! notClose) dismiss()
                notClose = false;
            }
            buttons.add(button);
            if (isShow) {
                buttonView.addView(button)
                button.startAnimation(alphaShow)
            }
        }
    }
    /**
     * 清空所有按钮
     */
    open fun clearButton() {
        buttons.clear()
        if (isShow) runOnUI {
            if (buttonView.isEmpty()) return@runOnUI
            buttonView.removeAllViews()
        }
    }
    /**
     * 根据文本内容获取一个按钮
     */
    open fun getButton(text: String) = buttons.firstOrNull { it.text == text }
    /**
     * 根据索引获取一个按钮
     */
    open fun getButton(index: Int) = if (buttons.size > index) buttons[index] else null;

    /**
     * 根据按钮文本内容删除一个按钮
     */
    fun removeButton(text: String?) {
        runOnUI {
            val buttonToRemove = buttons.firstOrNull { it.text == text }
            if (buttonToRemove != null) {
                buttons.remove(buttonToRemove)
                if (isShow) buttonView.removeView(buttonToRemove)
            }
        }
    }

    /**
     * 根据索引删除一个按钮
     *
     */
    fun removeButton(index: Int) {
        if (index >= buttons.size) return
        val button = buttons.removeAt(index)
        if (isShow) runOnUI { buttonView.removeView(button) }
    }
    /**
     * 用于构建一个点击按钮
     */
    protected open fun makeButton(text: String, color: Int?): Button {
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, context.resources.getDimensionPixelSize(R.dimen.winfxkliaDialogButtonSize), 1.0f)
        layoutParams.setMargins(0, 5, 0, 0)
        val button = Button(context, null)
        button.text = text
        button.setTextColor(color?.toARGB() ?: context.resources.getColor(R.color.winfxklia_dialogTextColor, context.theme))
        button.background = AppCompatResources.getDrawable(context, R.drawable.winfxklia_dialog_button)
        button.setLayoutParams(layoutParams)
        button.setPadding(0, 0, 0, 0)
        val stateListAnimator = StateListAnimator()
        stateListAnimator.addState(empIntarray, ObjectAnimator.ofFloat(button, "elevation", 0f))
        button.setStateListAnimator(stateListAnimator)
        return button
    }

    override fun getLayoutId(): Int = R.layout.winfxklia_mybuilder

    override fun initializeView() {
    }
    /**
     * 添加一个点击监听器
     */
    fun addClickListener(listener: ClickListener) {
        clickListener.add(listener);
    }
    @Synchronized
    override fun show() {
        runOnUI {
            buttons.forEach { buttonView.addView(it) }
            super.show()
        }
    }
    /**
     * 删除一个点击监听器
     */
    fun removeClickListener(listener: ClickListener) {
        clickListener.remove(listener)
    }

    companion object {
        val empIntarray = intArrayOf();
    }
}