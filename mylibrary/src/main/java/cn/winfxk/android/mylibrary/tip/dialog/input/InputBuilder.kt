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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toDrawable
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.tip.dialog.BaseBuilder
import cn.winfxk.android.mylibrary.tip.dialog.BuilderException
import cn.winfxk.android.mylibrary.tip.dialog.MyBuilder.Companion.empIntarray
import cn.winfxk.android.mylibrary.tip.dialog.Type
import cn.winfxk.android.mylibrary.utils.image.BitmapUtils // 导入你的 BitmapUtils
import cn.winfxk.android.mylibrary.view.ImageView
import com.google.android.material.textfield.TextInputLayout
import com.winfxk.lib.utils.toARGB
import java.util.concurrent.ConcurrentHashMap
import androidx.core.graphics.createBitmap

/**
 * 返回值决定是否关闭弹窗 true=关闭  false=不关闭
 */
typealias InputClickListener = () -> Boolean

class InputBuilder(context: Context) : BaseBuilder(context), InputClickListener {
    private val map = ConcurrentHashMap<String, InputView>();
    private val buttons = ArrayList<Button>();
    private val edits: LinearLayout by lazy { findViewById(R.id.line3) }
    private val bts: LinearLayout by lazy { findViewById(R.id.line4) }
    private val icon: ImageView by lazy { findViewById(R.id.imageView1) }
    private val titleView: TextView by lazy { findViewById(R.id.textView1) }
    private val messageView: TextView by lazy { findViewById(R.id.textView3) }
    override fun getLayoutId(): Int = R.layout.winfxklia_inputbuilder
    private val clickListener = ArrayList<InputClickListener>();

    /**
     *  获取 Material 库定义的标准图标大小 (默认为 24dp)
     * 我们将把 Bitmap 缩放到这个尺寸
     */
    private val iconSize: Int by lazy { context.resources.getDimensionPixelSize(R.dimen.winfxkliaDesigntextInputIconSizeFallback) }

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
                    Type.Empty    -> ObjectAnimator.ofFloat(icon, "alpha", 0f).setDuration(animDuration).start()
                    Type.Progress -> ObjectAnimator.ofFloat(icon, "alpha", 0f).setDuration(animDuration).start()
                    else          -> {
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
            runOnUI { titleView.text = value }
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
    fun add(view: InputView): InputView {
        if (map.containsKey(view.key)) throw BuilderException("不能添加两个Key重复的输入框！")
        map[view.key] = view;
        return view;
    }

    /**
     * 添加一个输入框
     *
     * @param key 输入框的唯一ID
     * @param hint 输入框的Hint
     * @param text 输入框前面需要显示的文本 (可选)
     * @return 构建的视图
     */
    fun add(key: String, hint: String, text: String? = null): InputView = setupInternal(key, hint, text, null)
    /**
     * 添加一个输入框 (DrawableRes)
     *  使用高效的 scaleBitmapToIconSize
     */
    fun add(key: String, hint: String, @DrawableRes icon: Int, text: String? = null): InputView = setupInternal(key, hint, text) {
        val drawable = AppCompatResources.getDrawable(context, icon)
        if (drawable is BitmapDrawable) it.startIconDrawable = scaleBitmapToIconSize(drawable.bitmap)
        else if (drawable != null) it.startIconDrawable = scaleBitmapToIconSize(BitmapUtils.drawableToBitmap(drawable))
    }

    /**
     * 添加一个输入框 (Bitmap)
     *  使用高效的 scaleBitmapToIconSize
     */
    fun add(key: String, hint: String, icon: Bitmap, text: String? = null): InputView = setupInternal(key, hint, text) {
        it.startIconDrawable = scaleBitmapToIconSize(icon)
    }

    /**
     * 辅助方法：将 Bitmap 缩放并居中到一个固定大小 (iconSize) 的 Drawable 中
     * 这模拟了 ImageView.ScaleType.FIT_CENTER，但只创建 *一个* Bitmap。
     */
    private fun scaleBitmapToIconSize(bitmap: Bitmap): BitmapDrawable {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        if (originalWidth <= 0 || originalHeight <= 0) return bitmap.toDrawable(context.resources)
        val scale = (iconSize.toFloat() / originalWidth).coerceAtMost(iconSize.toFloat() / originalHeight)
        val newWidth = (originalWidth * scale).toInt()
        val newHeight = (originalHeight * scale).toInt()
        val left = (iconSize - newWidth) / 2f
        val top = (iconSize - newHeight) / 2f
        val targetRect = RectF(left, top, left + newWidth, top + newHeight)
        val finalBitmap = createBitmap(iconSize, iconSize)
        val canvas = Canvas(finalBitmap)
        canvas.drawBitmap(bitmap, null, targetRect, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
        return finalBitmap.toDrawable(context.resources)
    }

    private fun setupInternal(
        key: String,
        hint: String,
        text: String?,
        iconSetter: ((TextInputLayout) -> Unit)?
    ): InputView {
        val view = InputView(key, this)
        view.textInputLayout.hint = hint
        if (text.isNullOrBlank()) view.textInputLayout.prefixText = null
        else view.textInputLayout.prefixText = text
        if (iconSetter != null) iconSetter(view.textInputLayout)
        else view.textInputLayout.startIconDrawable = null
        return add(view)
    }
    /**
     * 添加一个按钮
     */
    fun addButton(text: String, @ColorInt color: Int = 0x000000, listener: InputClickListener = this) {
        runOnUI {
            val button = makeButton(text, color);
            button.setOnClickListener {
                val isClose = listener()
                clickListener.forEach { it.invoke() }
                if (isClose) dismiss()
            }
            buttons.add(button);
            if (isShow) {
                button.visibility = View.INVISIBLE
                bts.addView(button)
                handler.postDelayed({
                    button.visibility = View.VISIBLE
                    button.startAnimation(alphaShow)
                }, 100)
            }
        }
    }
    /**
     * 用于构建一个点击按钮
     */
    fun makeButton(text: String, color: Int): Button {
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, context.resources.getDimensionPixelSize(R.dimen.winfxkliaDialogButtonSize), 1.0f)
        layoutParams.setMargins(0, 5, 0, 0)
        val button = Button(context, null)
        button.text = text
        button.setTextColor(color.toARGB())
        button.background = AppCompatResources.getDrawable(context, R.drawable.winfxklia_dialog_button)
        button.setLayoutParams(layoutParams)
        button.setPadding(0, 0, 0, 0)
        val stateListAnimator = StateListAnimator()
        stateListAnimator.addState(empIntarray, ObjectAnimator.ofFloat(button, "elevation", 0f))
        button.setStateListAnimator(stateListAnimator)
        return button
    }

    override fun invoke(): Boolean = true
}