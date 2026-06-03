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
* Created Date: 2026/6/3  08:13 */
package cn.winfxk.android.mylibrary.tip.dialog

import androidx.annotation.ColorInt


/**
 * 按钮配置 DSL 模型
 */
class DialogButton(var text: String = "") {
    @ColorInt var textColor: Int? = null
    @ColorInt var backgroundColor: Int? = null
    var isDismissOnClick: Boolean = true
    var onClick: ((MyBuilder) -> Unit)? = null
    fun onClick(action: (MyBuilder) -> Unit) {
        this.onClick = action
    }
}
