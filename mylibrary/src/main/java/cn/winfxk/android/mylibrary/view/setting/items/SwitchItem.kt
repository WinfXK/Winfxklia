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
* Created Date: 2025/12/12  09:42 */
package cn.winfxk.android.mylibrary.view.setting.items

import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import cn.winfxk.android.mylibrary.R
import com.winfxk.lib.utils.to.objToBoolean

class SwitchItem(title: String, get: GetValue, set: SetValue) : BaseTitleItem(title, get, set) {
    override val view: View by lazy { View.inflate(context, R.layout.winfxklia_basesettings_item_switch, null) }
    override val textView: TextView by lazy { view.findViewById(R.id.textView1) }
    val switch: SwitchCompat by lazy { view.findViewById(R.id.switch1) }

    override fun reloadValue() {
        val value = get.invoke(this);
        switch.isChecked = value as? Boolean ?: value.objToBoolean(false);
    }

    override fun init() {
        switch.isClickable = false;
        switch.focusable = View.NOT_FOCUSABLE
        super.init()
    }

    override fun onItemClick() {
        super.onItemClick()
        switch.isChecked = ! switch.isChecked
        save()
    }

    override fun save() {
        set.invoke(this, switch.isChecked)
    }
}