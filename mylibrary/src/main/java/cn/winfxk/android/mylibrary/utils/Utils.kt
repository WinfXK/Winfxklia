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
* Created Date: 2025/12/16  14:07 */
package cn.winfxk.android.mylibrary.utils

import android.util.TypedValue
import cn.winfxk.android.mylibrary.BaseActivity.Companion.resources

val Int.dp: Int
    get() {
        val res = resources ?: return 0;
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), res.displayMetrics).toInt()
    }

val Float.dp: Float
    get() {
        val res = resources ?: return 0.0F;
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, res.displayMetrics)
    }

val Int.sp: Float
    get() {
        val res = resources ?: return 0f
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.toFloat(),
            res.displayMetrics
        )
    }

val Float.sp: Float
    get() {
        val res = resources ?: return 0f
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            res.displayMetrics
        )
    }
