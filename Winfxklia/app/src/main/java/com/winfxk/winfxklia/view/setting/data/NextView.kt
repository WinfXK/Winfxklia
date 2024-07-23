/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/5  下午1:07*/
package com.winfxk.winfxklia.view.setting.data

import android.widget.TextView
import com.winfxk.winfxklia.view.ImageView
import com.winfxk.winfxklia.view.setting.Type

class NextView(icon: ImageView, title: TextView, hint: TextView, val switch: ImageView) : EmptyView(icon, title, hint) {
    override fun getType(): Type {
        return Type.Next;
    }
}