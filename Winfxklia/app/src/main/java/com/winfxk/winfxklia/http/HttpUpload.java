/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/6/29  下午1:16*/
package com.winfxk.winfxklia.http;

import android.content.Context;
import android.util.Log;
import com.winfxk.winfxklia.http.listener.OnUploadCompletListener;
import com.winfxk.winfxklia.http.listener.OnUploadListener;
import com.winfxk.winfxklia.tool.Tool;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HttpUpload extends BaseHttp {
    protected OnUploadCompletListener completListener;
    protected OnUploadListener uploadListener;
    protected int cacheSize = -1;

    public HttpUpload(Context context) {
        super(context, null);
    }

    public HttpUpload(List<String> cookies) {
        super(null, cookies);
    }

    public HttpUpload() {
        super(null, null);
    }

    public HttpUpload(Context context, List<String> cookies) {
        super(context, cookies);
    }

    public String upload(String httpUrl, File file) throws Exception {
        return upload(httpUrl, null, file);
    }

    public String upload(String httpUrl, Parameter param, File file) throws Exception {
        return upload(httpUrl, Type.POST, param, file);
    }

    public String upload(String httpUrl, Type type, Parameter param, File file) throws Exception {
        long tick = System.currentTimeMillis();
        if (file == null) {
            Exception th = new IOException("传入的文件对象为空");
            if (completListener != null)
                completListener.onUploadComplet(this, null, System.currentTimeMillis() - tick, false, th);
            throw th;
        }
        if (!file.exists() || !file.isFile()) {
            Exception th = new IOException("传入的文件不存在或不是文件");
            if (completListener != null)
                completListener.onUploadComplet(this, file, System.currentTimeMillis() - tick, false, th);
            throw th;
        }
        HttpURLConnection connection = null;
        DataOutputStream request = null;
        FileInputStream fileInputStream = null;
        BufferedReader in = null;
        try {
            Log.i(getTAG(),"Upload file to: "+httpUrl+", params: "+(param!=null?param.toString():"null"));
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            initeConnection(connection);
            connection.setUseCaches(false);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=****");
            if (param == null) param = getDefPost();
            else param.putAll(getDefPost());
            for (Map.Entry<String, Object> entry : param.entrySet())
                connection.setRequestProperty(URLEncoder.encode(entry.getKey(), getEncoding()),
                        URLEncoder.encode(Tool.objToString(entry.getValue()), getEncoding()));
            List<String> cookies = new ArrayList<>();
            if (saveCookies != null && !saveCookies.isEmpty()) cookies.addAll(saveCookies);
            if (this.cookies != null && !this.cookies.isEmpty()) cookies.addAll(this.cookies);
            if (!cookies.isEmpty()) connection.setRequestProperty("Cookie", String.join("; ", cookies));
            request = new DataOutputStream(connection.getOutputStream());
            fileInputStream = new FileInputStream(file);
            request.writeBytes("--****\r\n");
            request.writeBytes("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n\r\n");
            long maxlength = file.length();
            long now = 0;
            byte[] bytes = new byte[getCacheSize(maxlength)];
            int length, index = 0;
            while ((length = fileInputStream.read(bytes)) != -1) {
                request.write(bytes, 0, length);
                now += length;
                if (index++ % 5 == 0) request.flush();
                if (uploadListener != null) uploadListener.onUpload(maxlength, now);
            }
            if (uploadListener != null) uploadListener.onUpload(maxlength, maxlength);
            request.writeBytes("\r\n");
            request.writeBytes("--****--\r\n");
            String stringCookie = connection.getHeaderField("Set-Cookie");
            if (stringCookie != null) {
                Log.i(getTAG(), "正在插入缓存Cookie...");
                cookies.clear();
                cookies.addAll(Arrays.asList(stringCookie.split("; ")));
                saveCookies(url.getHost(), cookies);
            }
            request.flush();
            request.close();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();
                if (completListener != null)
                    completListener.onUploadComplet(this, file, System.currentTimeMillis() - tick, true, null);
                return response.toString();
            }
            if (completListener != null)
                completListener.onUploadComplet(this, file, System.currentTimeMillis() - tick, false, new IllegalAccessException("服务器返回结果不是200"));
        } catch (Exception e) {
            if (completListener != null)
                completListener.onUploadComplet(this, file, System.currentTimeMillis() - tick, false, e);
            throw e;
        } finally {
            close(connection, in, request, fileInputStream);
        }
        return null;
    }

    /**
     * @param maxlength 文件大小
     * @return 分配的缓存大小，如果用户指定了缓存大小，那么默认使用用户设置
     */
    protected int getCacheSize(long maxlength) {
        if (cacheSize > 0) return cacheSize;
        int defLength = 32768;
        return maxlength > 32768 ? defLength : maxlength > 8192 ? 8192 : 1024;
    }

    public OnUploadListener getUploadListener() {
        return uploadListener;
    }

    public void setUploadListener(OnUploadListener uploadListener) {
        this.uploadListener = uploadListener;
    }

    public OnUploadCompletListener getCompletListener() {
        return completListener;
    }

    public void setCompletListener(OnUploadCompletListener completListener) {
        this.completListener = completListener;
    }

    /**
     * 设置缓存大小
     *
     * @param cacheSize 想要设置的大小
     */
    public HttpUpload setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

}
