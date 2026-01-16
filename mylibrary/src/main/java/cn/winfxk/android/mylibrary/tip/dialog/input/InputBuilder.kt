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
* Web: http://winfxk.com
* Created Date: 2025/11/08 17:00
*/
package cn.winfxk.android.mylibrary.tip.dialog.input

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.tip.dialog.BaseBuilder
import cn.winfxk.android.mylibrary.tip.dialog.BuilderException
import cn.winfxk.android.mylibrary.tip.dialog.MyBuilder.Companion.empIntarray
import cn.winfxk.android.mylibrary.tip.dialog.Type
import cn.winfxk.android.mylibrary.view.ImageView
import com.winfxk.lib.sid.SnowflakeID
import com.winfxk.lib.utils.toARGB
import java.util.concurrent.ConcurrentHashMap

/**
 * 返回值决定是否关闭弹窗 true=关闭  false=不关闭
 */
typealias InputClickListener = (Boolean) -> Boolean

class InputBuilder(context: Context) : BaseBuilder(context), InputClickListener {
    private val map = ConcurrentHashMap<String, InputView>();
    private val buttons = ArrayList<Button>();
    private val edits: LinearLayout by lazy { findViewById(R.id.line3) }
    private val bts: LinearLayout by lazy { findViewById(R.id.line4) }
    private val icon: ImageView by lazy { findViewById(R.id.imageView1) }
    private val titleView: TextView by lazy { findViewById(R.id.textView1) }
    private val messageView: TextView by lazy { findViewById(R.id.textView3) }
    private val customViews = ConcurrentHashMap<String, CustomInputView>()
    override fun getLayoutId(): Int = R.layout.winfxklia_inputbuilder

    @Volatile private var lastSetMessageTime = 0L;
    /**
     * 设置标题类型
     */
    var type: Type = Type.Input
        set(value) {
            field = value
            runOnUI {
                val animDuration = 300L
                when (value) {
                    Type.Empty    -> ObjectAnimator.ofFloat(icon, "alpha", 0f)
                        .setDuration(animDuration)
                        .start()
                    Type.Progress -> ObjectAnimator.ofFloat(icon, "alpha", 0f)
                        .setDuration(animDuration)
                        .start()
                    else          -> {
                        if (icon.alpha < 0.1f) ObjectAnimator.ofFloat(icon, "alpha", 1f)
                            .setDuration(animDuration)
                            .start()
                        else icon.startAnimation(iconAnim)
                        icon.setImageResource(value.id)
                    }
                }
            }
        }

    /**
     * 设置弹窗正文文本
     */
    var message: String
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
    var title: String
        get() = titleView.text.toString()
        set(value) {
            val time = System.currentTimeMillis();
            runOnUI {
                titleView.text = value
                if (time - lastSetMessageTime > 500) titleView.startAnimation(alphaShow)
            }
        }

    override fun initializeView() {
    }

    override fun show() {
        map.values.forEach { edits.addView(it.view) }
        buttons.forEach { bts.addView(it) }
        super.show()
    }
    /**
     * 根据Key，提取编辑框里面的文本
     */
    fun getValue(key: String): String? = map[key]?.editText?.text?.toString();
    /**
     * 添加一个输入框
     */
    private fun add(view: InputView): InputView {
        if (map.containsKey(view.key)) throw BuilderException("不能添加两个Key重复的输入框！")
        map[view.key] = view;
        if (isShow) edits.addView(view.view)
        return view;
    }
    /**
     * 添加自定义项目
     */
    fun addView(builder: CustomInputView.() -> Unit) {
        val view = CustomInputView();
        builder.invoke(view);
        customViews[sid.nextKey()] = view;
        edits.addView(view.view)
    }

    fun add(builder: InputItem.() -> Unit): InputItem {
        val view = InputView(sid.nextKey(), this)
        builder.invoke(view.item);
        add(view)
        return view.item;
    }
    /**
     * 添加一个按钮
     */
    fun addButton(text: String, @ColorInt color: Int? = null, listener: InputClickListener = this) {
        runOnUI {
            val button = makeButton(text, color);
            button.setOnClickListener {
                val clicklist = map.values;
                val customViews = customViews.values
                val isClose = listener((clicklist.isEmpty() || clicklist.any {
                    val onClick = it.onClick;
                    if (onClick == null) true else onClick(it.item, button)
                }) && (customViews.isEmpty() || customViews.any {
                    val onClick = it.onClick;
                    if (onClick == null) true else  onClick(it, button)
                }))
                if (isClose) dismiss()
            }
            buttons.add(button);
            if (isShow) {
                bts.addView(button)
                button.startAnimation(alphaShow)
            }
        }
    }
    /**
     * 用于构建一个点击按钮
     */
    fun makeButton(text: String, color: Int?): Button {
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

    override fun invoke(isSuccess: Boolean): Boolean = true

    companion object {
        private val sid by lazy { SnowflakeID(1, 1) }
    }
}