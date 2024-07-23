/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/29  下午1:10*/
package com.winfxk.winfxklia.http;

import android.content.Context;
import android.util.Log;
import com.winfxk.winfxklia.http.listener.OnHttpCompletionListener;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class Http extends BaseHttp {
    protected OnHttpCompletionListener listener;

    public Http(Context context) {
        this(context, null);
    }

    public Http(List<String> cookies) {
        this(null, cookies);
    }

    public Http() {
        this(null, null);
    }

    public Http(Context context, List<String> cookies) {
        super(context, cookies);
    }

    public String getHttp(String httpUrl) throws Exception {
        return getHttp(httpUrl, Type.POST);
    }

    public String getHttp(String httpUrl, Parameter param) throws Exception {
        return getHttp(httpUrl, Type.POST, param);
    }

    public String getHttp(String httpUrl, Type typem) throws Exception {
        return getHttp(httpUrl, typem, null);
    }

    public String getHttp(String httpUrl, Type type, Parameter param) throws Exception {
        setType(type == null ? type = Type.POST : type);
        if (httpUrl == null || httpUrl.isEmpty()) {
            IllegalArgumentException exception = new IllegalArgumentException("Url不能为空！");
            if (listener != null) listener.onHttpCompletion(this, false, exception);
            throw exception;
        }
        Log.i(getTAG(), "get http by url " + httpUrl + ", type: " + type.getType() + ", paran: " + param);
        HttpURLConnection connection = null;
        BufferedReader br = null;
        OutputStream os = null;
        InputStream is = null;
        String result = null;
        String temp;
        try {
            URL url = new URL(httpUrl);
            initializeCookies(url.getHost());
            connection = getConnection(url);
            if (httpUrl.toLowerCase().startsWith("https://")) {
                Log.i(getTAG(), "正在使用Https访问" + url);
                SSLContext sslContext = SSLContext.getInstance("SSL");
                TrustManager[] tm = {this};
                sslContext.init(null, tm, new SecureRandom());
                SSLSocketFactory ssf = sslContext.getSocketFactory();
                ((HttpsURLConnection) connection).setSSLSocketFactory(ssf);
            }
            initeConnection(connection);
            List<String> cookies = new ArrayList<>();
            if (saveCookies != null && !saveCookies.isEmpty()) cookies.addAll(saveCookies);
            if (this.cookies != null && !this.cookies.isEmpty()) cookies.addAll(this.cookies);
            if (!cookies.isEmpty()) connection.setRequestProperty("Cookie", String.join("; ", cookies));
            if (param != null && !param.isEmpty()) (os = connection.getOutputStream()).write(param.getBytes(this));
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, getEncoding()));
                StringBuilder sbf = new StringBuilder();
                while ((temp = br.readLine()) != null) sbf.append(temp).append("\r\n");
                result = sbf.toString();
                Log.i(getTAG(), "result length: " + result.length());
            }
            String stringCookie = connection.getHeaderField("Set-Cookie");
            if (stringCookie != null) {
                cookies.clear();
                cookies.addAll(Arrays.asList(stringCookie.split("; ")));
                saveCookies(url.getHost(), cookies);
            }
            if (listener != null) listener.onHttpCompletion(this, true, null);
        } catch (Exception e) {
            if (listener != null) listener.onHttpCompletion(this, false, e);
            Log.e(getTAG(), "在发起Http至" + httpUrl + "请求时出现异常！", e);
            throw e;
        } finally {
            close(connection, br, is, os);
        }
        return result;
    }

    protected HttpURLConnection getConnection(URL url) throws Exception {
        return (HttpURLConnection) url.openConnection();
    }

    public void setListener(OnHttpCompletionListener listener) {
        this.listener = listener;
    }

    public OnHttpCompletionListener getListener() {
        return listener;
    }
}
