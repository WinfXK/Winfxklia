package com.winfxk.winfxklia.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.winfxk.winfxklia.R;
import com.winfxk.winfxklia.tool.Tool;

/**
 * @author Winfxk
 */
@SuppressWarnings("unused")
@SuppressLint("InflateParams")
public class Toast {
    public static final int[] Images1 = {
            R.drawable.winfxkliba_toast_icon1, R.drawable.winfxkliba_toast_icon2,
            R.drawable.winfxkliba_toast_icon3, R.drawable.winfxkliba_toast_icon4,
            R.drawable.winfxkliba_toast_icon5, R.drawable.winfxkliba_toast_icon6,
            R.drawable.winfxkliba_toast_icon7, R.drawable.winfxkliba_toast_icon8,
            R.drawable.winfxkliba_toast_icon9, R.drawable.winfxkliba_toast_icon10,
            R.drawable.winfxkliba_toast_icon11, R.drawable.winfxkliba_toast_icon12,
            R.drawable.winfxkliba_toast_icon13, R.drawable.winfxkliba_toast_icon14,
            R.drawable.winfxkliba_toast_icon15, R.drawable.winfxkliba_toast_icon16,
            R.drawable.winfxkliba_toast_icon17, R.drawable.winfxkliba_toast_icon18,
            R.drawable.winfxkliba_toast_icon19, R.drawable.winfxkliba_toast_icon20};
    public static final int[] Images2 = {
            R.drawable.winfxkliba_toast1_icon1, R.drawable.winfxkliba_toast1_icon2,
            R.drawable.winfxkliba_toast1_icon3, R.drawable.winfxkliba_toast1_icon4,
            R.drawable.winfxkliba_toast1_icon5, R.drawable.winfxkliba_toast1_icon6,
            R.drawable.winfxkliba_toast1_icon7, R.drawable.winfxkliba_toast1_icon8,
            R.drawable.winfxkliba_toast1_icon9, R.drawable.winfxkliba_toast1_icon10,
            R.drawable.winfxkliba_toast1_icon11, R.drawable.winfxkliba_toast1_icon12,
            R.drawable.winfxkliba_toast1_icon13, R.drawable.winfxkliba_toast1_icon14,
            R.drawable.winfxkliba_toast1_icon15, R.drawable.winfxkliba_toast1_icon16,
            R.drawable.winfxkliba_toast1_icon17, R.drawable.winfxkliba_toast1_icon18,
            R.drawable.winfxkliba_toast1_icon19, R.drawable.winfxkliba_toast1_icon20};
    public static final int LENGTH_SHORT = 0;
    public static final int LENGTH_LONG = 1;
    private ImageView imageView, imageView1;
    private final Context activity;
    private TextView textView;
    private int showTime;
    private View view;

    public Toast(Context context) {
        this.activity = context;
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflate.inflate(R.layout.winfxkliba_toast, null);
        textView = view.findViewById(R.id.textView1);
        imageView = view.findViewById(R.id.imageView1);
        imageView1 = view.findViewById(R.id.imageView2);
    }

    /**
     * 显示文本
     */
    public void show() {
        android.widget.Toast toast = new android.widget.Toast(activity);
        toast.setView(view);
        toast.setDuration(showTime);
        toast.show();
    }

    /**
     * 获取显示的图片视图1的对象
     */
    public ImageView getImageView() {
        return imageView;
    }

    /**
     * 获取显示的图片视图2的对象
     *
     * @return
     */
    public ImageView getImageView1() {
        return imageView1;
    }

    /**
     * 获取显示的文本视图对象
     *
     * @return
     */
    public TextView getTextView() {
        return textView;
    }

    /**
     * 获取显示的时长
     *
     * @return
     */
    public int getTime() {
        return showTime;
    }

    /**
     * 获取显示的视图
     *
     * @return
     */
    public View getView() {
        return view;
    }

    /**
     * 设置显示的文本视图
     *
     * @param textView
     */
    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    /**
     * 设置显示的图片1
     *
     * @param imageView
     */
    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    /**
     * 设置显示的图片2
     *
     * @param imageView1
     */
    public void setImageView1(ImageView imageView1) {
        this.imageView1 = imageView1;
    }

    /**
     * 设置显示的视图
     *
     * @param view
     */
    public void setView(View view) {
        this.view = view;
    }

    /**
     * 设置显示的时长
     *
     * @param i
     */
    public void setTime(int i) {
        this.showTime = i;
    }

    /**
     * 设置显示的文本
     *
     * @param string
     */
    public void setText(String string) {
        textView.setText(string);
    }

    /**
     * 设置显示的图片
     *
     * @param drawable
     */
    public void setImage(Drawable drawable) {
        imageView.setImageDrawable(drawable);
        imageView1.setImageDrawable(drawable);
    }

    /**
     * 设置显示的图片
     *
     * @param bm
     */
    public void setImage(Bitmap bm) {
        imageView.setImageBitmap(bm);
        imageView1.setImageBitmap(bm);
    }

    /**
     * 设置显示的图片
     *
     * @param resId
     */
    public void setImage(int resId) {
        imageView.setImageResource(resId);
        imageView1.setImageResource(resId);
    }

    /**
     * 设置显示的图片
     *
     * @param uri
     */
    public void setImage(Uri uri) {
        imageView.setImageURI(uri);
        imageView1.setImageURI(uri);
    }

    /**
     * 快捷提示一个窗口
     *
     * @param context Activity对象
     * @param string  要提示的内容
     * @param i       <b>提示时间</b>
     * @return
     * @see #LENGTH_SHORT
     * @see #LENGTH_LONG
     */
    public static Toast makeText(Context context, Object string, int i) {
        Toast tsate = new Toast(context);
        tsate.setText(String.valueOf(string));
        tsate.setTime(i);
        tsate.setRandImage();
        return tsate;
    }

    /**
     * 快捷显示一个提示
     *
     * @param context Activity对象
     * @param string  要显示的文本
     * @return
     */
    public static Toast makeText(Context context, Object string) {
        return makeText(context, String.valueOf(string), LENGTH_SHORT);
    }

    /**
     * 设置随机字体颜色
     *
     * @param r
     * @param g
     * @param b
     */
    public void setTextColor(int r, int g, int b) {
        GradientDrawable drawable = (GradientDrawable) textView.getBackground();
        drawable.setColor(Color.rgb(255 - r, 255 - g, 255 - b));
        textView.setBackground(drawable);
        textView.setTextColor(Color.rgb(r, g, b));
    }

    /**
     * 设置随机字体颜色
     */
    public void setRandColor() {
        setTextColor(Tool.getRand(0, 255), Tool.getRand(0, 255), Tool.getRand(0, 255));
    }

    /**
     * 设置随机字体背景颜色
     *
     * @param r
     * @param g
     * @param b
     */
    public void setTextBackgColor(int r, int g, int b) {
        GradientDrawable drawable = (GradientDrawable) textView.getBackground();
        drawable.setColor(Color.rgb(r, g, b));
        textView.setBackground(drawable);
        textView.setTextColor(Color.rgb(255 - r, 255 - g, 255 - b));
    }

    /**
     * 设置随机字体背景颜色
     */
    public void setRandBackgColor() {
        setTextBackgColor(Tool.getRand(0, 255), Tool.getRand(0, 255), Tool.getRand(0, 255));
    }

    /**
     * 设置随机图片跟随
     */
    public void setRandImage() {
        int[] Images1 = Tool.getRand(1, 2) == 1 ? Toast.Images1 : Images2;
        int r = Images1[Tool.getRand(0, Images1.length - 1)];
        int r2 = Images1[Tool.getRand(0, Images1.length - 1)];
        while (r == r2) r2 = Images1[Tool.getRand(0, Images1.length - 1)];
        getImageView().setImageResource(r);
        getImageView1().setImageResource(r2);
    }
}
