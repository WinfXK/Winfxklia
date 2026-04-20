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
* Created Date: 2026/4/20  11:03 */
package cn.winfxk.android.mylibrary.tip.dialog.buttom

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView

class ButtomScrollView(ctx: Context, private val maxScrollHeight: Int) : ScrollView(ctx) {
    constructor(ctx: Context) : this(ctx, 0)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxSpec = MeasureSpec.makeMeasureSpec(maxScrollHeight, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, maxSpec)
    }

    fun load(root: LinearLayout) {
        isVerticalScrollBarEnabled = true
        layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        root.addView(this)
    }
}