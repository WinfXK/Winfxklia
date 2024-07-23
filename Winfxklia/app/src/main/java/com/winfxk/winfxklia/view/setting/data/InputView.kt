/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/10  下午1:08*/
package com.winfxk.winfxklia.view.setting.data

import android.widget.EditText
import android.widget.TextView
import com.winfxk.winfxklia.view.ImageView
import com.winfxk.winfxklia.view.setting.Type

class InputView(icon: ImageView, title: TextView, hint: TextView,val edit: EditText) : com.winfxk.winfxklia.view.setting.data.TextView(icon, title, hint, edit) {
    override fun getType(): Type {
        return Type.Text;
    }
}