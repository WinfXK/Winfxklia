package com.winfxk.winfxklia.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.winfxk.winfxklia.BaseActivity;
import com.winfxk.winfxklia.R;
import com.winfxk.winfxklia.http.BaseHttp;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * author: Winfx
 * date: 2024/5/3 21:23
 */
@SuppressWarnings("unused")
public class ImageView extends androidx.appcompat.widget.AppCompatImageView {
    private static final HandlerThread thread = new HandlerThread("MyImageView Thread");
    private final List<String> cookies = new ArrayList<>();
    private static DisplayMetrics metrics = null;
    private volatile boolean isload = false;
    private static final Handler handler;
    private ImageViewListener listener;
    private static int densityDpi;
    private final Http http;

    protected static class Http extends BaseHttp {
        private Http(Context c) {
            super(c);
        }

        @Override
        protected void initializeCookies(String host) {
            super.initializeCookies(host);
        }

        @Override
        protected List<String> getSaveCookies() {
            return super.getSaveCookies();
        }

        @Override
        protected boolean saveCookies(String host, List<String> cookies) {
            return super.saveCookies(host, cookies);
        }
    }

    static {
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    public ImageView(@NonNull @NotNull Context context) {
        this(context, null);
    }

    public ImageView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (metrics == null) {
            metrics = context.getResources().getDisplayMetrics();
            densityDpi = (int) (metrics.density * 160f);
        }
        http = new Http(context);
    }

    public void setImageFile(@NotNull File file) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(options);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        setImageBitmap(BitmapFactory.decodeStream(Files.newInputStream(file.toPath()), null, options));
    }

    public void setImageURL(@NotNull String url) {
        setImageURL(url, null);
    }

    public void setImageURL(@NotNull String url, List<String> cookies) {
        if (isload) return;
        isload = true;
        if (cookies != null && !cookies.isEmpty()) addCookies(cookies);
        setImageResource(R.drawable.winfxkliba_loading);
        handler.post(() -> this.run(url));
    }

    private String getTAG() {
        Context context = getContext();
        if (context instanceof BaseActivity)
            return ((BaseActivity) context).getTAG() + " ImageView";
        return context.getClass().getSimpleName() + " ImageView";
    }

    private void run(String string) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(string);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            http.initializeCookies(url.getHost());
            int code = connection.getResponseCode();
            if (code == 200) {
                inputStream = connection.getInputStream();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = calculateInSampleSize(options);
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                connection = (HttpURLConnection) url.openConnection();
                List<String> list = new ArrayList<>(cookies);
                if (http.getSaveCookies() != null) list.addAll(http.getSaveCookies());
                connection.setRequestProperty("Cookie", String.join("; ", list));
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(60000);
                connection.setDoInput(true);
                connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
                connection.setRequestProperty("Connection", "keep-alive");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
                inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                String stringCookie = connection.getHeaderField("Set-Cookie");
                if (stringCookie != null) {
                    list.clear();
                    list.addAll(Arrays.asList(stringCookie.split("; ")));
                    http.saveCookies(url.getHost(), cookies);
                }
                post(() -> {
                    if (listener != null) listener.onListener(true);
                    setImageBitmap(bitmap);
                });
            } else post(() -> {
                Log.e(getTAG(), "网络图片\"" + string + "\"获取失败！");
                if (listener != null) listener.onListener(false);
                setImageResource(R.drawable.winfxkliba_wifi_sb);
            });
        } catch (Exception e) {
            Log.e(getTAG(), "网络图片\"" + string + "\"获取失败！", e);
            post(() -> {
                if (listener != null) listener.onListener(false);
                setImageResource(R.drawable.winfxkliba_wifi_sb);
            });
        } finally {
            isload = false;
            if (connection != null) try {
                connection.disconnect();
            } catch (Exception e) {
                Log.w(getTAG(), e);
            }
            if (inputStream != null) try {
                inputStream.close();
            } catch (Exception e) {
                Log.w(getTAG(), e);
            }
        }
    }

    public ImageViewListener getListener() {
        return listener;
    }

    public void setListener(ImageViewListener listener) {
        this.listener = listener;
    }

    private int calculateInSampleSize(BitmapFactory.Options options) {
        int inSampleSize = 1;
        final int width = options.outWidth;
        final int height = options.outHeight;
        while ((width / inSampleSize) >= densityDpi && (height / inSampleSize) >= densityDpi)
            inSampleSize *= 2;
        return inSampleSize;
    }

    public Bitmap getImageBitmap() {
        Drawable drawable = getDrawable();
        if (!(drawable instanceof BitmapDrawable)) return null;
        return ((BitmapDrawable) drawable).getBitmap();
    }

    public interface ImageViewListener {
        void onListener(boolean isSucceed);
    }

    public List<String> setCookies(List<String> list) {
        if (list == null) list = new ArrayList<>();
        cookies.clear();
        cookies.addAll(list);
        return cookies;
    }

    public List<String> addCookies(List<String> list) {
        if (list == null) return null;
        cookies.addAll(list);
        return cookies;
    }

    public void clearCookies() {
        cookies.clear();
    }

    public List<String> getCookies() {
        return cookies;
    }
}
