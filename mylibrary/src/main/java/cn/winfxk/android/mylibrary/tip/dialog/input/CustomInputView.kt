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
* Created Date: 2026/1/9  14:41 */
package cn.winfxk.android.mylibrary.tip.dialog.input

import android.view.View
import android.widget.Button
/**
 * @return 是否关闭Dialog
 */
typealias onCustomInputItemClick = CustomInputView.(Button) -> Boolean

class CustomInputView {
    lateinit var view: View;
    var onClick: onCustomInputItemClick? = null
}