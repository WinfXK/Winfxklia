/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/29  下午1:05*/
package com.winfxk.winfxklia.http.listener;

import com.winfxk.winfxklia.http.BaseHttp;

public interface OnHttpCompletionListener {
    void onHttpCompletion(BaseHttp http, boolean isComplete, Throwable throwable);
}
