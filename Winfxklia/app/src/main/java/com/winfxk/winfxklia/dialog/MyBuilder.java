package com.winfxk.winfxklia.dialog;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import com.winfxk.winfxklia.R;
import com.winfxk.winfxklia.tool.Tool;
import com.winfxk.winfxklia.view.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * author: Winfx
 * date: 2024/5/3 22:12
 */
@SuppressWarnings("unused")
public class MyBuilder extends BaseBuilder implements ImageView.ImageViewListener {
    private final List<OnClickListener> allListeners = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private boolean isClickOnClose = true;
    private final RelativeLayout view;
    private final Animation iconAnim;
    private final LinearLayout line2;
    private boolean isShow = false;
    private final TextView message;
    private final Context context;
    private final ImageView icon;
    private final TextView title;

    public MyBuilder(@NonNull Context context, Type state) {
        this(context, state, context.getString(R.string.winfxkliba_info), "");
    }

    public MyBuilder(@NonNull Context context) {
        this(context, context.getString(R.string.winfxkliba_info), "");
    }

    public MyBuilder(@NonNull Context context, String title, String message) {
        this(context, R.drawable.winfxkliba_info2, title, message);
    }

    public MyBuilder(@NonNull Context context, Type state, String title, String message) {
        this(context, state, title, message, false);
    }

    public MyBuilder(@NonNull Context context, Type state, String title, String message, boolean Cancelable) {
        this(context, state.getIcon(), title, message, Cancelable);
    }

    public MyBuilder(@NonNull Context context, int IconID, String title, String message) {
        this(context, IconID, title, message, false);
    }

    public MyBuilder(@NonNull Context context, int IconID, String title, String message, boolean Cancelable) {
        super(context, Animations[Tool.getRand(0, Animations.length - 1)]);
        iconAnim = AnimationUtils.loadAnimation(context, R.anim.winfxkliba_mydialog_icon);
        this.context = context;
        setContentView(R.layout.winfxkliba_alert_dialog);
        this.icon = findViewById(R.id.imageView1);
        this.title = findViewById(R.id.textView1);
        this.view = findViewById(R.id.view);
        this.message = findViewById(R.id.textView3);
        this.line2 = findViewById(R.id.line2);
        this.icon.setImageResource(IconID);
        this.icon.setListener(this);
        this.title.setText(title);
        this.message.setText(message);
        setCancelable(Cancelable);
        getWindow().setWindowAnimations(Animations[Tool.getRand(0, Animations.length - 1)]);
    }

    public MyBuilder setType(Type type) {
        return setIcon(type);
    }

    public MyBuilder clearButtons() {
        buttons.clear();
        if (isShow) handler.post(line2::removeAllViews);
        return this;
    }

    public MyBuilder removeButton(int index) {
        Button button = buttons.remove(index);
        if (isShow) handler.post(() -> line2.removeView(button));
        return this;
    }

    public MyBuilder removeButton(String text) {
        if (Build.VERSION.SDK_INT >= 24) buttons.removeIf(button -> button.getText().equals(text));
        else for (Button button : new ArrayList<>(buttons))
            if (button.getText().equals(text)) {
                buttons.remove(button);
                break;
            }
        if (isShow) handler.post(() -> {
            view.removeAllViews();
            for (Button button : buttons) view.addView(button);
        });
        return this;
    }

    @Override
    public void show() {
        if (isShow) throw new IllegalStateException("Dialog show already");
        isShow = true;
        handler.post(() -> {
            for (Button button : buttons) line2.addView(button);
            this.icon.startAnimation(iconAnim);
            try {
                super.show();
            } catch (Exception e) {
                Log.e(getTAG(), "在尝试显示对话框时出现异常！", e);
            }
        });
    }

    public MyBuilder setButton(int index, OnClickListener listener) {
        if (buttons.size() <= index) throw new IllegalArgumentException("index is too large");
        Button button = buttons.get(index);
        handler.post(() -> button.setOnClickListener(v -> onClick(v, listener)));
        return this;
    }

    public MyBuilder setButton(String text, OnClickListener listener) {
        List<Button> list = new ArrayList<>();
        for (Button button : buttons)
            if (button.getText().equals(text)) list.add(button);
        if (!list.isEmpty()) handler.post(() -> {
            for (Button button : list) button.setOnClickListener(v -> onClick(v, listener));
        });
        else addButton(text, listener);
        return this;
    }

    public static Handler getHandler() {
        return handler;
    }

    @Override
    public void dismiss() {
        handler.post(super::dismiss);
    }

    public MyBuilder removeAllClickListener(OnClickListener listener) {
        if (listener == null) return this;
        allListeners.remove(listener);
        return this;
    }

    public MyBuilder clearAllClickListener() {
        allListeners.clear();
        return this;
    }

    public MyBuilder addAllClickListener(OnClickListener listener) {
        if (listener == null) return this;
        allListeners.add(listener);
        return this;
    }

    public MyBuilder setView(View view) {
        handler.post(() -> {
            this.view.removeAllViews();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            view.setLayoutParams(params);
            view.setMinimumHeight(this.view.getMinimumHeight());
            this.view.addView(view);
        });
        return this;
    }

    public String getTitle() {
        return title.getText().toString();
    }

    public Drawable getIcon() {
        return icon.getDrawable();
    }

    public String getMessage() {
        return message.getText().toString();
    }

    public Button getButton(int index) {
        if (index >= buttons.size()) return null;
        return buttons.get(index);
    }

    public List<Button> getButtons() {
        return new ArrayList<>(buttons);
    }

    public MyBuilder setIcon(Bitmap icon) {
        handler.post(() -> {
            this.icon.startAnimation(iconAnim);
            this.icon.setImageBitmap(icon);
        });
        return this;
    }

    public MyBuilder setIcon(Type state) {
        return setIcon(state.getIcon());
    }

    public MyBuilder setIcon(int icon) {
        handler.post(() -> {
            this.icon.startAnimation(iconAnim);
            this.icon.setImageResource(icon);
        });
        return this;
    }

    public MyBuilder setIcon(String url) {
        this.icon.setImageURL(url);
        return this;
    }

    public MyBuilder setIcon(File file) throws IOException {
        return setIcon(BitmapFactory.decodeStream(new FileInputStream(file)));
    }

    public MyBuilder setMessage(int messageId) {
        handler.post(() -> this.message.setText(context.getString(messageId)));
        return this;
    }

    public MyBuilder setMessage(String message) {
        handler.post(() -> this.message.setText(message));
        return this;
    }

    public MyBuilder setTitle(String title) {
        handler.post(() -> this.title.setText(title));
        return this;
    }

    @Override
    public void setTitle(int titleId) {
        handler.post(() -> title.setText(context.getString(titleId)));
    }

    public MyBuilder addButton(String text) {
        return addButton(text, Dialog::dismiss);
    }

    public MyBuilder addButton(String text, OnClickListener listener) {
        int color = Build.VERSION.SDK_INT >= 23 ? context.getColor(R.color.winfxkliba_black)
                : context.getResources().getColor(R.color.winfxkliba_black);
        return addButton(text, listener, color);
    }

    public MyBuilder addButton(String text, OnClickListener listener, int fontColor) {
        Button button = getButton(text);
        button.setTextColor(fontColor);
        button.setOnClickListener(v -> onClick(v, listener));
        buttons.add(button);
        if (isShow) handler.post(() -> line2.addView(button));
        return this;
    }


    private void onClick(View v, OnClickListener listener) {
        if (listener != null) {
            isClickOnClose = true;
            listener.onClick(this);
            if (isClickOnClose) dismiss();
        }
        for (OnClickListener listener1 : allListeners)
            if (listener1 != null) listener1.onClick(this);
    }

    private Button getButton(String text) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f
        );
        layoutParams.setMargins(0, 5, 0, 0);
        Button button = new Button(context, null);
        button.setText(text);
        button.setTextColor(Color.BLACK);
        button.setLayoutParams(layoutParams);
        button.setPadding(0, 0, 0, 0);
        button.setBackground(AppCompatResources.getDrawable(context, R.drawable.winfxkliba_dialog_button));
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator stateListAnimator = new StateListAnimator();
            stateListAnimator.addState(new int[]{}, ObjectAnimator.ofFloat(button, "elevation", 0f));
            button.setStateListAnimator(stateListAnimator);
        }
        return button;
    }

    @Override
    public void onListener(boolean isSucceed) {
        this.icon.startAnimation(iconAnim);
    }

    public void setNotClose() {
        isClickOnClose = false;
    }

    public interface OnClickListener {
        void onClick(MyBuilder builder);
    }

    public static MyBuilder make(Context context) {
        return make(context, "");
    }

    public static MyBuilder make(Context context, String message) {
        return make(context, "提示", message);
    }

    public static MyBuilder make(Context context, String title, String message) {
        return make(context, title, message, Type.INFO);
    }

    public static MyBuilder make(Context context, String title, String message, Type type) {
        return new MyBuilder(context, title, message).setIcon(type);
    }
}
