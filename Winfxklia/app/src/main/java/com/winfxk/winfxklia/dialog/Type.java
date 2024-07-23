/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/21  上午10:43*/
package com.winfxk.winfxklia.dialog;

import com.winfxk.winfxklia.R;

public enum Type {
    Image(R.drawable.winfxkliba_img),
    Empty(R.drawable.winfxkliba_empty),
    INFO(R.drawable.winfxkliba_info),
    WARNING(R.drawable.winfxkliba_warn),
    ERROR(R.drawable.winfxkliba_error),
    SUCCESS(R.drawable.winfxkliba_succeed),
    ASK(R.drawable.winfxkliba_ask),
    Progress(R.drawable.winfxkliba_loading),
    Loading(R.drawable.winfxkliba_loading),
    Fail(R.drawable.winfxkliba_fail),
    Confirm(R.drawable.winfxkliba_confirm);
    private final int icon;

    Type(int icon) {
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }
}
