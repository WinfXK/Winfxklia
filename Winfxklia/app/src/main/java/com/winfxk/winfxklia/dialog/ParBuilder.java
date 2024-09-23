package com.winfxk.winfxklia.dialog;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import com.winfxk.winfxklia.R;
import com.winfxk.winfxklia.view.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * author: Winfx
 * date: 2024/5/5 16:33
 */
@SuppressWarnings("unused")
public class ParBuilder extends BaseBuilder implements ImageView.ImageViewListener {
    private final List<OnClickListener> allListeners = new ArrayList<>();
    private final Animation iconAnim, alpha_show, alpha_hide;
    private final List<Button> buttons = new ArrayList<>();
    private boolean isClickOnClose = true;
    private final ProgressBar progressBar;
    private final LinearLayout line1;
    private final TextView message;
    private final Context context;
    private final ImageView icon;
    private final TextView title;
    private boolean isShow = false;
    private Type type;

    public ParBuilder(@NonNull Context context) {
        this(context, Type.INFO);
    }

    public ParBuilder(@NonNull Context context, Type type) {
        this(context, "提示", "", type);
    }

    public ParBuilder(@NonNull Context context, String title, String message) {
        this(context, title, message, Type.INFO);
    }

    public ParBuilder(@NonNull Context context, String title, String message, Type type) {
        super(context, R.style.winfxkliba_progress_dialog);
        setContentView(R.layout.winfxkliba_progress_dialog);
        iconAnim = AnimationUtils.loadAnimation(context, R.anim.winfxkliba_progress_dialog_icon);
        alpha_hide = AnimationUtils.loadAnimation(context, R.anim.winfxkliba_alpha_hide);
        alpha_show = AnimationUtils.loadAnimation(context, R.anim.winfxkliba_alpha_show);
        getWindow().setWindowAnimations(R.style.winfxkliba_progress_dialog);
        this.progressBar = findViewById(R.id.progressBar);
        this.message = findViewById(R.id.textView3);
        this.title = findViewById(R.id.textView1);
        icon = findViewById(R.id.imageView1);
        line1 = findViewById(R.id.line1);
        alpha_hide.setFillAfter(true);
        alpha_show.setFillAfter(true);
        this.message.setText(message);
        this.icon.setListener(this);
        setIcon(this.type = type);
        this.title.setText(title);
        this.context = context;
    }

    public void setNotClose() {
        isClickOnClose = false;
    }

    public ParBuilder addButton(String text) {
        return addButton(text, null);
    }

    public ParBuilder addButton(String text, OnClickListener listener) {
        return addButton(text, listener, Build.VERSION.SDK_INT >= 23 ? context.getColor(R.color.winfxkliba_black) : context.getResources().getColor(R.color.winfxkliba_black));
    }

    public ParBuilder addButton(String text, OnClickListener listener, int fontColor) {
        Button button = getButton(text);
        button.setTextColor(fontColor);
        button.setOnClickListener(v -> onClick(v, listener));
        if (!isShow) buttons.add(button);
        else handler.post(() -> line1.addView(button));
        return this;
    }

    public void show() {
        if (isShow) throw new IllegalStateException("Dialog show already");
        handler.post(() -> {
            for (Button button : buttons) line1.addView(button);
            setType(type);
            try {
                super.show();
            } catch (Exception e) {
                Log.e(getTAG(), "在尝试显示对话框时出现异常！", e);
            }
            isShow = true;
        });
    }

    public ParBuilder setIcon(Type type) {
        int id = type.getIcon();
        handler.post(() -> {
            if (type.equals(Type.Empty)) {
                if (progressBar.getAlpha() > 0) progressBar.startAnimation(alpha_hide);
                if (icon.getAlpha() > 0) icon.startAnimation(alpha_hide);
            } else if (type.equals(Type.Progress)) {
                if (icon.getAlpha() > 0) icon.startAnimation(alpha_hide);
                if (progressBar.getAlpha() > 0) progressBar.startAnimation(iconAnim);
                else progressBar.startAnimation(alpha_show);
            } else {
                if (progressBar.getAlpha() > 0) progressBar.startAnimation(alpha_hide);
                if (icon.getAlpha() > 0) icon.startAnimation(iconAnim);
                else icon.startAnimation(alpha_show);
                this.icon.setImageResource(id);
            }
        });
        this.type = type;
        return this;
    }

    public ParBuilder setIcon(String url) {
        handler.post(() -> {
            if (type.equals(Type.Empty) || type.equals(Type.Progress)) {
                progressBar.setVisibility(View.GONE);
                icon.setVisibility(View.VISIBLE);
            }
            this.type = Type.Image;
            icon.setImageURL(url);
        });
        return this;
    }

    public ParBuilder setIcon(File file) throws IOException {
        return setIcon(new FileInputStream(file));
    }

    public ParBuilder setIcon(InputStream is) {
        return setIcon(BitmapFactory.decodeStream(is));
    }

    public ParBuilder setIcon(Bitmap icon) {
        this.type = Type.Image;
        handler.post(() -> {
            if (type.equals(Type.Empty) || type.equals(Type.Progress)) {
                this.progressBar.setVisibility(View.GONE);
                this.icon.setVisibility(View.VISIBLE);
            }
            this.icon.startAnimation(iconAnim);
            this.icon.setImageBitmap(icon);
        });
        return this;
    }

    public ParBuilder removeAllClickListener(OnClickListener listener) {
        if (listener == null) return this;
        allListeners.remove(listener);
        return this;
    }

    public ParBuilder clearAllClickListener() {
        allListeners.clear();
        return this;
    }

    public ParBuilder addAllClickListener(OnClickListener listener) {
        if (listener == null) return this;
        allListeners.add(listener);
        return this;
    }

    private void onClick(View view, OnClickListener listener) {
        if (listener != null) {
            isClickOnClose = true;
            listener.onClick(this);
            if (isClickOnClose) dismiss();
        } else dismiss();
        for (OnClickListener listener1 : allListeners)
            if (listener1 != null) listener1.onClick(this);
    }

    public ParBuilder setButton(int index, OnClickListener listener) {
        if (buttons.size() <= index) throw new IllegalArgumentException("index is too large");
        Button button = buttons.get(index);
        handler.post(() -> button.setOnClickListener(v -> onClick(v, listener)));
        return this;
    }

    @Override
    public void dismiss() {
        handler.post(super::dismiss);
    }

    public static Handler getHandler() {
        return handler;
    }

    public ParBuilder setButton(String text, OnClickListener listener) {
        List<Button> list = new ArrayList<>();
        for (Button button : buttons) if (button.getText().equals(text)) list.add(button);
        if (!list.isEmpty()) handler.post(() -> {
            for (Button button : list) button.setOnClickListener(v -> onClick(v, listener));
        });
        else addButton(text, listener);
        return this;
    }

    private Button getButton(String text) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                context.getResources().getDimensionPixelSize(R.dimen.winfxkliba_parbuilder_button_h), 1.0f);
        layoutParams.setMargins(0, 5, 0, 0);
        Button button = new Button(context, null);
        button.setText(text);
        button.setTextColor(Color.BLACK);
        button.setBackground(AppCompatResources.getDrawable(context, R.drawable.winfxkliba_dialog_button));
        button.setLayoutParams(layoutParams);
        button.setPadding(0, 0, 0, 0);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator stateListAnimator = new StateListAnimator();
            stateListAnimator.addState(new int[]{}, ObjectAnimator.ofFloat(button, "elevation", 0f));
            button.setStateListAnimator(stateListAnimator);
        }
        return button;
    }

    public ParBuilder clearButtons() {
        buttons.clear();
        if (isShow) handler.post(line1::removeAllViews);
        return this;
    }

    public ParBuilder removeButton(String text) {
        if (Build.VERSION.SDK_INT >= 24)
            buttons.removeIf(button -> button.getText().equals(text));
        else for (Button button : new ArrayList<>(buttons))
            if (button.getText().equals(text)) line1.removeView(button);
        if (isShow) handler.post(() -> {
            line1.removeAllViews();
            for (Button button : buttons) line1.addView(button);
        });
        return this;
    }

    public ParBuilder removeButton(int index) {
        if (buttons.size() <= index) throw new IllegalArgumentException("index is too large");
        Button button = buttons.remove(index);
        if (isShow) handler.post(() -> line1.removeView(button));
        return this;
    }

    public String getMessage() {
        return message.getText().toString();
    }

    private long timeline = 0;

    public ParBuilder setMessage(String message) {
        long time = System.currentTimeMillis();
        handler.post(() -> {
            if (Math.abs(time - timeline) > 1000)
                this.message.startAnimation(alpha_show);
            this.message.setText(message);
            timeline = time;
        });
        return this;
    }

    public String getTitle() {
        return title.getText().toString();
    }

    public ParBuilder setTitle(String title) {
        handler.post(() -> this.title.setText(title));
        return this;
    }

    public Type getType() {
        return type;
    }

    public ParBuilder setType(Type type) {
        return setIcon(type);
    }

    @Override
    public void onListener(boolean isSucceed) {
        if (!isSucceed) icon.setVisibility(View.GONE);
        else icon.startAnimation(iconAnim);
    }

    public interface OnClickListener {
        void onClick(ParBuilder builder);
    }

    public static ParBuilder make(Context context) {
        return make(context, "");
    }

    public static ParBuilder make(Context context, String message) {
        return make(context, "提示", message);
    }

    public static ParBuilder make(Context context, String title, String message) {
        return make(context, title, message, Type.INFO);
    }

    public static ParBuilder make(Context context, String title, String message, Type type) {
        return new ParBuilder(context, title, message, type);
    }
}
