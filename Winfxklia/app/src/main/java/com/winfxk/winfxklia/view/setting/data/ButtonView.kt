/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/5  下午1:08*/
package com.winfxk.winfxklia.view.setting.data

import android.widget.Button
import android.widget.TextView
import com.winfxk.winfxklia.view.setting.Type

open class ButtonView(hint: TextView, val switch: Button) : LineView(hint) {
    override fun getType(): Type {
        return Type.Button
    }
}