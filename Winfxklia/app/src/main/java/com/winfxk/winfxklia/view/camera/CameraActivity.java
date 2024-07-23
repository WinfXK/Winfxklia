/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/6/28  下午1:03*/
package com.winfxk.winfxklia.view.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.winfxk.winfxklia.BaseActivity;
import com.winfxk.winfxklia.R;
import com.winfxk.winfxklia.dialog.MyBuilder;
import com.winfxk.winfxklia.dialog.ParBuilder;
import com.winfxk.winfxklia.dialog.Type;
import com.winfxk.winfxklia.tool.Tool;
import com.winfxk.winfxklia.view.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class CameraActivity extends BaseActivity implements OnPreviewCallbackListener, View.OnClickListener {
    protected static final String[] CameraPermissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static File ImageCacheDir = null;
    private AutoFitTextureView textureView;
    private ImageButton button, confirm;
    private boolean isCamera = false;
    private Camera2Helper helper;
    private ImageView imageView;
    private Bitmap image;
    private File path;
    private volatile boolean photograph = false;
    private RelativeLayout relativeLayout;
    private Animation hide, show;

    @Override
    protected void onInitialize() {
        setContentView(R.layout.winfxkliba_camera_preview);
        textureView = findViewById(R.id.textureView1);
        imageView = findViewById(R.id.imageView1);
        button = findViewById(R.id.imageButton1);
        confirm = findViewById(R.id.imageButton2);
        relativeLayout = findViewById(R.id.rinearLayout1);
        show = AnimationUtils.loadAnimation(this, R.anim.winfxkliba_alpha_show);
        show.setFillAfter(true);
        hide = AnimationUtils.loadAnimation(this, R.anim.winfxkliba_alpha_hide);
        hide.setFillAfter(true);
        button.setOnClickListener(this);
        initializePath();
        confirm.setOnClickListener(this::save);
        havePermission(CameraPermissions);
        confirm.startAnimation(hide);
        relativeLayout.startAnimation(hide);
    }

    @Override
    protected boolean UnauthorisedAccess() {
        return true;
    }

    @Override
    protected void init() {
        super.init();
        helper = new Camera2Helper(this, textureView);
        helper.setOnImageAvailableListener(this);
    }

    private void save(View view) {
        if (image == null) return;
        ParBuilder par = new ParBuilder(this);
        par.setTitle("提示");
        par.setMessage("保存中请稍后...");
        par.setType(Type.Progress);
        par.show();
        thread(() -> {
            try {
                ImageUtils.saveBitmapToFile(this, image, path);
                Intent intent = new Intent().putExtra("path", path.getAbsolutePath());
                Log.i(getTAG(), "图像保存成功！保存地址：" + path);
                post(() -> {
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                });
            } catch (IOException e) {
                Log.e(getTAG(), "保存图片时出现异常！", e);
                post(() -> {
                    MyBuilder builder = new MyBuilder(this);
                    builder.setTitle("提示");
                    builder.setMessage("无法获取拍摄的图像！\n" + e.getMessage());
                    builder.addButton("确定");
                    builder.show();
                    par.dismiss();
                });
            }
        });
    }

    private void initializePath() {
        if (ImageCacheDir == null) ImageCacheDir = new File(getCacheDir(), "Images/");
        if (!ImageCacheDir.exists() || !ImageCacheDir.isDirectory())
            if (!ImageCacheDir.mkdirs()) Log.e(getTAG(), "无法创建图片缓存路径！");
        String str = getIntent().getStringExtra("path");
        if (str == null || str.isEmpty()) str = getIntent().getStringExtra("Path");
        if (str == null || str.isEmpty()) str = getIntent().getStringExtra("file");
        if (str == null || str.isEmpty()) str = getIntent().getStringExtra("File");
        if (str == null || str.isEmpty())
            str = Tool.CompressNumber(System.currentTimeMillis()) + UUID.randomUUID().toString() + cacheFileEx;
        path = new File(ImageCacheDir, str);
    }

    @Override
    public void onClick(View v) {
        if (photograph) return;
        if (isCamera) {
            isCamera = false;
            photograph = false;
            confirm.setEnabled(false);
            relativeLayout.startAnimation(hide);
            confirm.startAnimation(hide);
            button.setImageResource(R.drawable.winfxkliba_camera);
            return;
        }
        isCamera = true;
        photograph = true;
        Log.i(getTAG(), "接收拍摄指令");
    }

    @Override
    public void onImageAvailable(Image image) {
        if (!photograph) return;
        Log.i(getTAG(), "已接收帧，正在转换....");
        this.image = ImageUtils.convertImageToBitmap(image);
        photograph = false;
        post(() -> {
            Log.i(getTAG(), "已捕获图像");
            confirm.startAnimation(show);
            relativeLayout.startAnimation(show);
            imageView.setImageBitmap(this.image);
            button.setImageResource(R.drawable.winfxkliba_close);
        });
    }

    public void onResume() {
        super.onResume();
        helper.open();
    }

    @Override
    public void onPause() {
        helper.closeCamera();
        super.onPause();
    }

    protected ImageButton getConfirm() {
        return confirm;
    }

    protected ImageButton getButton() {
        return button;
    }

    protected Bitmap getImage() {
        return image;
    }

    public static File getImageCacheDir() {
        return ImageCacheDir;
    }

    protected ImageView getImageView() {
        return imageView;
    }

    protected Camera2Helper getHelper() {
        return helper;
    }
}
