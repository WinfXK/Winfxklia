/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/29  下午1:03*/
package com.winfxk.winfxklia.http;

import androidx.annotation.NonNull;
import com.winfxk.winfxklia.tool.Tool;
import com.winfxk.winfxklia.tool.copy.net.URLEncoder;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Parameter extends HashMap<String, Object> {
    public static Parameter make(String key, Object value) {
        Parameter parameter = new Parameter();
        parameter.put(key, value);
        return parameter;
    }

    public Parameter add(String key, Object value) {
        put(key, value);
        return this;
    }

    protected byte[] getBytes(BaseHttp http) throws UnsupportedEncodingException {
        return getString(http).getBytes(http.getEncoding());
    }

    protected String getString(BaseHttp http) throws UnsupportedEncodingException {
        StringBuilder str = new StringBuilder();
        for (Entry<String, Object> entry : entrySet())
            str.append((str.length() == 0) ? "" : "&").append(URLEncoder.encode(entry.getKey(), http.getEncoding())).
                    append("=").append(URLEncoder.encode(Tool.objToString(entry.getValue(), ""), http.getEncoding()));
        return str.toString();
    }

    @NonNull
    @NotNull
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, Object> entry : entrySet())
            str.append((str.length() == 0) ? "" : ", ").append(entry.getKey()).append("=>").append(entry.getValue());
        return "[" + str + "]";
    }

}
