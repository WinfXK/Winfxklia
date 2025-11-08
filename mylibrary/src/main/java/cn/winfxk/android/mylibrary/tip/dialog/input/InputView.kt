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
* Created Date: 2025/11/8  11:27 */
package cn.winfxk.android.mylibrary.tip.dialog.input

import android.view.View
import android.widget.EditText
import cn.winfxk.android.mylibrary.R
import com.google.android.material.textfield.TextInputLayout
import kotlin.lazy

class InputView(val key: String, val builder: InputBuilder) {
    val view: View by lazy { View.inflate(builder.context, R.layout.winfxklia_inputbuilder_item, null) }
    val textInputLayout: TextInputLayout by lazy { view.findViewById(R.id.text_input_layout) }
    val editText: EditText by lazy { view.findViewById(R.id.editText1) }
    val text :String get() = editText.text.toString()
}