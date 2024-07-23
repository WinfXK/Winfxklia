/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/29  下午1:01*/
package com.winfxk.winfxklia.http;

import com.winfxk.winfxklia.view.setting.data.LineView;

public enum Type {
    GET("GET"), POST("POST");
    private final String type;

    private Type(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
