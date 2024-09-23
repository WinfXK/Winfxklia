/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/17  下午4:43*/
package com.winfxk.winfxklia.view.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.winfxk.winfxklia.R;
import com.winfxk.winfxklia.tool.Tool;
import com.winfxk.winfxklia.view.ImageView;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class MenuButton extends LinearLayout {
    private MenuButton.OnClickListener listener = (v, i) -> onClick();
    private final List<ItemView> itemViews = new ArrayList<>();
    private Animation mb_in, mb_out, click_show, click_hide;
    private OnChangeListener show, hide;
    private boolean isClickClose = true;
    private LinearLayout line1, line2;
    private boolean isShow = false;
    private ImageView imageView;
    private int backgroundColor;
    private TextView title;
    private int width;

    public MenuButton(Context context) {
        this(context, null);
    }

    public MenuButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSize(context, attrs);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MenuButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        initSize(context, attrs);
        init();
    }

    private void initSize(Context context, AttributeSet attrs) {
        boolean isInitial = false;
        TypedArray a = null;
        try {
            a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Winfxklia_MenuButton, 0, 0);
            width = a.getDimensionPixelSize(R.styleable.Winfxklia_MenuButton_menuSize, 0);
            isInitial = true;
        } catch (Exception e) {
            Log.e(getLogTag(), "无法获取自定义宽度", e);
        } finally {
            try {
                if (a != null) a.recycle();
            } catch (Exception e) {
                Log.e(getLogTag(), "", e);
            }
        }
        if (!isInitial) width = 140;
        Log.d(getLogTag(), "init width" + width);
    }

    private void init() {
        mb_in = AnimationUtils.loadAnimation(getContext(), R.anim.winfxkliba_mb_in);
        mb_in.setFillAfter(true);
        mb_out = AnimationUtils.loadAnimation(getContext(), R.anim.winfxkliba_mb_out);
        mb_out.setFillAfter(true);
        click_hide = new RotateAnimation(225, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        click_hide.setDuration(500);
        click_hide.setFillAfter(true);
        click_show = new RotateAnimation(0, 225, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        click_show.setDuration(500);
        click_show.setFillAfter(true);
        LinearLayout view = (LinearLayout) View.inflate(getContext(), R.layout.winfxkliba_menu_button, null);
        line1 = view.findViewById(R.id.line1);
        line2 = view.findViewById(R.id.line2);
        imageView = view.findViewById(R.id.imageView1);
        title = view.findViewById(R.id.textView1);
        imageView.setBackgroundColor(backgroundColor = 0xff777777);
        imageView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(this, null);
        });
        title.setOnClickListener(v -> {
            if (listener != null) listener.onClick(this, null);
        });
        title.setText("");
        title.setTextColor(0xff000000);
        view.setOrientation(VERTICAL);
        line1.setOrientation(VERTICAL);
        Log.d(getLogTag(), getHeight() + "");
        line1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(view);
        if (Build.VERSION.SDK_INT >= 21) {
            imageView.setElevation(10);
            imageView.setTranslationZ(10);
        }
    }

    public static MenuButton make(Context context, int size) {
        MenuButton menuButton = new MenuButton(context);
        menuButton.width = size;
        return menuButton;
    }

    public List<ItemView> getItemViews() {
        return itemViews;
    }

    public void hide() {
        Animation animation;
        isShow = false;
        imageView.startAnimation(click_hide);
        for (int i = 0; i < itemViews.size(); i++) {
            ItemView itemView = itemViews.get(i);
            animation = AnimationUtils.loadAnimation(getContext(), R.anim.winfxkliba_mb_item_out);
            animation.setStartOffset((itemViews.size() - 1 - i) * 30L);
            animation.setFillAfter(true);
            itemView.startAnimation(mb_out, animation);
        }
        animation = AnimationUtils.loadAnimation(getContext(), R.anim.winfxkliba_mb_item_out);
        animation.setFillAfter(true);
        line1.startAnimation(mb_out);
        title.startAnimation(animation);
        new Thread(() -> {
            Tool.sleep(mb_out.getDuration());
            post(() -> {
                line1.getLayoutParams().height = 0;
                line1.requestLayout();
                getLayoutParams().height = imageView.getHeight();
                requestLayout();
            });
        }).start();
        if (hide != null) hide.OnChange(this, false);
    }

    public void setNotClose() {
        isClickClose = false;
    }

    public void show() {
        line1.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        line1.requestLayout();
        getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        requestLayout();
        Animation animation;
        isShow = true;
        imageView.startAnimation(click_show);
        animation = AnimationUtils.loadAnimation(getContext(), R.anim.winfxkliba_mb_item_in);
        animation.setFillAfter(true);
        line1.startAnimation(mb_in);
        title.startAnimation(animation);
        for (int i = 0; i < itemViews.size(); i++) {
            ItemView itemView = itemViews.get(i);
            animation = AnimationUtils.loadAnimation(getContext(), R.anim.winfxkliba_mb_item_in);
            animation.setStartOffset(i * 30L);
            animation.setFillAfter(true);
            itemView.startAnimation(mb_in, animation);
        }
        if (show != null) show.OnChange(this, true);
    }

    public ItemView getItemView(int position) {
        if (itemViews.isEmpty() || itemViews.size() <= position) return null;
        return itemViews.get(position);
    }

    public ItemView getItemView(String text) {
        if (itemViews.isEmpty()) return null;
        for (ItemView itemView : itemViews)
            if (itemView.getTitle().equals(text)) return itemView;
        return null;
    }

    public void build() {
        build(width);
    }

    public void build(int size) {
        Animation build = AnimationUtils.loadAnimation(getContext(), R.anim.winfxkliba_mb_out);
        build.setFillAfter(true);
        build.setDuration(0);
        line1.startAnimation(build);
        build = AnimationUtils.loadAnimation(getContext(), R.anim.winfxkliba_mb_item_out);
        build.setFillAfter(true);
        build.setDuration(0);
        title.startAnimation(build);
        width = size;
        line1.removeAllViews();
        line1.getLayoutParams().height = 0;
        requestLayout();
        imageView.setLayoutParams(new LayoutParams(width, getMyWidth()));
        line2.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, getMyWidth()));
        for (ItemView itemView : itemViews) {
            line1.addView(itemView.getView());
            itemView.setSize();
        }
        if (Build.VERSION.SDK_INT >= 21) setZ(999);
    }

    public OnChangeListener getHideListener() {
        return hide;
    }

    public void setHideListener(OnChangeListener hide) {
        this.hide = hide;
    }

    public void setShowListener(OnChangeListener show) {
        this.show = show;
    }

    public OnChangeListener getShowListener() {
        return show;
    }

    private void onClick() {
        if (isShow) hide();
        else show();
    }

    public ItemView addMenu(String title, String imageUrl, @ColorInt  int background, OnClickListener listener) {
        ItemView itemView = new ItemView(this);
        itemView.setTitle(title);
        itemView.setImageUrl(imageUrl);
        itemView.setBackground(background);
        itemView.setOnClickListener(listener);
        addMenu(itemView);
        return itemView;
    }

    public ItemView addMenu(String title, File image, @ColorInt  int background, OnClickListener listener) throws IOException {
        ItemView itemView = new ItemView(this);
        itemView.setTitle(title);
        itemView.setImageFile(image);
        itemView.setBackground(background);
        itemView.setOnClickListener(listener);
        addMenu(itemView);
        return itemView;
    }

    public ItemView addMenu(String title, @DrawableRes int image, @ColorInt  int background, OnClickListener listener) {
        ItemView itemView = new ItemView(this);
        itemView.setTitle(title);
        itemView.setImageResource(image);
        itemView.setBackground(background);
        itemView.setOnClickListener(listener);
        addMenu(itemView);
        return itemView;
    }

    public ItemView addMenu(String title, Bitmap image, @ColorInt int background, OnClickListener listener) {
        ItemView itemView = new ItemView(this);
        itemView.setTitle(title);
        itemView.setImageBitmap(image);
        itemView.setBackground(background);
        itemView.setOnClickListener(listener);
        addMenu(itemView);
        return itemView;
    }

    public int getMyWidth() {
        return width;
    }

    public MenuButton addMenu(ItemView itemView) {
        itemViews.add(itemView);
        return this;
    }

    public OnClickListener getClickListener() {
        return listener;
    }

    public void setClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public MenuButton setBackground(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        imageView.setBackgroundColor(backgroundColor);
        return this;
    }

    public @ColorInt int getImageBackground() {
        return this.backgroundColor;
    }

    public MenuButton setTitle(@NotNull String title) {
        this.title.setText(title);
        return this;
    }

    public String getTitle() {
        return title.getText().toString();
    }

    public @ColorInt int getBackgroundColor() {
        return backgroundColor;
    }

    public static String getLogTag() {
        return MenuButton.class.getSimpleName();
    }

    @SuppressLint("ClickableViewAccessibility")
    public static class ItemView implements View.OnClickListener {
        private OnClickListener listener = null;
        private final ImageView imageView;
        private final LinearLayout line;
        private final MenuButton menu;
        private final Context context;
        private final TextView title;

        private ItemView(MenuButton menu) {
            this.menu = menu;
            this.context = menu.getContext();
            line = (LinearLayout) ViewGroup.inflate(getContext(), R.layout.winfxkliba_menu_b_i, null);
            line.setOnClickListener(this);
            title = line.findViewById(R.id.textView1);
            imageView = line.findViewById(R.id.imageView1);
            title.setText("");
        }

        public void setPadding(int left, int top, int right, int bottom) {
            imageView.setPadding(left, top, right, bottom);
        }

        public LinearLayout getView() {
            return line;
        }

        private ItemView startAnimation(Animation titleAnimation, Animation viewAnimation) {
            title.startAnimation(titleAnimation);
            line.startAnimation(viewAnimation);
            return this;
        }


        @Override
        public void onClick(View v) {
            menu.isClickClose = true;
            if (listener != null) listener.onClick(menu, this);
            if (menu.isClickClose) menu.hide();
        }

        public ItemView setBackground(@ColorInt int color) {
            imageView.setBackgroundColor(Tool.expandColor(color));
            return this;
        }

        public ItemView setImageFile(File file) throws IOException {
            imageView.setImageFile(file);
            return this;
        }

        public ItemView setImageUrl(String Url) {
            imageView.setImageURL(Url);
            return this;
        }

        public ItemView setImageBitmap(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
            return this;
        }

        public ItemView setImageResource(@DrawableRes int ID) {
            imageView.setImageResource(ID);
            return this;
        }

        public Bitmap getImageBitmap() {
            return imageView.getImageBitmap();
        }

        public ItemView setTitleColor(@ColorInt int color) {
            title.setTextColor(color);
            return this;
        }

        public @ColorInt int getTitleColor() {
            return title.getCurrentTextColor();
        }

        public float getTitleSize() {
            return title.getTextSize();
        }

        public ItemView setTitleSize(float fontSize) {
            title.setTextSize(fontSize);
            return this;
        }

        public ItemView setTitle(String title) {
            this.title.setText(title);
            return this;
        }

        public String getTitle() {
            return title.getText().toString();
        }

        public Context getContext() {
            return context;
        }

        public OnClickListener getOnClickListener() {
            return listener;
        }

        public void setOnClickListener(OnClickListener listener) {
            this.listener = listener;
        }

        public void setSize() {
            imageView.setLayoutParams(new LayoutParams(menu.getMyWidth(), menu.getMyWidth()));
            line.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, menu.getMyWidth()));
        }
    }

    public interface OnClickListener {
        void onClick(MenuButton button, ItemView itemView);
    }

    public interface OnChangeListener {
        void OnChange(MenuButton button, boolean isShow);
    }
}
