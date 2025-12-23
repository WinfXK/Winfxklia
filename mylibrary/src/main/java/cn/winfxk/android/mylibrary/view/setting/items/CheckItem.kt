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
* Created Date: 2025/12/12  13:44 */
package cn.winfxk.android.mylibrary.view.setting.items

import android.view.View
import android.widget.TextView
import cn.winfxk.android.mylibrary.R
import com.google.android.material.checkbox.MaterialCheckBox
import com.winfxk.lib.utils.to.objToBoolean

class CheckItem(title: String, get: GetValue, set: SetValue) : BaseTitleItem(title, get, set) {
    override val view: View by lazy { View.inflate(context, R.layout.winfxklia_basesettings_item_check, null) }
    override val textView: TextView by lazy { view.findViewById(R.id.textView1) }
    val check: MaterialCheckBox by lazy { view.findViewById(R.id.checkBox1) }
    override fun reloadValue() {
        val value = get.invoke(this);
        check.isChecked = value as? Boolean ?: value.objToBoolean(false);
    }

    override fun init() {
        super.init()
        check.isClickable = false;
        check.focusable = View.NOT_FOCUSABLE
    }

    override fun onItemClick() {
        super.onItemClick()
        check.isChecked = ! check.isChecked
        save()
    }

    override fun save() {
        set.invoke(this, check.isChecked)
    }
}