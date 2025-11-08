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
* Created Date: 2025/11/05 14:03
*/
package cn.winfxk.android.mylibrary.tip

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import cn.winfxk.android.mylibrary.R


/**
 * @author Winfxk
 */
@Suppress("unused")
@SuppressLint("InflateParams")
class Toast(context: Context) {
    /**
     * 设置显示的图片1
     *
     * @param imageView
     */
    val imageView: ImageView by lazy { view.findViewById(R.id.imageView1) } //
    /**
     * 设置显示的图片2
     *
     * @param imageView1
     */
    val imageView1: ImageView by lazy { view.findViewById(R.id.imageView2) } //
    private val activity: Context = context //
    /**
     * 设置显示的文本视图
     *
     * @param textView
     */
    val textView: TextView by lazy { view.findViewById(R.id.textView1) } //
    /**
     * 获取显示的时长
     *
     * @return
     */
    var time: Int = 0 //
    /**
     * 设置显示的视图
     *
     * @param view
     */
    // 确保 view 是 lazy 初始化的，只创建一次
    val view: View by lazy { //
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.winfxklia_toast, null) //
    }

    /**
     *   显示文本
     * 这是替代 setView() 的核心逻辑
     */
    fun show() { //
        val currentActivity = getActivity(activity)
        if (currentActivity == null) {
            Log.e("WinfxkCustomToast", "Cannot show custom toast with a non-Activity context.")
            android.widget.Toast.makeText(activity, textView.text, time).show()
            return
        }
        (view.parent as? ViewGroup)?.removeView(view)
        val rootView = currentActivity.findViewById<FrameLayout>(android.R.id.content)
        if (rootView == null) {
            Log.e("WinfxkCustomToast", "Cannot find root view (android.R.id.content).")
            return
        }
        val durationMs = if (time == LENGTH_LONG) 3500L else 2000L
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            val bottomMarginPx = (100 * activity.resources.displayMetrics.density).toInt()
            bottomMargin = bottomMarginPx
        }
        rootView.addView(view, params)
        view.postDelayed({ if (view.parent != null) rootView.removeView(view) }, durationMs)
    }

    private fun getActivity(context: Context?): Activity? {
        return when (context) {
            is Activity       -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else              -> null
        }
    }

    /**
     * 设置显示的文本
     *
     * @param string
     */
    fun setText(string: String?) { //
        textView.text = string //
    }

    /**
     * 设置显示的图片
     *
     * @param drawable
     */
    fun setImage(drawable: Drawable?) { //
        imageView.setImageDrawable(drawable) //
        imageView1.setImageDrawable(drawable) //
    }

    /**
     * 设置显示的图片
     *
     * @param bm
     */
    fun setImage(bm: Bitmap?) { //
        imageView.setImageBitmap(bm) //
        imageView1.setImageBitmap(bm) //
    }

    /**
     * 设置显示的图片
     *
     * @param resId
     */
    fun setImage(resId: Int) { //
        imageView.setImageResource(resId) //
        imageView1.setImageResource(resId) //
    }

    /**
     * 设置显示的图片
     *
     * @param uri
     */
    fun setImage(uri: Uri?) { //
        imageView.setImageURI(uri) //
        imageView1.setImageURI(uri) //
    }

    /**
     * 设置随机字体颜色
     *
     * @param r
     * @param g
     * @param b
     */
    fun setTextColor(r: Int, g: Int, b: Int) { //
        val drawable = textView.background as GradientDrawable //
        drawable.setColor(Color.rgb(255 - r, 255 - g, 255 - b)) //
        textView.background = drawable //
        textView.setTextColor(Color.rgb(r, g, b)) //
    }

    /**
     * 设置随机字体颜色
     */
    fun setRandColor() { //
        setTextColor((0 .. 255).random(), (0 .. 255).random(), (0 .. 255).random()) //
    }

    /**
     * 设置随机字体背景颜色
     *
     * @param r
     * @param g
     * @param b
     */
    fun setTextBackgColor(r: Int, g: Int, b: Int) { //
        val drawable = textView.background as GradientDrawable //
        drawable.setColor(Color.rgb(r, g, b)) //
        textView.background = drawable //
        textView.setTextColor(Color.rgb(255 - r, 255 - g, 255 - b)) //
    }

    /**
     * 设置随机字体背景颜色
     */
    fun setRandBackgColor() { //
        setTextBackgColor((0 .. 255).random(), (0 .. 255).random(), (0 .. 255).random()) //
    }

    /**
     * 设置随机图片跟随
     */
    fun setRandImage() { //
        val r = Images1.random() //
        var r2 = Images1.random() //
        while (r == r2) r2 = Images1.random() //
        this.imageView.setImageResource(r) //
        this.imageView1.setImageResource(r2) //
    }

    companion object {
        val Images1: IntArray = intArrayOf(R.drawable.winfxklia_toast_icon1, R.drawable.winfxklia_toast_icon2, //
            R.drawable.winfxklia_toast_icon3, R.drawable.winfxklia_toast_icon4, //
            R.drawable.winfxklia_toast_icon5, R.drawable.winfxklia_toast_icon6, //
            R.drawable.winfxklia_toast_icon7, R.drawable.winfxklia_toast_icon8, //
            R.drawable.winfxklia_toast_icon9, R.drawable.winfxklia_toast_icon10, //
            R.drawable.winfxklia_toast_icon11, R.drawable.winfxklia_toast_icon12, //
            R.drawable.winfxklia_toast_icon13, R.drawable.winfxklia_toast_icon14, //
            R.drawable.winfxklia_toast_icon15, R.drawable.winfxklia_toast_icon16, //
            R.drawable.winfxklia_toast_icon17, R.drawable.winfxklia_toast_icon18, //
            R.drawable.winfxklia_toast_icon19, R.drawable.winfxklia_toast_icon20) //
        const val LENGTH_SHORT: Int = 0 //
        const val LENGTH_LONG: Int = 1 //
        /**
         * 快捷提示一个窗口
         *
         * @param context Activity对象
         * @param string  要提示的内容
         * @param i       **提示时间**
         * @return
         * @see .LENGTH_SHORT
         *
         * @see .LENGTH_LONG
         */
        @JvmStatic // 增加 JvmStatic 以便 Java 代码也能方便调用
        fun makeText(context: Context, string: Any?, i: Int): Toast { //
            val tsate = Toast(context) //
            tsate.setText(string.toString()) //
            tsate.time = i //
            tsate.setRandImage() //
            return tsate //
        }

        /**
         * 快捷显示一个提示
         *
         * @param context Activity对象
         * @param string  要显示的文本
         * @return
         */
        @JvmStatic // 增加 JvmStatic 以便 Java 代码也能方便调用
        fun makeText(context: Context, string: Any?): Toast { //
            return makeText(context, string.toString(), LENGTH_SHORT) //
        }
    }
}