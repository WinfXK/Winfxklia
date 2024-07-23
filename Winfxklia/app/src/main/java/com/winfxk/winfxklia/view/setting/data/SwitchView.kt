/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/5  下午1:08*/
package com.winfxk.winfxklia.view.setting.data

import android.annotation.SuppressLint
import android.widget.Switch
import android.widget.TextView
import com.winfxk.winfxklia.view.ImageView
import com.winfxk.winfxklia.view.setting.Type

@SuppressLint("UseSwitchCompatOrMaterialCode")
class SwitchView(val icon: ImageView, val title: TextView, hint: TextView, switch: Switch) : ButtonView(hint, switch) {
    override fun getType(): Type {
        return Type.Switch
    }
}