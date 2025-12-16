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
* Created Date: 2025/12/12  09:41 */
package cn.winfxk.android.mylibrary.view.setting.items

import android.view.View
import android.widget.EditText
import android.widget.TextView
import cn.winfxk.android.mylibrary.R
import kotlin.lazy

class InputItem(title: String, get: GetValue, set: SetValue) : BaseTitleItem(title, get, set) {
    override val view: View by lazy { View.inflate(context, R.layout.winfxklia_basesettings_item_input, null) }
    override val textView: TextView by lazy { view.findViewById(R.id.textView1) }
    val edittext: EditText by lazy { view.findViewById(R.id.editText1) }

    override fun reloadValue() {
        edittext.setText(get.invoke(this)?.toString() ?: "")
    }

    override fun init() {
        super.init()
    }

    override fun onItemClick() {
        super.onItemClick()
        edittext.requestFocus()
    }

    override fun save() {
        set.invoke(this, edittext.text.toString())
    }
}