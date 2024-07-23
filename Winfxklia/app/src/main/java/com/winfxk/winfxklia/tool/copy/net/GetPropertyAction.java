package com.winfxk.winfxklia.tool.copy.net;

import java.security.PrivilegedAction;
@SuppressWarnings("unused")
public class GetPropertyAction implements PrivilegedAction<String> {
    private final String theProp;
    private String defaultVal;

    public GetPropertyAction(String var1) {
        this.theProp = var1;
    }

    public GetPropertyAction(String var1, String var2) {
        this.theProp = var1;
        this.defaultVal = var2;
    }

    public String run() {
        String var1 = System.getProperty(this.theProp);
        return var1 == null ? this.defaultVal : var1;
    }
}
