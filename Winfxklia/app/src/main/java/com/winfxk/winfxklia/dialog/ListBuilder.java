package com.winfxk.winfxklia.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import com.winfxk.winfxklia.R;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Winfx
 * date: 2024/5/4 8:58
 */
@SuppressWarnings("unused")
public class ListBuilder extends BaseBuilder {
    private final List<OnClickListener> allListeners = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private OnClickListener cancelListener;
    private boolean isClickOnClose = true;
    private final LinearLayout view;
    private boolean isShow = false;
    private final Context context;
    private final Button cancel;

    public ListBuilder(@NonNull Context context) {
        this(context, false);
    }

    public ListBuilder(@NonNull Context context, boolean Cancelable) {
        this(context, Cancelable, (Item) null);
    }

    public ListBuilder(@NonNull Context context, boolean Cancelable, Item... items) {
        super(context, R.style.winfxkliba_listDialog);
        this.context = context;
        setContentView(R.layout.winfxkliba_list_dialog);
        cancel = findViewById(R.id.button1);
        view = findViewById(R.id.line1);
        cancel.setOnClickListener(this::onCancelClick);
        if (items != null) for (Item item : items)
            if (item != null) addButton(item.getText(), item.getListener());
        getWindow().setWindowAnimations(R.style.winfxkliba_listDialog);
    }

    public Button getButton(int index) {
        if (index >= buttons.size()) return null;
        return buttons.get(index);
    }

    public void setNotClose() {
        isClickOnClose = false;
    }

    @Override
    public void show() {
        if (isShow) throw new IllegalStateException("Dialog show already");
        handler.post(() -> {
            view.removeAllViews();
            for (Button button : buttons) view.addView(button);
            try {
                super.show();
            } catch (Exception e) {
                Log.e(getTAG(), "在尝试显示对话框时出现异常！", e);
            }
        });
        isShow = true;
    }

    public ListBuilder setButton(int index, OnClickListener listener) {
        if (buttons.size() <= index) throw new IllegalArgumentException("index is too large");
        Button button = buttons.get(index);
        handler.post(() -> button.setOnClickListener(v -> onClick(v, listener)));
        return this;
    }

    public ListBuilder setButton(String text, OnClickListener listener) {
        List<Button> list = new ArrayList<>();
        for (Button button : buttons)
            if (button.getText().equals(text)) list.add(button);
        if (!list.isEmpty()) handler.post(() -> {
            for (Button button : list) button.setOnClickListener(v -> onClick(v, listener));
        });
        else addButton(text, listener);
        return this;
    }

    public ListBuilder removeAllClickListener(OnClickListener listener) {
        if (listener == null) return this;
        allListeners.remove(listener);
        return this;
    }

    public ListBuilder clearAllClickListener() {
        allListeners.clear();
        return this;
    }

    public ListBuilder addAllClickListener(OnClickListener listener) {
        if (listener == null) return this;
        allListeners.add(listener);
        return this;
    }

    public List<Button> getButtons() {
        return new ArrayList<>(buttons);
    }

    private void onCancelClick(View v) {
        if (cancelListener != null) cancelListener.onClick(this);
        dismiss();
    }

    @Override
    public void dismiss() {
        handler.post(super::dismiss);
    }

    public static Handler getHandler() {
        return handler;
    }

    public ListBuilder setCancel(String cancel) {
        handler.post(() -> this.cancel.setText(cancel));
        return this;
    }

    public ListBuilder setCancel(String cancel, OnClickListener listener) {
        handler.post(() -> this.cancel.setText(cancel));
        this.cancelListener = listener;
        return this;
    }

    public ListBuilder addButton(String text) {
        return addButton(text, Dialog::dismiss);
    }

    public ListBuilder addButton(String text, OnClickListener listener) {
        return addButton(text, listener, Build.VERSION.SDK_INT >= 23 ? context.getColor(R.color.winfxkliba_black) : context.getResources().getColor(R.color.winfxkliba_black));
    }

    public ListBuilder addButton(String text, OnClickListener listener, int fontColor) {
        Button button = getButton(text);
        button.setTextColor(fontColor);
        button.setOnClickListener(v -> onClick(v, listener));
        if (!isShow) buttons.add(button);
        else handler.post(() -> view.addView(button));
        return this;
    }

    public ListBuilder clearButtons() {
        buttons.clear();
        if (isShow) handler.post(view::removeAllViews);
        return this;
    }

    public ListBuilder removeButton(int index) {
        buttons.remove(index);
        if (isShow) handler.post(() -> {
            view.removeAllViews();
            for (Button button : buttons) view.addView(button);
        });
        return this;
    }

    public ListBuilder removeButton(String text) {
        if (Build.VERSION.SDK_INT >= 24)
            buttons.removeIf(button -> button.getText().equals(text));
        else for (Button button : new ArrayList<>(buttons))
            if (button.getText().equals(text)) buttons.remove(button);
        if (isShow) handler.post(() -> {
            view.removeAllViews();
            for (Button button : buttons) view.addView(button);
        });
        return this;
    }

    private void onClick(View view, OnClickListener listener) {
        if (listener != null) {
            isClickOnClose = true;
            listener.onClick(this);
            if (isClickOnClose) dismiss();
        }
        for (OnClickListener listener1 : allListeners)
            if (listener1 != null) listener1.onClick(this);
    }

    private Button getButton(String text) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                cancel.getResources().getDimensionPixelSize(R.dimen.winfxkliba_listbuilder_item_h));
        layoutParams.setMargins(0, cancel.getResources().getDimensionPixelSize(R.dimen.winfxkliba_listbuilder_item_p), 0, 1);
        Button button = new Button(context, null);
        button.setText(text);
        button.setTextColor(0xAA3333FF);
        button.setTextSize(14);
        button.setBackground(AppCompatResources.getDrawable(context, R.drawable.winfxkliba_listdialog_button));
        button.setLayoutParams(layoutParams);
        button.setHeight(cancel.getResources().getDimensionPixelSize(R.dimen.winfxkliba_listbuilder_item_h));
        button.setPadding(0, 0, 0, 0);
        return button;
    }

    public static class Item {
        private final String text;
        private final OnClickListener listener;

        public Item(String text, OnClickListener listener) {
            this.text = text;
            this.listener = listener;
        }

        public OnClickListener getListener() {
            return listener;
        }

        public String getText() {
            return text;
        }
    }

    public interface OnClickListener {
        void onClick(ListBuilder builder);
    }

    public static ListBuilder make(Context context) {
        return new ListBuilder(context);
    }

    public static ListBuilder make(Context context, Item... items) {
        return new ListBuilder(context, false, items);
    }
}
