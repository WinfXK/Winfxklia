package com.winfxk.winfxklia.config;

import android.util.Log;
import com.alibaba.fastjson2.JSONObject;
import com.winfxk.winfxklia.tool.Tool;
import com.winfxk.winfxklia.tool.Utils;
import com.winfxk.winfxklia.tool.able.Tabable;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author: Winfx
 * date: 2024/5/3 17:57
 */
@SuppressWarnings("unused")
public class Config implements Tabable {
    private final ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();
    private static final DumperOptions dumperOptions = new DumperOptions();
    public final static Yaml yaml;
    private final int Passwd;
    private final Type type;
    private final File file;

    static {
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(dumperOptions);
    }

    /**
     * 使用指定的文件和默认的配置类型（JSON）构造Config对象。
     *
     * @param file 配置文件的File对象。
     */
    public Config(File file) {
        this(file, Type.Json);
    }

    /**
     * 使用指定的文件和配置类型构造Config对象。
     *
     * @param file 配置文件的File对象。
     * @param type 配置类型。
     */
    public Config(File file, Type type) {
        this(file, type, null, 1998);
    }

    /**
     * 使用指定的文件和配置类型构造Config对象。
     *
     * @param file 配置文件的File对象。
     */
    public Config(File file, int passwd) {
        this(file, Type.Text, new HashMap<>(), passwd);
    }

    /**
     * 使用指定的文件和预设数据构造Config对象。
     *
     * @param file 配置文件的File对象。
     * @param data 预设的配置数据。
     */
    public Config(File file, Map<String, Object> data) {
        this(file, Type.Json, data, 1998);
    }

    /**
     * 使用指定的文件、配置类型和预设数据构造Config对象。
     *
     * @param file 配置文件的File对象。
     * @param type 配置类型。
     * @param data 预设的配置数据。
     */
    public Config(File file, Type type, Map<String, Object> data, int passwd) {
        if (file == null) throw new IllegalArgumentException("file cannot be null");
        if (type == null) type = Type.Json;
        this.Passwd = passwd;
        this.file = file;
        this.type = type;
        boolean isReload = reload();
        if (!isReload && data != null && !data.isEmpty()) this.data.putAll(data);
    }

    public Map<String, Object> getMap(String key) {
        return getMap(key, null);
    }

    public Map<String, Object> getMap(String key, Map<String, Object> defaultValue) {
        Object obj = get(key);
        if (!(obj instanceof Map)) return defaultValue;
        return (Map<String, Object>) obj;
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object obj = get(key);
        if (obj == null) return defaultValue;
        return Tool.ObjToBool(obj, defaultValue);
    }

    public List<String> getListOfString(String key) {
        List<String> list = new ArrayList<>();
        List<Object> objects = getList(key, null);
        if (objects == null) return null;
        for (Object obj : objects) list.add(String.valueOf(obj));
        return list;
    }

    public List<String> getListOfString(String key, List<String> defaultValue) {
        List<String> list = new ArrayList<>();
        List<Object> objects = getList(key, null);
        if (objects == null) return defaultValue;
        for (Object obj : objects) list.add(String.valueOf(obj));
        return list;
    }

    public <T> List<T> getListOf(String key, List<T> defaultValue) {
        Object obj = get(key);
        if (!(obj instanceof List)) return defaultValue;
        return (List<T>) obj;
    }

    public List<Object> getList(String key) {
        return getList(key, null);
    }

    public List<Object> getList(String key, List<Object> defaultValue) {
        Object obj = get(key);
        if (!(obj instanceof List)) return defaultValue;
        return (List<Object>) obj;
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        Object obj = get(key);
        if (obj == null) return defaultValue;
        String value = getString(key, null);
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return Tool.ObjToInt(get(key), defaultValue);
    }

    public float getFloat(String key) {
        return getFloat(key, 0);
    }

    public float getFloat(String key, float defaultValue) {
        return Tool.objToFloat(get(key), defaultValue);
    }

    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    public double getDouble(String key, double defaultValue) {
        return Tool.objToDouble(get(key), defaultValue);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        Object obj = get(key);
        if (obj == null) return defaultValue;
        return String.valueOf(obj);
    }

    /**
     * 获取指定键对应的对象，如果键不存在或值为null，则返回null。
     *
     * @param key 要获取的键。
     * @return 键对应的对象或null。
     */
    public Object get(String key) {
        return data.getOrDefault(key, null);
    }

    /**
     * 设置指定键的值，并返回Config对象自身，以支持链式调用。
     *
     * @param key   要设置的键。
     * @param value 要设置的值。
     * @return Config
     */
    public Config set(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public Config clear() {
        data.clear();
        return this;
    }

    public Config setAll(Map<String, Object> map) {
        return setAll(map, false);
    }

    public Config setAll(Map<String, Object> map, boolean isClear) {
        if (isClear) data.clear();
        data.putAll(map);
        return this;
    }

    /**
     * 将当前的配置数据保存到配置文件。
     *
     * @return 是否成功保存。
     */
    public synchronized boolean save() {
        try {
            File path = file.getParentFile();
            if (path == null) Log.i(getTAG(), "无法获取配置文件(" + file + ")所属文件夹！");
            else if (!path.exists() || !path.isDirectory())
                if (path.mkdirs()) Log.i(getTAG(), "创建配置文件父文件夹(" + path + ")时肯能已经失败！");
            if (!file.exists() || !file.isFile()) if (!file.createNewFile()) {
                Log.e(getTAG(), "创建配置文件" + file + "时出现异常！");
                return false;
            }
            String content;
            switch (type) {
                case Text:
                    content = saveText();
                    break;
                case Yaml:
                    content = yaml.dump(data);
                    break;
                case Hax:
                    content = saveToHax();
                    break;
                case Int:
                    content = saveToInt();
                    break;
                case Json:
                default:
                    content = JSONObject.toJSONString(data);
            }
            Utils.writeFile(file, content);
        } catch (Exception e) {
            Log.e(getTAG(), "创建配置文件" + file + "时出现异常！", e);
            return false;
        }
        return true;
    }

    private String saveText() {
        return getStringPasswd(data, Passwd);
    }

    public static String getStringPasswd(Map<String, Object> data, int passwd) {
        String json = JSONObject.toJSONString(data);
        StringBuilder content = new StringBuilder();
        int length = json.length();
        for (int i = 0; i < length; i++) content.append(getCharLinkPasswd(json.charAt(i), passwd));
        return content.toString();
    }

    private static char getCharLinkPasswd(char string, int passwd) {
        int result = string + passwd;
        if (result > 0xffff) result = 0xffff - result;
        return (char) result;
    }

    private String saveToHax() {
        String json = JSONObject.toJSONString(data);
        StringBuilder content = new StringBuilder();
        int length = json.length();
        for (int i = 0; i < length; i++)
            content.append(Tool.CompressNumber(((int) json.charAt(i)) + Passwd)).append("/");
        return content.toString();
    }

    private String saveToInt() {
        String json = JSONObject.toJSONString(data);
        StringBuilder content = new StringBuilder();
        int length = json.length();
        for (int i = 0; i < length; i++)
            content.append(((int) json.charAt(i)) + Passwd).append(".");
        return content.toString();
    }

    /**
     * 从配置文件重新加载配置数据。
     *
     * @return 是否成功重新加载。
     */
    public synchronized boolean reload() {
        data.clear();
        if (!file.exists() || !file.isFile()) return false;
        try {
            Map<String, Object> map;
            String content = Utils.readFile(file);
            if (content.isEmpty()) return false;
            switch (type) {
                case Text:
                    map = readText(content, Passwd);
                    break;
                case Yaml:
                    map = yaml.loadAs(content, Map.class);
                    break;
                case Int:
                    map = JSONObject.parseObject(readInt(content));
                    break;
                case Hax:
                    map = JSONObject.parseObject(readHax(content));
                    break;
                case Json:
                default:
                    map = JSONObject.parseObject(content);
            }
            if (map.isEmpty()) return false;
            data.putAll(map);
        } catch (Exception e) {
            Log.e(getTAG(), "加载配置文件" + file + "时出现异常！", e);
            return false;
        }
        return true;
    }

    public Config remove(String... keys) {
        if (keys == null) return this;
        for (String key : keys) data.remove(key);
        return this;
    }

    public Config removeValue(Object value) {
        if (data.containsValue(value)) {
            List<Map.Entry<String, Object>> list = new ArrayList<>(data.entrySet());
            list.parallelStream().forEach(entry -> {
                if (entry.getValue().equals(value))
                    data.remove(entry.getKey());
            });
        }
        return this;
    }

    public static Map<String, Object> getPasswdMap(String data, int passwd) {
        return readText(data, passwd);
    }

    private static Map<String, Object> readText(String content, int passwd) {
        int length = content.length();
        StringBuilder json = new StringBuilder();
        for (int i = 0; i < length; i++) json.append(getCharsubPasswd(content.charAt(i), passwd));
        return JSONObject.parseObject(json.toString());
    }

    private static char getCharsubPasswd(int c, int passwd) {
        int result = c - passwd;
        if (result < 0) result = 0xffff + result;
        return (char) result;
    }


    /**
     * 已整数的方式加载数据
     */
    private String readInt(String content) {
        String[] strings = content.split("\\.");
        StringBuilder json = new StringBuilder();
        for (String s : strings) {
            if (s == null || s.isEmpty()) continue;
            json.append((char) (Integer.parseInt(s) - Passwd));
        }
        return json.toString();
    }

    /**
     * 已二进制的方式加载数据
     */
    private String readHax(String content) {
        String[] strings = content.split("/");
        StringBuilder json = new StringBuilder();
        for (String s : strings) {
            if (s == null || s.isEmpty()) continue;
            json.append((char) (Tool.UnCompressNumber(s) - Passwd));
        }
        return json.toString();
    }

    /**
     * 获取配置文件的File对象。
     *
     * @return 配置文件的File对象。
     */
    public File getFile() {
        return file;
    }

    /**
     * 获取配置类型。
     *
     * @return 配置类型。
     */
    public Type getType() {
        return type;
    }

    public ConcurrentHashMap<String, Object> getData() {
        return data;
    }

    @NotNull
    @Override
    public String toString() {
        return "Config{" + "data=" + data + ", type=" + type + ", file=" + file + '}';
    }
}
