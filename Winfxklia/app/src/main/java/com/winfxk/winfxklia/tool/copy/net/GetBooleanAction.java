package com.winfxk.winfxklia.tool.copy.net;

import java.security.PrivilegedAction;
@SuppressWarnings("unused")
public class GetBooleanAction implements PrivilegedAction<Boolean> {
    private final String theProp;

    public GetBooleanAction(String var1) {
        this.theProp = var1;
    }

    public Boolean run() {
        return Boolean.getBoolean(this.theProp);
    }
}
