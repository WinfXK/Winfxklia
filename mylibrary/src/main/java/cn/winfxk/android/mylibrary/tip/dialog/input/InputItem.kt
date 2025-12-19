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
* Created Date: 2025/12/17  15:54 */
package cn.winfxk.android.mylibrary.tip.dialog.input

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.widget.Button
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.utils.image.BitmapUtils

typealias onInputItemClick = InputItem.(Button) -> Boolean

class InputItem(val view: InputView, val context: Context) {
    private val iconSize: Int by lazy { context.resources.getDimensionPixelSize(R.dimen.winfxkliaDesigntextInputIconSizeFallback) }
    /**
     * 输入框的Hint
     */
    var hint: String
        set(value) {
            view.textInputLayout.hint = value
        }
        get() = view.textInputLayout.hint.toString()
    /**
     * 输入框前面显示的文本
     */
    var title: String?
        set(value) {
            if (value.isNullOrBlank()) view.textInputLayout.prefixText = null
            else view.textInputLayout.prefixText = value
        }
        get() = view.textInputLayout.prefixText.toString()
    /**
     * 要显示在输入框前面的图标
     */
    @setparam:DrawableRes var iconRes: Int
        set(value) {
            val it = view.textInputLayout;
            val drawable = AppCompatResources.getDrawable(context, value)
            if (drawable is BitmapDrawable) it.startIconDrawable = scaleBitmapToIconSize(drawable.bitmap)
            else if (drawable != null) it.startIconDrawable = scaleBitmapToIconSize(BitmapUtils.drawableToBitmap(drawable))
        }
        get() = 0

    /**
     * 输入框内的文字
     */
    var text: String
        get() = view.editText.text.toString()
        set(value) {
            view.editText.setText(value)
        }
    /**
     * 要显示在输入框前面的图标
     */
    var icon: Bitmap?
        get() = null
        set(value) {
            if (value != null) view.textInputLayout.startIconDrawable = scaleBitmapToIconSize(value)
        }
    /**
     * 点击按钮的事件
     */
    var onClick: onInputItemClick?
        get() = view.onClick
        set(value) {
            view.onClick = value;
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


}