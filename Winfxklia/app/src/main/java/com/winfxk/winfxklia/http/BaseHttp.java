/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/29  上午11:03*/
package com.winfxk.winfxklia.http;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.winfxk.winfxklia.BaseActivity;
import com.winfxk.winfxklia.Main;
import com.winfxk.winfxklia.config.Config;
import com.winfxk.winfxklia.tool.Tool;
import com.winfxk.winfxklia.tool.Utils;
import com.winfxk.winfxklia.tool.able.Tabable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import java.io.Closeable;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
@SuppressLint({"BadHostnameVerifier", "HardwareIds"})
public class BaseHttp implements Tabable, TrustManager, HostnameVerifier {
    protected final List<String> saveCookies = new ArrayList<>();
    protected final List<String> cookies = new ArrayList<>();
    private int passd = 1998;
    private int ConnectTimeout = 15000;
    private String encoding = "UTF-8";
    private int ReadTimeout = 60000;
    private static File file = null;
    protected final Context context;
    private Type type;
    private static final Parameter defPost = new Parameter();
    private static String SessionID = null;

    public void initializeSessionID(Context context) {
        if (SessionID != null) return;
        long bitlength = 0;
        try {
            String sid = Settings.Secure.ANDROID_ID;
            for (int i = 0; i < sid.length(); i++) bitlength += sid.charAt(i);
        } catch (Exception e) {
            Log.e(getTAG(), "生成SessionID时出错", e);
            bitlength = 33442;
        }
        File dir = Build.VERSION.SDK_INT >= 24 ? context.getDataDir() : context.getFilesDir();
        Config config = new Config(new File(dir, "session.wxk"), (int) (bitlength > 65535 ? bitlength % 65535 : bitlength));
        String session = config.getString("sessionID");
        if (session == null || session.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            long time = Math.max(calendar.get(Calendar.YEAR), 1L) * Math.max(calendar.get(Calendar.MONTH) + 1, 1) * Math.max(calendar.get(Calendar.DAY_OF_MONTH), 1);
            while (time < 10000000) time = time * (Tool.getRand(10, 20) / Tool.getRand(1, 9));
            session = Tool.CompressNumber(System.currentTimeMillis())
                    + "-" + Tool.CompressNumber(Tool.getRand(1000000, Integer.MAX_VALUE))
                    + "-" + Tool.CompressNumber(time) +
                    "-" + Tool.CompressNumber(Tool.getRand(1000000, Integer.MAX_VALUE))
                    + "-" + Tool.CompressNumber(bitlength);
            Log.i(getTAG(), "已生成SessionID：" + session);
            config.set("sessionID", session).save();
        } else Log.i(getTAG(), "以获取SessionID：" + session);
        SessionID = session;
    }

    public static Parameter setDefPost(String key, Object value) {
        defPost.add(key, value);
        return defPost;
    }

    public static Parameter setDefPost(Parameter parameter) {
        defPost.putAll(parameter);
        return defPost;
    }

    public static Parameter getDefPost() {
        return defPost;
    }

    public BaseHttp(Context context) {
        this(context, null);
    }

    public BaseHttp(List<String> cookies) {
        this(null, cookies);
    }

    public BaseHttp() {
        this(null, null);
    }

    public BaseHttp(Context context, List<String> cookies) {
        File dir = context == null ? null : Build.VERSION.SDK_INT >= 24 ? context.getDataDir() : context.getFilesDir();
        if (context != null && dir != null) {
            if (file == null) file = new File(dir, "Cookies/");
        } else if (cookies == null || cookies.isEmpty()) Log.w(getTAG(), "无法获取上下文信息，Cookie不可用！");
        if (!(context == null)) initializeSessionID(context);
        if (cookies != null) this.cookies.addAll(cookies);
        this.context = context;
        setPassd();
        Log.i(getTAG(), "已" + (context == null ? "" : "为 " + context.getClass().getSimpleName()) + "创建" + getClass().getSimpleName() + "实例");
    }

    private int getPassd() {
        return passd;
    }

    private void setPassd() {
        if (context != null) try {
            String string = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (string.length() > 6) string = string.substring(string.length() - 6);
            if (string.contains("-")) string = string.substring(string.lastIndexOf("-") + 1);
            long loid = Tool.UnCompressNumber(filterString(string)) + passd;
            try {
                String name = context.getPackageName();
                if (name.length() > 6) name = name.substring(name.length() - 6);
                loid += Tool.UnCompressNumber(filterString(name));
            } catch (Exception e) {
                Log.e(getTAG(), e.getMessage(), e);
            }
            passd = (int) (loid / 2 % 65534);
        } catch (Exception e) {
            Log.e(getTAG(), e.getMessage(), e);
        }
    }

    private String filterString(String name) {
        StringBuilder filtered = new StringBuilder();
        for (char c : name.toCharArray())
            if (containsChar(c))
                filtered.append(c);
        return filtered.toString();
    }

    private boolean containsChar(char c) {
        for (char validChar : Tool.digits)
            if (c == validChar) return true;
        return false;
    }

    /**
     * 设置请求类型
     *
     * @param type 请求类型
     */
    public BaseHttp setType(Type type) {
        this.type = type;
        return this;
    }

    /**
     * @return 返回请求类型
     */
    public Type getType() {
        return type;
    }

    /**
     * 设置编码类型为指定值
     *
     * @param encoding 编码类型
     */
    public BaseHttp setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * @return 返回编码类型
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * 设置连接超时时间
     *
     * @param readTimeout 超时时间
     */
    public BaseHttp setReadTimeout(int readTimeout) {
        ReadTimeout = readTimeout;
        return this;
    }

    /**
     * @return 获取读取超时时间
     */
    public int getReadTimeout() {
        return ReadTimeout;
    }

    /**
     * 设置连接超时时间
     *
     * @param connectTimeout 超时时间
     */
    public BaseHttp setConnectTimeout(int connectTimeout) {
        ConnectTimeout = connectTimeout;
        return this;
    }

    /**
     * @return 获取连接超时时间
     */
    public int getConnectTimeout() {
        return ConnectTimeout;
    }

    /**
     * 初始化Cookie数据
     *
     * @param host 访问的地址
     */
    protected void initializeCookies(String host) {
        saveCookies.clear();
        if (file == null) {
            Log.d(getTAG(), "无法获取Cookie缓存文件存储地址，本次访问不使用缓存Cookie");
            return;
        }
        if (host == null) {
            Log.d(getTAG(), "无法获取Host，本次访问不使用缓存Cookie");
            return;
        }
        try {
            File file = new File(BaseHttp.file, host + ".wck");
            if (!file.exists() || file.isDirectory()) {
                Log.d(getTAG(), "无法获取Cookie缓存文件(" + file + ")对象，本次访问不使用缓存Cookie");
                return;
            }
            String content = Utils.readFile(file);
            if (content.isEmpty()) {
                Log.d(getTAG(), "无法获取Cookie数据，本次访问不使用缓存Cookie");
                return;
            }
            JSONArray json = readText(content);
            for (Object obj : json) {
                if (obj == null) continue;
                saveCookies.add(obj.toString());
            }
        } catch (Exception e) {
            Log.e(getTAG(), "读取Cookie数据时出现异常！", e);
        }
    }

    protected List<String> getSaveCookies() {
        return saveCookies;
    }

    /**
     * 保存Cookie
     *
     * @param host    请求的地址
     * @param cookies 要保存的Cookie
     * @return 保存结果
     */
    protected boolean saveCookies(String host, List<String> cookies) {
        if (file == null) {
            Log.i(getTAG(), "无法获取Cookie缓存文件！Cookie缓存失败！");
            return false;
        }
        if (host == null) {
            Log.i(getTAG(), "无法获取Host，Cookie缓存失败！");
            return false;
        }
        try {
            File file = new File(BaseHttp.file, host + ".wck");
            if (file.getParentFile() == null) {
                Log.e(getTAG(), "无法获取Cookie的存储路径！");
                return false;
            }
            if (!file.getParentFile().exists())
                if (!file.getParentFile().mkdirs()) Log.w(getTAG(), "创建Cookie存储路径时可能并未成功！！");
            Utils.writeFile(file, getStringPasswd(cookies));
            return true;
        } catch (Exception e) {
            Log.e(getTAG(), "保存Cookie数据时出现异常！", e);
            return false;
        }
    }

    /**
     * @return 返回日志的Tag
     */
    public String getTAG() {
        return (context == null ? "" : context instanceof BaseActivity ? ((BaseActivity) context).getTAG() : context.getClass().getSimpleName()) + " Http";
    }

    /**
     * 将List序列化为文本并且进行混淆后返回
     *
     * @param array 需要序列化的对象
     * @return 序列化并且混淆后的结果
     */
    private String getStringPasswd(Collection<?> array) {
        String json = JSON.toJSONString(array);
        StringBuilder content = new StringBuilder();
        int length = json.length();
        for (int i = 0; i < length; i++) content.append(getCharLinkPasswd(json.charAt(i)));
        return content.toString();
    }

    private char getCharLinkPasswd(char string) {
        int result = string + getPassd();
        if (result > 0xffff) result = 0xffff - result;
        return (char) result;
    }

    /**
     * 将被混淆的字符串序列化为一个数组对象
     *
     * @param content 需要反混淆的字符串
     * @return 反序列化的对象F
     */
    private JSONArray readText(String content) {
        int length = content.length();
        StringBuilder json = new StringBuilder();
        for (int i = 0; i < length; i++) json.append(getCharsubPasswd(content.charAt(i)));
        return JSONArray.parseArray(json.toString());
    }

    private char getCharsubPasswd(int c) {
        int result = c - getPassd();
        if (result < 0) result = 0xffff + result;
        return (char) result;
    }

    public BaseHttp close(Closeable... closeables) {
        if (closeables == null || closeables.length < 1) return this;
        for (Closeable closeable : closeables)
            if (closeable != null) try {
                closeable.close();
            } catch (Exception e) {
                Log.e(getTAG(), "关闭流" + closeable.getClass().getSimpleName() + "时出现异常！", e);
            }
        return this;
    }

    public BaseHttp close(HttpURLConnection connection) {
        if (connection == null) return this;
        try {
            connection.disconnect();
        } catch (Exception e) {
            Log.e(getTAG(), "关闭Http链接(" + connection.getClass().getSimpleName() + ")时出现异常！", e);
        }
        return this;
    }

    public BaseHttp close(HttpURLConnection connection, Closeable... closeables) {
        close(connection);
        close(closeables);
        return this;
    }

    protected void initeConnection(HttpURLConnection connection) throws ProtocolException {
        connection.setConnectTimeout(getConnectTimeout());
        connection.setReadTimeout(getReadTimeout());
        if (type != null) connection.setRequestMethod(type.getType());
        connection.setRequestProperty("Connection", " keep-alive");
        connection.setRequestProperty("Application-package", context.getPackageName());
        connection.setRequestProperty("Application-name", context.getApplicationInfo().name);
        connection.setRequestProperty("Winfxk-SessionID", SessionID);
        connection.setRequestProperty("Device-ID", Settings.Secure.ANDROID_ID);
        connection.setRequestProperty("Request-tool", "Winfxk's Winfxklia Android Runtime Library by version " + Main.version + " - http://winfxk.cn");
        connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
        connection.setRequestProperty("Content-Type", " application/x-www-form-urlencoded; charset=" + getEncoding());
        connection.setRequestProperty("User-Agent", " Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
        connection.setDoOutput(true);
        connection.setDoInput(true);
    }

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
