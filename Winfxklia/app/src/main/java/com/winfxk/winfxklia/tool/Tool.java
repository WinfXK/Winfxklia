package com.winfxk.winfxklia.tool;


import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

import javax.net.ssl.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Winfxk
 */
@SuppressWarnings("unused")
@SuppressLint({"CustomX509TrustManager", "TrustAllX509TrustManager"})
public class Tool implements X509TrustManager, HostnameVerifier {
    private static final String colorKeyString = "123456789abcdef";
    private static final String randString = "-+abcdefghijklmnopqrstuvwxyz_";
    private static final SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
    private static final SimpleDateFormat data = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private final static char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '+', '-'};
    private static final List<String> INVALID_CHARACTERS = Arrays.asList("<", ">", ":", "\\", "\"", "/", "|", "?", "*");
    private static final List<String> reservedWords = Arrays.asList("con", "prn", "aux", "nul", ".", "..");
    private static final String[] FileSizeUnit = {"B", "KB", "MB", "GB", "TB", "PB", "EB"};
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final Random random = new Random();
    private static final String isDigit = ".*\\d+.*";
    private static final String Tag = "Tool";

    /**
     * 读取一个文件并且序列化为Base64字符串
     *
     * @param file 要序列化的文件
     * @return 序列化后的内容
     * @throws IOException 可能得异常
     */
    public static String readFileToBase64String(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1)
                baos.write(buffer, 0, bytesRead);
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        }
    }

    /**
     * @param fileName 需要判断的文件名
     * @return 判断一个文件名是否合法合规
     */
    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) return false;
        if (fileName.length() > 260) return false;
        for (String s : INVALID_CHARACTERS) if (fileName.contains(s)) return false;
        return !reservedWords.contains(fileName.toLowerCase());
    }

    /**
     * 颜色值转换为RGB
     *
     * @param colorInt Int颜色值
     * @return RGB
     */
    public static int[] ColorToRGB(int colorInt) {
        int alpha = (colorInt & 0xFF000000) >>> 24;
        int red = (colorInt & 0x00FF0000) >>> 16;
        int green = (colorInt & 0x0000FF00) >>> 8;
        int blue = colorInt & 0x000000FF;
        return new int[]{colorInt < 0x01000000 ? 0xff : alpha, red, green, blue};
    }

    /**
     * 将RGB转换为颜色值
     */
    public static long toRGB(byte a, byte r, byte g, byte b) {
        long result = r & 0xff;
        result |= (g & 0xff) << 8;
        result |= (b & 0xff) << 16;
        result |= (long) (a & 0xff) << 24;
        return result & 0xFFFFFFFFL;
    }

    /**
     * RGB转换为颜色值
     *
     * @param argb RGB
     * @return 颜色值
     */
    public static int RGBtoColor(int[] argb) {
        int alpha = argb.length > 3 ? argb[0] : 0xff;
        int reb = argb.length > 3 ? argb[1] : argb[0];
        int green = argb.length > 3 ? argb[2] : argb[1];
        int blue = argb.length > 3 ? argb[3] : argb[2];
        return RGBtoColor(alpha, reb, green, blue);
    }

    /**
     * RGB转换为颜色值
     *
     * @param red   红色
     * @param green 绿色
     * @param blue  蓝色
     * @return Int颜色值
     */
    public static int RGBtoColor(int red, int green, int blue) {
        return RGBtoColor(red, green, blue, 0xff);
    }

    /**
     * ARGB转换为颜色值
     *
     * @param red   红色
     * @param green 绿色
     * @param blue  蓝色
     * @param alpha 透明度
     * @return Int颜色值
     */
    public static int RGBtoColor(int red, int green, int blue, Integer alpha) {
        int alphaValue = (alpha != null && alpha >= 0 && alpha <= 255) ? alpha : 0xFF;
        return ((alphaValue << 24) | (red << 16) | (green << 8) | blue);
    }

    /**
     * 使用哈希加密用户密码
     *
     * @param plaintextPassword 用户密码
     * @return 加密结果
     */
    public static String hashPassword(String plaintextPassword) {
        if (plaintextPassword == null || plaintextPassword.isEmpty()) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plaintextPassword.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) sb.append(String.format("%02x", aByte));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * @param obj 需要判断的内容
     * @return 判断一个字符串时候包含数值
     */
    public static boolean isDigit(Object obj) {
        if (obj == null) return false;
        String string = Tool.objToString(obj, null);
        if (string == null || string.isEmpty()) return false;
        return Pattern.matches(isDigit, string);
    }

    public static boolean sleep(long time) {
        try {
            Thread.sleep(time);
            return true;
        } catch (Exception e) {
            Log.e(Tag, "使用Sleep时出现异常", e);
            return false;
        }
    }

    /**
     * 从两个数据中间获取合理的活动数
     *
     * @param max  最大值
     * @param min  最小值
     * @param live 可能活动的值
     * @return 适合的值
     */
    public static int getMath(int max, int min, int live) {
        return Math.min(min, Math.max(max, live));
    }

    /**
     * 将List转换为数组
     *
     * @param list 需要转换的list
     * @param v    传入一个空数组
     * @param <V>  数组的类型
     * @param <T>  list的类型
     * @return 转换结果
     */
    public static <V, T> V[] getArray(List<T> list, V[] v) {
        if (list != null) try {
            return list.toArray(v);
        } catch (Exception e) {
            Log.e(Tag, "将List转换为数组时出现异常！", e);
        }
        return v;
    }

    /**
     * 返回文件大小
     *
     * @param file 要获取大小的文件对象
     */
    public static String getSize(File file) {
        return getSize(file.length());
    }

    /**
     * 获取文件大小
     */
    public static String getSize(long size) {
        if (size < 1024)
            return size + "B";
        int index = 1;
        float Size = size / (float) 1024;
        while (Size > 1024) {
            index++;
            Size /= 1024;
        }
        return Tool.Double2(Size) + FileSizeUnit[Math.min(index, FileSizeUnit.length - 1)];
    }

    /**
     * 读取包内文件
     */
    public static InputStream getResourceStream(String FileName) {
        return Tool.class.getResourceAsStream("/res/" + FileName);
    }

    /**
     * 读取包内文本
     */
    public static String getResource(String FileName) throws IOException {
        return Utils.readFile(Tool.class.getResourceAsStream("/res/" + FileName));
    }

    /**
     * 把10进制的数字转换成64进制
     */
    public static String CompressNumber(long number) {
        char[] buf = new char[64];
        int charPos = 64;
        int radix = 1 << 6;
        long mask = radix - 1;
        do {
            buf[--charPos] = digits[(int) (number & mask)];
            number >>>= 6;
        } while (number != 0);
        return new String(buf, charPos, (64 - charPos));
    }

    /**
     * 把64进制的字符串转换成10进制
     */
    public static long UnCompressNumber(String decompStr) {
        long result = 0;
        for (int i = decompStr.length() - 1; i >= 0; i--) {
            for (int j = 0; j < digits.length; j++) {
                if (decompStr.charAt(i) == digits[j]) {
                    result += ((long) j) << 6 * (decompStr.length() - 1 - i);
                }
            }
        }
        return result;
    }

    /**
     * 计算两个日期之间相隔多少天
     *
     * @param date1 第一个日期字符串
     * @param date2 第二个日期字符串
     */
    public static long getDay(String date1, String date2) {
        return getDay(date1, "yyyy-MM-dd", date2, "yyyy-MM-dd");
    }

    /**
     * 计算两个日期之间相隔多少天
     *
     * @param date1format 第一个日期字符串的格式<yyyy-MM-dd>
     * @param date2format 第二个日期字符串<yyyy-MM-dd>
     */
    public static long getDay(String date1sStr, String date1format, String date2Str, String date2format) {
        Date date1 = parseDate(date1sStr, date1format);
        Date date2 = parseDate(date2Str, date2format);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        long timeInMillis1 = calendar.getTimeInMillis();
        calendar.setTime(date2);
        long timeInMillis2 = calendar.getTimeInMillis();
        return (timeInMillis2 - timeInMillis1) / (1000L * 3600L * 24L);
    }

    /**
     * 将指定的日期字符串转换成日期
     *
     * @param dateStr 日期字符串
     * @return 日期对象
     */
    public static Date parseDate(String dateStr) {
        return parseDate(dateStr, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 将指定的日期字符串转换成日期
     *
     * @param dateStr 日期字符串
     * @param pattern 格式
     * @return 日期对象
     */
    public static Date parseDate(String dateStr, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.CHINA);
        Date date;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("日期转化错误");
        }
        return date;
    }

    /**
     * 数组相加
     *
     * @param <T>    数组的类型
     * @param arrays 要先加的数组内容
     */
    @SafeVarargs
    public static <T> T[] Arrays(T[]... arrays) {
        List<T> list = new ArrayList<>();
        for (T[] t : arrays)
            Collections.addAll(list, t);
        return (T[]) list.toArray();
    }

    /**
     * 将一段不知道什么玩意转化为纯整数
     */
    public static int ObjToInt(Object object) {
        return ObjToInt(object, 0);
    }

    /**
     * 讲一段不知道什么玩意转化为纯数字
     *
     * @param i 若不是纯数字将默认转化的值
     */
    public static int ObjToInt(Object object, int i) {
        try {
            return objToBigDecimal(object, new BigDecimal(i)).intValue();
        } catch (Exception e) {
            Log.d(Tag, "尝试吧一个未知值转换为整数时出现异常！", e);
            return i;
        }
    }

    /**
     * 将一个不知道什么玩意转换为Long
     */
    public static long objToLong(Object obj) {
        return objToLong(obj, 0L);
    }

    /**
     * 将一个不知道什么玩意转换为Long
     */
    public static long objToLong(Object obj, long d) {
        try {
            return objToBigDecimal(obj, new BigDecimal(d)).longValue();
        } catch (Exception e) {
            Log.d(Tag, "尝试吧一个未知值转换为Long时出现异常！", e);
            return d;
        }
    }

    /**
     * 将一个不知道什么玩意转换为双精
     */
    public static float objToFloat(Object obj) {
        return objToFloat(obj, 0f);
    }

    /**
     * 将一个不知道什么玩意转换为浮点
     */
    public static float objToFloat(Object obj, float d) {
        try {
            return objToBigDecimal(obj, new BigDecimal(d)).floatValue();
        } catch (Exception e) {
            Log.d(Tag, "尝试吧一个未知值转换为Float时出现异常！", e);
            return d;
        }
    }

    /**
     * 将一个不知道什么玩意转换为双精
     */
    public static double objToDouble(Object obj) {
        return objToDouble(obj, 0d);
    }

    /**
     * 将一个不知道什么玩意转换为双精
     */
    public static double objToDouble(Object obj, double d) {
        try {
            return objToBigDecimal(obj, new BigDecimal(d)).doubleValue();
        } catch (Exception e) {
            Log.d(Tag, "尝试吧一个未知值转换为Double时出现异常！", e);
            return d;
        }
    }

    /**
     * 将一个不知道什么值转换为BigDecimal
     *
     * @param obj Object
     * @return BigDecimal
     */
    public static BigDecimal objToBigDecimal(Object obj) {
        return objToBigDecimal(obj, null);
    }

    /**
     * 将一个不知道什么值转换为BigDecimal
     *
     * @param obj Object
     * @param d   转换失败默认返回的值
     * @return BigDecimal
     */
    public static BigDecimal objToBigDecimal(Object obj, BigDecimal d) {
        String string = String.valueOf(obj).trim();
        if (obj == null || string.isEmpty()) return d;
        try {
            return new BigDecimal(string);
        } catch (Exception e) {
            Log.e(Tag, "尝试将未知数值转换为BigDecimal时出现异常！", e);
        }
        return d;
    }

    /**
     * 求最大公约数
     */
    public static long getGys(long num1, long num2) {
        num1 = Math.abs(num1);
        num2 = Math.abs(num2);
        while (num2 != 0) {
            long remainder = num1 % num2;
            num1 = num2;
            num2 = remainder;
        }
        return num1;
    }

    /**
     * 获取小数长度
     */
    public static int getDecimalsLength(float f) {
        String string = String.valueOf(f);
        if (f == 0 || !string.contains("."))
            return 0;
        return string.substring(string.indexOf(".") + 1).length();
    }

    /**
     * 获取字符真实长度
     */
    public static int String_length(String value) {
        int valueLength = 0;
        String chinese = "[一-龥]";
        for (int i = 0; i < value.length(); i++) {
            String temp = value.substring(i, i + 1);
            if (temp.matches(chinese))
                valueLength += 2;
            else
                valueLength += 1;
        }
        return valueLength;
    }

    /**
     * 小数转分数
     *
     * @param f int[分子,分母]
     */
    public static long[] getGrade(float f, int floatLength) {
        if (f == 0)
            return new long[]{0, 0};
        String string = String.valueOf(f);
        if (!string.contains("."))
            return new long[]{(long) f, 1};
        String sint = string.substring(0, string.indexOf("."));
        String sfloat = string.substring(string.indexOf(".") + 1);
        long Fenmu = 1;
        for (int k = 0; k < floatLength; k++)
            Fenmu *= 10;
        long Fenzi = Long.parseLong(sint + sfloat);
        long lXs = Math.min(Fenzi, Fenmu), j;
        for (j = lXs; j > 1; j--)
            if (Fenzi % j == 0 && Fenmu % j == 0)
                break;
        Fenzi = Fenzi / j;
        Fenmu = Fenmu / j;
        return new long[]{Fenzi, Fenmu};
    }

    /**
     * Object对象转换为String
     */
    public static String objToString(Object obj) {
        return objToString(obj, null);
    }

    /**
     * Object对象转换为String
     */
    public static String objToString(Object obj, String string) {
        if (obj == null)
            return string;
        try {
            return String.valueOf(obj);
        } catch (Exception e) {
            return string;
        }
    }

    /**
     * 将秒长度转换为日期长度
     *
     * @param active 秒长度
     */
    public static String getTimeBy(long active) {
        long years = active / (365 * 86400000L);
        active %= (365 * 86400000L);
        long months = active / (30 * 86400000L);
        active %= (30 * 86400000L);
        long days = active / 86400000L;
        active %= 86400000L;
        long hours = active / 3600000;
        active %= 3600000;
        long minutes = active / 60000;
        active %= 60000;
        long seconds = active / 1000;
        long milliseconds = active % 1000;
        StringBuilder sb = new StringBuilder();
        if (years > 0) sb.append(years).append("年");
        if (months > 0) sb.append(months).append("月");
        if (days > 0) sb.append(days).append("天");
        if (hours > 0) sb.append(hours).append("小时");
        if (minutes > 0) sb.append(minutes).append("分");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("秒");
        if (milliseconds > 0) sb.append(milliseconds).append("毫秒");
        return sb.toString();
    }

    /**
     * 判断两个ID是否匹配，x忽略匹配
     */
    public static boolean isMateID(Object id1, Object id2) {
        if (id1 == null || id2 == null) return false;
        String ID1 = String.valueOf(id1), ID2 = String.valueOf(id2);
        if (!ID1.contains(":")) ID1 += ":0";
        if (!ID2.contains(":")) ID2 += ":0";
        String[] ID1s = ID1.split(":"), ID2s = ID2.split(":");
        if (ID1s[0].equals("x") || ID2s[0].equals("x") || ID1s[0].equals(ID2s[0]))
            return ID1s[1].equals("x") || ID2s[1].equals("x") || ID2s[1].equals(ID1s[1]);
        return false;
    }

    /**
     * 获取当前时间
     */
    public static String getTime() {
        return time.format(new Date());
    }

    /**
     * 文件复制
     *
     * @param file1 源文件
     * @param file2 目标文件
     */
    public static void Copy(String file1, String file2) throws Exception {
        Copy(new File(file1), new File(file2));
    }

    /**
     * 文件复制
     *
     * @param file1 源文件
     * @param file2 目标文件
     */
    public static void Copy(File file1, File file2) throws Exception {
        if (!file1.exists()) return;
        File Parent = file2.getParentFile();
        if (Parent != null && !Parent.exists()) if (!Parent.mkdirs()) Log.e(Tag, "复制文件时创建文件所在路径失败！");
        InputStream fileInputStream = Files.newInputStream(file1.toPath());
        OutputStream fileOutputStream = new FileOutputStream(file2, true);
        int temp;
        while ((temp = fileInputStream.read()) != -1)
            fileOutputStream.write(temp);
        fileInputStream.close();
        fileOutputStream.close();
    }

    /**
     * 返回当前时间 <年-月-日>
     */
    public static String getDate() {
        return data.format(new Date());
    }

    /**
     * 自动检查ID是否包含特殊值，若不包含则默认特殊值为0后返回数组
     *
     * @param ID 要检查分解的ID
     * @return int[]{ID, Damage}
     */
    public static int[] IDtoFullID(Object ID) {
        return IDtoFullID(ID, 0);
    }

    /**
     * 自动检查ID是否包含特殊值，若不包含则设置特殊值为用户定义值后返回数组
     *
     * @param Damage 要默认设置的特殊值
     * @return int[]{ID, Damage}
     */
    public static int[] IDtoFullID(Object obj, int Damage) {
        String ID = "0";
        if (obj != null && !String.valueOf(obj).isEmpty())
            ID = String.valueOf(obj);
        if (!ID.contains(":"))
            ID += ":" + Damage;
        String[] strings = ID.split(":");
        try {
            return new int[]{Integer.parseInt(strings[0]), Integer.parseInt(strings[1])};
        } catch (Exception e) {
            return new int[]{0, 0};
        }
    }
    /**
     * 获取随机数
     */
    public static int getRand(int min, int max) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * 将可能简化的颜色值转换为完全体
     *
     * @param color 可能为简称的颜色值
     * @return AARRGGBB的颜色值
     */
    public static int expandColor(int color) {
        if (color <= 0xff) color |= (color << 16) | (color << 8);
        else if (color <= 0xffff) color |= (color << 16);
        else if (color <= 0xffffff) color |= 0xff000000;
        return color;
    }

    /**
     * 从集合中随机返回一个值
     *
     * @param arrays 集合
     * @param <T>    值得类型
     * @return 随机值
     */
    @SafeVarargs
    public static <T> T getRand(T... arrays) {
        return getRand((T) null, arrays);
    }

    /**
     * 从集合中随机返回一个值
     *
     * @param defaultValue 默认值
     * @param arrays       集合
     * @param <T>          值得类型
     * @return 随机值
     */
    @SafeVarargs
    public static <T> T getRand(T defaultValue, T... arrays) {
        if (arrays == null || arrays.length == 0) return defaultValue;
        return arrays[getRand(0, arrays.length - 1)];
    }

    /**
     * 从list中随机返回一个数据
     *
     * @param list 要返回的集合
     * @param <T>  值得类型
     * @return 可能的值
     */
    public static <T> T getRand(List<T> list) {
        return getRand(list, (T) null);
    }

    /**
     * 从list集合随机返回一个字
     *
     * @param list         list集合
     * @param defaultValue 默认值
     * @param <T>          值得类型
     * @return 随机值
     */
    public static <T> T getRand(List<T> list, T defaultValue) {
        if (list == null || list.isEmpty()) return defaultValue;
        return list.get(getRand(0, list.size() - 1));
    }

    /**
     * 获取随机数
     */
    public static int getRand() {
        return getRand(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * 返回一个随机颜色代码
     */
    public static String getRandColor() {
        return getRandColor(colorKeyString);
    }

    /**
     * 返回一个随机颜色代码
     *
     * @param ColorFont 可以随机到的颜色代码
     */
    public static String getRandColor(String ColorFont) {
        int rand = Tool.getRand(0, ColorFont.length() - 1);
        return "§" + ColorFont.charAt(rand);
    }

    /**
     * 将字符串染上随机颜色
     *
     * @param Font 要染色的字符串
     */
    public static String getColorFont(String Font) {
        return getColorFont(Font, colorKeyString);
    }

    /**
     * 返回一个随机字符
     *
     * @return 随机字符
     */
    public static String getRandString(int min, int max) {
        return getRandString(Tool.getRand(min, max));
    }

    /**
     * 返回一个随机字符
     *
     * @return 随机字符
     */
    public static String getRandString(int length) {
        if (length < 1) throw new IllegalArgumentException("length must be a positive integer");
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < length; i++) str.append(getRandString());
        return str.toString();
    }

    /**
     * 返回一个随机字符
     *
     * @return 随机字符
     */
    public static String getRandString() {
        return getRandString(randString);
    }

    /**
     * 返回一个随机字符
     *
     * @param string 要随机字符的范围
     * @return 随机字符
     */
    public static String getRandString(String string) {
        int r1 = getRand(0, string.length() - 1);
        return string.substring(r1, r1 + 1);
    }

    /**
     * 将字符串染上随机颜色
     *
     * @param Font      要染色的字符串
     * @param ColorFont 随机染色的颜色代码
     */
    public static String getColorFont(String Font, String ColorFont) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < Font.length(); i++) {
            int rand = Tool.getRand(0, ColorFont.length() - 1);
            text.append("§").append(ColorFont.charAt(rand)).append(Font.charAt(i));
        }
        return text.toString();
    }

    /**
     * 判断字符串是否是整数型
     */
    public static boolean isInteger(Object str) {
        try {
            new BigDecimal(String.valueOf(str));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断一段字符串中是否只为纯数字
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9.]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 字符串转换Unicode
     *
     * @param string 要转换的字符串
     */
    public static String StringToUnicode(String string) {
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < string.length(); i++)
            unicode.append("\\u").append(Integer.toHexString(string.charAt(i)));
        return unicode.toString();
    }

    /**
     * unicode 转字符串
     *
     * @param unicode 全为 Unicode 的字符串
     */
    public static String UnicodeToString(String unicode) {
        StringBuilder string = new StringBuilder();
        String[] hex = unicode.split("\\\\u");
        for (int i = 1; i < hex.length; i++)
            string.append((char) Integer.parseInt(hex[i], 16));
        return string.toString();
    }

    /**
     * 设置小数长度</br>
     * 默认保留两位小数</br>
     *
     * @param d 要设置的数值
     */
    public static double Double2(double d) {
        return Double2(d, 2);
    }

    /**
     * 设置小数长度</br>
     *
     * @param d      要设置的数
     * @param length 要保留的小数的
     */
    public static double Double2(double d, int length) {
        if (d == 0)
            return 0;
        StringBuilder s = new StringBuilder("#.0");
        for (int i = 1; i < length; i++)
            s.append("0");
        DecimalFormat df = new DecimalFormat(s.toString());
        return Double.parseDouble(df.format(d));
    }

    /**
     * 根据提供的文件列表删除
     *
     * @param files 需要删除的文件列表
     * @return 删除结果
     */
    public static boolean delete(File... files) {
        if (files == null) return false;
        boolean isSu = true;
        for (File file : files) {
            if (file == null || !file.exists()) continue;
            isSu = isSu & delete(file);
        }
        return isSu;
    }

    /**
     * 删除文件或者文件夹
     *
     * @param file 文件或文件夹对象
     * @return 删除结果
     */
    public static boolean delete(File file) {
        if (file == null || !file.exists()) return false;
        if (file.isFile()) return file.delete();
        else {
            File[] files = file.listFiles();
            if (files == null) return false;
            boolean delete = true;
            for (File item : files) {
                if (item == null || !item.exists()) continue;
                if (item.isFile()) delete = delete & item.delete();
                else delete = delete & delete(item);
            }
            return delete & file.delete();
        }
    }

    /**
     * 发送HTTP请求
     *
     * @param httpUrl 请求地址
     */
    public static String getHttp(String httpUrl) throws Exception {
        return getHttp(httpUrl, "POST", null);
    }

    /**
     * 发送HTTP请求
     *
     * @param httpUrl 请求地址
     * @param param   请求的内容
     */
    public static String getHttp(String httpUrl, String param) throws Exception {
        return getHttp(httpUrl, "POST", param);
    }

    /**
     * 发送HTTP请求
     *
     * @param httpUrl 请求地址
     * @param Type    请求的方式
     * @param param   请求的内容
     */
    public static String getHttp(String httpUrl, String Type, String param) throws Exception {
        HttpURLConnection connection;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;
        URL url = new URL(httpUrl);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(Type);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(60000);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
        connection.setRequestProperty("Connection", " keep-alive");
        connection.setRequestProperty("Content-Type", " application/x-www-form-urlencoded; charset=UTF-8");
        connection.setRequestProperty("User-Agent", " Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
        if (param != null && !param.isEmpty()) {
            OutputStream os;
            os = connection.getOutputStream();
            os.write(param.getBytes());
            os.close();
        }
        if (connection.getResponseCode() == 200) {
            is = connection.getInputStream();
            br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sbf = new StringBuilder();
            String temp;
            while ((temp = br.readLine()) != null) {
                sbf.append(temp);
                sbf.append("\r\n");
            }
            result = sbf.toString();
        }
        if (null != br) br.close();
        if (null != is) is.close();
        connection.disconnect();
        return result;
    }

    /**
     * 从一段字符内截取另一段字符
     *
     * @param Context 要截取字符的原文
     * @return 截取完毕的内容
     */
    public static String cutString(String Context, String strStart, String strEnd) {
        int strStartIndex = Context.indexOf(strStart);
        int strEndIndex = Context.indexOf(strEnd, strStartIndex + 1);
        if (strStartIndex < 0 || strEndIndex < 0)
            return null;
        return Context.substring(strStartIndex, strEndIndex).substring(strStart.length());
    }

    /**
     * 下载文件
     *
     * @param urlStr   要下载的文件的连接
     * @param fileName 下载后文件的名字
     * @param savePath 要保存的位置
     */
    public static void DownFile(String urlStr, String fileName, String savePath) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(3 * 1000);
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        InputStream inputStream = conn.getInputStream();
        byte[] getData = readInputStream(inputStream);
        File saveDir = new File(savePath);
        if (!saveDir.exists()) if (!saveDir.mkdir()) Log.e(Tag, "下载文件时创建文件所在路径失败！");
        File file = new File(saveDir + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        fos.close();
        inputStream.close();
    }

    /**
     * 保存字节流
     *
     * @param inputStream 文件流
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1)
            bos.write(buffer, 0, len);
        bos.close();
        return bos.toByteArray();
    }


    /**
     * 一个Object值转换为bool值，转化失败返回false
     */
    public static boolean ObjToBool(Object obj) {
        return ObjToBool(obj, false);
    }

    /**
     * 一个Object值转换为bool值，转化失败返回false
     */
    public static boolean ObjToBool(Object obj, boolean Del) {
        if (obj == null)
            return Del;
        try {
            return Boolean.parseBoolean(String.valueOf(obj));
        } catch (Exception e) {
            return Del;
        }
    }

    /**
     * 发送Https请求
     *
     * @param requestUrl 请求的地址
     */
    public static String getHttps(String requestUrl) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        return getHttps(requestUrl, "POST", null);
    }

    /**
     * 发送Https请求
     *
     * @param requestUrl 请求的地址
     * @param outputStr  请求的参数值
     */
    public static String getHttps(String requestUrl, String outputStr) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        return getHttps(requestUrl, "POST", outputStr);
    }

    /**
     * 发送Https请求
     *
     * @param requestUrl    请求的地址
     * @param requestMethod 请求的方式
     * @param outputStr     请求的参数值
     */
    public static String getHttps(String requestUrl, String requestMethod, String outputStr) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        StringBuilder buffer;
        SSLContext sslContext = SSLContext.getInstance("SSL");
        TrustManager[] tm = {new Tool()};
        sslContext.init(null, tm, new SecureRandom());
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        URL url = new URL(requestUrl);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod(requestMethod);
        conn.setSSLSocketFactory(ssf);
        conn.connect();
        if (null != outputStr && !outputStr.isEmpty()) {
            OutputStream os = conn.getOutputStream();
            os.write(outputStr.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
        InputStream is = conn.getInputStream();
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        buffer = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            buffer.append(line);
        return buffer.toString();
    }

    /**
     * 替换掉字符中的html标签
     */
    public static String delHtmlString(String htmlStr) {
        if (htmlStr == null || htmlStr.isEmpty())
            return htmlStr;
        htmlStr = htmlStr.replace("<p>", "\r\n\t").replace("<span>", "\r\n\t").replace("<br>", "\r\n").replace("</br>", "\r\n");
        Pattern p_script = Pattern.compile("<script[^>]*?>[\\s\\S]*?</script>", Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll("");
        Pattern p_style = Pattern.compile("<style[^>]*?>[\\s\\S]*?</style>", Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll("");
        Pattern p_html = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll("");
        return htmlStr.replaceAll("&nbsp;", "").trim();
    }

    /**
     * Https下载文件
     *
     * @param urlStr   要下载的文件链接
     * @param fileName 要保存的文件的名字
     * @param savePath 文件的保存位置
     */
    public static void downLoadFromUrlHttps(String urlStr, String fileName, String savePath) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        TrustManager[] tm = {new Tool()};
        sslContext.init(null, tm, new SecureRandom());
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        URL url = new URL(urlStr);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setHostnameVerifier(new Tool());
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setSSLSocketFactory(ssf);
        conn.connect();
        InputStream inputStream = conn.getInputStream();
        byte[] getData = readInputStream(inputStream);
        File saveDir = new File(savePath);
        if (!saveDir.exists()) if (!saveDir.mkdirs()) Log.e(Tag, "使用HTTPS下载文件时创建文件所在路径失败！");
        FileOutputStream fos = new FileOutputStream(new File(saveDir, fileName));
        fos.write(getData);
        fos.close();
        inputStream.close();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    @Override
    public boolean verify(String arg0, SSLSession arg1) {
        return true;
    }

    /**
     * 将未知参数转换为小数
     */
    public static Double ObjToDouble(Object obj) {
        return ObjToDouble(obj, 0d);
    }

    /**
     * 将未知参数转换为小数
     */
    public static Double ObjToDouble(Object obj, Double double1) {
        if (obj == null) return double1;
        double d;
        try {
            String S = String.valueOf(obj);
            if (S.isEmpty()) return double1;
            d = Double.parseDouble(S);
        } catch (Exception e) {
            return double1;
        }
        return d;
    }

    /**
     * 将Map按数据升序排列
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueAscending(Map<K, V> map) {
        List<Entry<K, V>> list = new LinkedList<>(map.entrySet());
        list.sort(Entry.comparingByValue());
        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list)
            result.put(entry.getKey(), entry.getValue());
        return result;
    }

    /**
     * 将Map降序排序
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDescending(Map<K, V> map) {
        List<Entry<K, V>> list = new LinkedList<>(map.entrySet());
        list.sort((a, b) -> -(a.getValue().compareTo(b.getValue())));
        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list)
            result.put(entry.getKey(), entry.getValue());
        return result;
    }

    /**
     * 返回一个随机数
     */
    public static double getRand(double d, double e) {
        return getRand(ObjToInt(d), ObjToInt(e));
    }
}
