/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/10  下午1:08*/
package com.winfxk.winfxklia.view.setting.data

import android.widget.TextView
import com.winfxk.winfxklia.view.ImageView
import com.winfxk.winfxklia.view.setting.Type

open class TextView(icon: ImageView, title: TextView, hint: TextView, val text: TextView) : EmptyView(icon, title, hint) {
    override fun getType(): Type {
        return Type.Text;
    }
}