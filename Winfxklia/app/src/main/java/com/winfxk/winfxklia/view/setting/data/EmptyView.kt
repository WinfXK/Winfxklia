/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/5  下午1:06*/
package com.winfxk.winfxklia.view.setting.data

import android.widget.TextView
import com.winfxk.winfxklia.view.ImageView
import com.winfxk.winfxklia.view.setting.Type

open class EmptyView(val icon: ImageView, val title: TextView, hint: TextView) : LineView(hint) {
    override fun getType(): Type {
        return Type.Empty
    }
}