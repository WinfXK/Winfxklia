/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/5  上午11:23*/
package com.winfxk.winfxklia.view.setting.data

import android.widget.TextView
import com.winfxk.winfxklia.view.setting.SettingItem
import com.winfxk.winfxklia.view.setting.Type

open class LineView(val hint: TextView) {
    lateinit var item: SettingItem;
    open fun getType(): Type {
        return Type.Line;
    }
}