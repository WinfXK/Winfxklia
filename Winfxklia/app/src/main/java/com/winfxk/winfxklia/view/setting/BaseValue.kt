/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/10  下午1:17*/
package com.winfxk.winfxklia.view.setting

import com.winfxk.winfxklia.view.setting.data.LineView

interface BaseValue {
    fun setValue(activity: BaseSetting, data: LineView);
}