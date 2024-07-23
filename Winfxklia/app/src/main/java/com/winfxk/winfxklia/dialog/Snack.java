/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/6  上午8:43*/
package com.winfxk.winfxklia.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;
@SuppressWarnings("unused")
public class Snack implements View.OnClickListener {
    private final static Handler handler = new Handler(Looper.getMainLooper());
    public static final int LENGTH_SHORT = -1;
    public static final int LENGTH_LONG = 0;
    private OnClickListener listener;
    private final Context context;
    private Snackbar snackbar;

    private Snack(Context context) {
        if (context == null) throw new IllegalStateException("Context is null");
        this.context = context;
        listener = Snack::dismiss;
    }

    public Context getContext() {
        return context;
    }

    public Snack show() {
        handler.post(this::showSnack);
        return this;
    }

    private void showSnack() {
        snackbar.show();
    }

    public Snack dismiss() {
        snackbar.dismiss();
        return this;
    }

    public static Snack makeText(Activity activity, String message) {
        return makeText(activity, activity.findViewById(android.R.id.content), LENGTH_LONG, message);
    }

    public static Snack makeText(Context context, View anchorView, int time, String message) {
        return makeText(context, anchorView, time, message, null);
    }

    public static Snack makeText(Context context, View anchorView, int time, String message, String close) {
        Snack snack = new Snack(context);
        snack.snackbar = Snackbar.make(context, anchorView, message, time);
        if (close != null) snack.snackbar.setAction(close, snack);
        return snack;
    }

    public Snack setAction(String close, OnClickListener listener) {
        this.listener = listener;
        if (close != null) snackbar.setAction(close, this);
        return this;
    }

    public Snack setAction(String close) {
        if (close != null) snackbar.setAction(close, this);
        return this;
    }

    @Override
    public void onClick(View v) {
        listener.onClick(this);
    }

    public interface OnClickListener {
        void onClick(Snack snack);
    }
}
