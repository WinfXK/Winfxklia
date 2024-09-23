/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/20  下午4:02*/
package com.winfxk.winfxklia.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.winfxk.winfxklia.R;
import com.winfxk.winfxklia.tool.Tool;
import com.winfxk.winfxklia.view.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SuppressLint("SetTextI18n")
@SuppressWarnings("unused")
public class Loading extends BaseBuilder implements ImageView.ImageViewListener {
    private ProgressTextFormat format = (max, now, percent) -> max + "/" + now + "(" + percent + "%)";
    private final Animation alpha_show, alpha_hide;
    private final TextView title, loading, hint;
    private static List<String> defMotto = null;
    private volatile boolean isDismiss = false;
    private final ProgressBar progressBar;
    private volatile List<String> motto;
    private final Animation iconAnim;
    private boolean isShow = false;
    private int mottoSleep = 1000;
    private final ImageView icon;
    private int max, now;

    public Loading(@NonNull Context context) {
        this(context, context.getString(R.string.winfxkliba_info));
    }

    public Loading(@NonNull Context context, String title) {
        this(context, title, Type.INFO);
    }

    public Loading(@NonNull Context context, String title, Type type) {
        this(context, title, 100, type);
    }

    public Loading(@NonNull Context context, String title, int max, Type type) {
        this(context, title, max, 0, type);
    }

    public Loading(@NonNull Context context, String title, int max, int now, Type type) {
        super(context, R.style.winfxkliba_progress_dialog);
        if (defMotto == null)
            defMotto = Arrays.asList(context.getResources().getStringArray(R.array.winfxklia_loading_motto));
        iconAnim = AnimationUtils.loadAnimation(context, R.anim.winfxkliba_mydialog_icon);
        alpha_hide = AnimationUtils.loadAnimation(context, R.anim.winfxkliba_alpha_hide);
        alpha_show = AnimationUtils.loadAnimation(context, R.anim.winfxkliba_alpha_show);
        getWindow().setWindowAnimations(R.style.winfxkliba_progress_dialog);
        setContentView(R.layout.winfxkliba_loading);
        this.progressBar = findViewById(R.id.progressBar1);
        this.loading = findViewById(R.id.textView2);
        this.title = findViewById(R.id.textView1);
        this.icon = findViewById(R.id.imageView1);
        this.hint = findViewById(R.id.textView3);
        hint.setText(Tool.getRand(motto, ""));
        new Thread(this::motto).start();
        alpha_hide.setDuration(mottoSleep);
        alpha_hide.setFillAfter(true);
        alpha_show.setDuration(mottoSleep + 500);
        alpha_show.setFillAfter(true);
        this.icon.setListener(this);
        this.title.setText(title == null ? "" : title);
        this.motto = defMotto;
        setProgress(max, now);
        this.setIcon(type);
    }

    public ProgressTextFormat getFormat() {
        return format;
    }

    public void setFormat(ProgressTextFormat format) {
        this.format = format;
    }

    public int getMottoSleep() {
        return mottoSleep;
    }

    public void setMottoSleep(int mottoSleep) {
        this.mottoSleep = mottoSleep;
    }

    private void motto() {
        int index = 0, cache;
        while (!isDismiss) {
            Tool.sleep(2000);
            if (isDismiss) break;
            hint.startAnimation(alpha_hide);
            Tool.sleep(mottoSleep);
            if (isDismiss) break;
            hint.startAnimation(alpha_show);
            if (motto == null || motto.isEmpty()) {
                handler.post(() -> hint.setText(""));
                continue;
            }
            do cache = Tool.getRand(0, motto.size() - 1);
            while (motto.size() > 1 && cache == index);
            index = cache;
            String string = motto.get(index);
            if (string == null || string.isEmpty()) {
                handler.post(() -> hint.setText(""));
                continue;
            }
            handler.post(() -> hint.setText(string));
            Tool.sleep(200L * string.length());
        }
    }

    @Override
    public void show() {
        if (isShow) throw new IllegalStateException("请勿重复显示");
        isShow = true;
        handler.post(() -> {
            try {
                super.show();
            } catch (Exception e) {
                Log.e(getTAG(), "在尝试显示对话框时出现异常！", e);
            }
        });
    }

    public void setMotto(List<String> motto) {
        this.motto = motto;
    }

    public List<String> getMotto() {
        return motto;
    }

    @Override
    public void dismiss() {
        isDismiss = true;
        handler.post(super::dismiss);
    }

    public void setTitle(String title) {
        handler.post(() -> this.title.setText(title));
    }

    public void setMax(int max) {
        handler.post(() -> {
            setProgressText();
            this.progressBar.setMax(this.max = max);
        });
    }

    private void setProgressText() {
        loading.setText(format.getTextFormat(max, now, Math.max(0, Math.min(100, max == 0 ? 0 : Tool.Double2((double) now / max * 100, 2)))));
    }

    public void setProgress(int now) {
        handler.post(() -> {
            setProgressText();
            this.progressBar.setProgress(this.now = now);
        });
    }

    public void setProgress(int max, int now) {
        handler.post(() -> {
            setProgressText();
            this.progressBar.setMax(this.max = max);
            this.progressBar.setProgress(this.now = now);
        });
    }

    public Loading setIcon(Bitmap icon) {
        handler.post(() -> {
            this.icon.startAnimation(iconAnim);
            this.icon.setImageBitmap(icon);
        });
        return this;
    }


    public Loading setIcon(Type state) {
        return setIcon(state.getIcon());
    }

    public Loading setIcon(int icon) {
        handler.post(() -> {
            this.icon.startAnimation(iconAnim);
            this.icon.setImageResource(icon);
        });
        return this;
    }

    public Loading setIcon(String url) {
        this.icon.setImageURL(url);
        return this;
    }

    public Loading setIcon(File file) throws IOException {
        return setIcon(BitmapFactory.decodeStream(new FileInputStream(file)));
    }

    @Override
    public void onListener(boolean isSucceed) {
        handler.post(() -> this.icon.startAnimation(iconAnim));
    }

    public static Loading make(Context context) {
        return make(context, "提示");
    }

    public static Loading make(Context context, String title) {
        return make(context, title, Type.INFO, 100);
    }

    public static Loading make(Context context, int max) {
        return make(context, null, Type.INFO, max);
    }

    public static Loading make(Context context, String title, Type icon, int max) {
        return new Loading(context, title == null ? "提示" : title, max, 0, icon);
    }

    public interface ProgressTextFormat {
        String getTextFormat(long max, long now, double percent);
    }
}
