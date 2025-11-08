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
* Created Date: 2025/11/4  16:21 */
package cn.winfxk.android.mylibrary.tip.dialog

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import cn.winfxk.android.mylibrary.R

abstract class EmptyBuilder(context: Context) : BaseBuilder(context, R.layout.winfxklia_emptybuilder) {
    private val rl: RelativeLayout by lazy { findViewById(R.id.line1) }

    fun setView(view: View) {
        rl.removeAllViews();
        rl.addView(view);
    }

}