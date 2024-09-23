/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/29  下午2:18*/
package com.winfxk.winfxklia.http;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.winfxk.winfxklia.http.listener.OnDownloadCompletListener;
import com.winfxk.winfxklia.http.listener.OnDownloadListener;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class HttpDownload extends BaseHttp {
    protected OnDownloadCompletListener completListener;
    protected OnDownloadListener downloadListener;
    protected int cacheSize = -1;

    public HttpDownload(Context context) {
        this(context, null);
    }

    public HttpDownload(List<String> cookies) {
        this(null, cookies);
    }

    public HttpDownload() {
        this(null, null);
    }

    public HttpDownload(Context context, List<String> cookies) {
        super(context, cookies);
    }

    public void DownFile(String urlStr, File file) throws Exception {
        DownFile(urlStr, file, null);
    }

    public void DownFile(String urlStr, File file, Parameter param) throws Exception {
        Type type = Type.POST;
        long time = System.currentTimeMillis();
        if (urlStr == null) {
            NullPointerException exception = new NullPointerException("urlStr is null");
            if (completListener != null)
                completListener.onDownloadComplet(this, file, System.currentTimeMillis() - time, false, exception);
            throw exception;
        }
        if (file == null) {
            NullPointerException exception = new NullPointerException("file is null");
            if (completListener != null)
                completListener.onDownloadComplet(this, null, System.currentTimeMillis() - time, false, exception);
            throw exception;
        }
        if (param == null) param = getDefPost();
        else param.putAll(getDefPost());
        if (!urlStr.contains("?")) urlStr += "?";
        urlStr += param.getString(this);
        Log.i(getTAG(), "download file to " + file + " by " + urlStr + ", request type: " + type.getType() + ", param->" + param);
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream out = null;
        OutputStream os = null;
        try {
            URL url = new URL(urlStr);
            initializeCookies(url.getHost());
            connection = getConnection(url);
            if (urlStr.toLowerCase().startsWith("https://")) {
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
            inputStream = connection.getInputStream();
            File parent = file.getParentFile();
            if (parent == null) Log.e(getTAG(), "下载文件时出现异常！无法创建下载路径！");
            else if (!parent.exists()) if (!parent.mkdirs()) Log.w(getTAG(), "创建下载路径失败！");
            if (file.exists()) if (!file.delete()) Log.w(getTAG(), "下载前删除源文件失败！");
            out = new FileOutputStream(file);
            long maxlength = Math.abs(Build.VERSION.SDK_INT >= 24 ? connection.getContentLengthLong() : connection.getContentLength());
            byte[] bytes = new byte[getCacheSize(maxlength)];
            long now = 0, length;
            while ((length = inputStream.read(bytes)) != -1) {
                out.write(bytes, 0, (int) length);
                now += length;
                if (downloadListener != null) downloadListener.onDownload(maxlength, now);
            }
            out.flush();
            if (downloadListener != null) downloadListener.onDownload(maxlength, maxlength);
            String stringCookie = connection.getHeaderField("Set-Cookie");
            if (stringCookie != null) {
                cookies.clear();
                cookies.addAll(Arrays.asList(stringCookie.split("; ")));
                saveCookies(url.getHost(), cookies);
            }
            if (completListener != null)
                completListener.onDownloadComplet(this, file, System.currentTimeMillis() - time, true, null);
        } catch (Exception e) {
            if (completListener != null)
                completListener.onDownloadComplet(this, file, System.currentTimeMillis() - time, false, e);
            throw e;
        } finally {
            close(connection, inputStream, out, os);
        }
    }

    protected HttpURLConnection getConnection(URL url) throws Exception {
        return (HttpURLConnection) url.openConnection();
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

    /**
     * 设置下载缓存大小
     *
     * @param cacheSize 想要设置的大小
     */
    public HttpDownload setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    /**
     * @return 返回下载缓存大小
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * 设置下载过程监听器
     */
    public void setDownloadListener(OnDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    /**
     * @return 返回下载过程监听器
     */
    public OnDownloadListener getDownloadListener() {
        return downloadListener;
    }

    /**
     * 设置下载完毕的监听器
     */
    public void setCompletListener(OnDownloadCompletListener completListener) {
        this.completListener = completListener;
    }

    /**
     * @return 获取下载完成的监听器
     */
    public OnDownloadCompletListener getCompletListener() {
        return completListener;
    }

}
