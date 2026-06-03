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
* Created Date: 2026/6/3  08:07 */
package cn.winfxk.android.mylibrary.tip.dialog

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isGone
import kotlin.math.max

/**
 * 轻量级流式布局 (用于自动换行排列无限数量的按钮)
 */
internal class DialogFlowLayout(context: Context) : ViewGroup(context) {
    var itemSpacing = 0
    var lineSpacing = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var currentWidth = paddingLeft
        var currentHeight = paddingTop
        var maxLineHeight = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isGone) continue
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            if (currentWidth + child.measuredWidth + paddingRight > widthSize) {
                currentWidth = paddingLeft
                currentHeight += maxLineHeight + lineSpacing
                maxLineHeight = 0
            }
            maxLineHeight = max(maxLineHeight, child.measuredHeight)
            currentWidth += child.measuredWidth + itemSpacing
        }
        val totalHeight = currentHeight + maxLineHeight + paddingBottom
        setMeasuredDimension(widthSize, resolveSize(totalHeight, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var currentWidth = paddingLeft
        var currentHeight = paddingTop
        var maxLineHeight = 0
        val width = r - l
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isGone) continue
            if (currentWidth + child.measuredWidth + paddingRight > width) {
                currentWidth = paddingLeft
                currentHeight += maxLineHeight + lineSpacing
                maxLineHeight = 0
            }
            child.layout(currentWidth, currentHeight, currentWidth + child.measuredWidth, currentHeight + child.measuredHeight)
            maxLineHeight = max(maxLineHeight, child.measuredHeight)
            currentWidth += child.measuredWidth + itemSpacing
        }
    }
}