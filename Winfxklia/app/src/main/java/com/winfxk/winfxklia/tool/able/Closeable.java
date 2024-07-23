/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/1  上午9:26*/
package com.winfxk.winfxklia.tool.able;

import android.util.Log;

public interface Closeable {
    default void close(AutoCloseable... closes) {
        if (closes == null) return;
        for (AutoCloseable close : closes)
            if (close != null) try {
                close.close();
            } catch (Exception e) {
                Log.w("Closeable", "关闭流时出现异常！", e);
            }
    }
}
