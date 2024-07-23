/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/5  上午10:31*/
package com.winfxk.winfxklia.view.setting;

import com.winfxk.winfxklia.R;

public enum Type {
    Empty(R.layout.winfxklia_setting_item_empty), Line(R.layout.winfxklia_setting_item_line),
    Button(R.layout.winfxklia_setting_item_button), Next(R.layout.winfxklia_setting_item_next),
    Switch(R.layout.winfxklia_setting_item_switch),Check(R.layout.winfxklia_setting_item_check),
    Text(R.layout.winfxklia_setting_item_text),Input(R.layout.winfxklia_setting_item_input);
    private final int res;

    private Type(int res) {
        this.res = res;
    }

    public int getRes() {
        return res;
    }
}
