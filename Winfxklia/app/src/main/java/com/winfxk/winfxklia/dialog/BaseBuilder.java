/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/15  上午8:44*/
package com.winfxk.winfxklia.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.winfxk.winfxklia.R;
import com.winfxk.winfxklia.tool.Tool;
import com.winfxk.winfxklia.tool.able.Tabable;

@SuppressWarnings("unused")
public abstract class BaseBuilder extends Dialog implements Tabable {
    protected CloseListener closeListener;
    protected static final int[] Animations = {R.style.winfxkliba_alert_dialog1, R.style.winfxkliba_alert_dialog2};
    protected static final Handler handler = new Handler(Looper.getMainLooper());
    protected volatile boolean isClose = false;
    private volatile Object value;

    public BaseBuilder(@NonNull Context context, Integer style) {
        super(context, style == null ? Animations[Tool.getRand(0, MyBuilder.Animations.length - 1)] : style);
        getWindow().setWindowAnimations(Animations[Tool.getRand(0, Animations.length - 1)]);
        setCancelable(false);
    }

    public BaseBuilder(@NonNull Context context) {
        this(context, null);
    }

    public BaseBuilder(@NonNull Context context, boolean isCancelable) {
        this(context, null);
        setCancelable(isCancelable);
    }

    @Deprecated
    @Override
    public void dismiss() {
        dismiss(true);
    }

    public void dismiss(boolean isClickClosed) {
        if (closeListener != null) closeListener.onBuilderClosed(this, isClickClosed);
        super.dismiss();
        isClose = true;
    }

    public CloseListener getCloseListener() {
        return closeListener;
    }

    /**
     * @param closeListener 窗口关闭事件
     */
    public void setCloseListener(CloseListener closeListener) {
        this.closeListener = closeListener;
    }

    /**
     * @return 窗口是否已经关闭
     */
    public boolean isClose() {
        return isClose;
    }

    public Object getValue() {
        return value;
    }

    /**
     * 存放一个捆绑数据于Dialog
     *
     * @param value 需要存放的数据
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
