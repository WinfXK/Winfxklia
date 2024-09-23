/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/31  上午9:43*/
package com.winfxk.winfxklia.tool.able;

public interface Tabable {
    default String getTAG() {
        return this.getClass().getSimpleName();
    }

    default String getTab() {
        return getTAG();
    }
}
