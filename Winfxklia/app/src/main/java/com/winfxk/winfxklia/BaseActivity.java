/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/17  下午3:39*/
package com.winfxk.winfxklia;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.winfxk.winfxklia.dialog.MyBuilder;
import com.winfxk.winfxklia.dialog.ParBuilder;
import com.winfxk.winfxklia.dialog.Toast;
import com.winfxk.winfxklia.tool.Tool;
import com.winfxk.winfxklia.tool.able.Closeable;
import com.winfxk.winfxklia.tool.able.Tabable;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class BaseActivity extends Activity implements Tabable, Closeable {
    protected static final int hasPermissionsCode = Tool.getRand(10000, Integer.MAX_VALUE);
    protected final String TAG = getClass().getSimpleName();
    private ParBuilder permissionsBuilder;
    protected Handler handler, thread;
    protected static final FileFilter cacheFilter = a -> a.isDirectory() || isCache(a);
    protected static final String cacheFileEx = ".cache";

    protected abstract void onInitialize();

    @Override
    public File getDataDir() {
        if (Build.VERSION.SDK_INT < 24) {
            File file = getFilesDir();
            File pe = file.getParentFile();
            if (pe != null && pe.getName().equals(getPackageName()))
                return pe;
            return file;
        }
        return super.getDataDir();
    }

    /**
     * 创建并返回一个缓存文件
     *
     * @return 创建并返回一个缓存文件
     * @throws IOException 可能的异常
     */
    protected File createCache() throws IOException {
        File cache = getCacheDir();
        StringBuilder filename = new StringBuilder(Tool.CompressNumber(System.currentTimeMillis()));
        File file = new File(cache, filename + cacheFileEx);
        int index = 0;
        do {
            while (file.exists()) {
                filename.append(Tool.getRandString());
                file = new File(cache, filename + cacheFileEx);
            }
            if (index++ > 100) throw new IOException("无法创建缓存文件！");
        } while (!file.createNewFile());
        return file;
    }

    /**
     * 删除文件/文件夹(缓存)
     *
     * @param files 文件/文件夹对象
     * @return 清除结果
     */
    protected boolean clearCache(File... files) {
        if (files == null) return false;
        boolean isSu = true;
        for (File file : files) {
            if (file == null || !file.exists()) continue;
            if (file.isFile()) if (isCache(file)) isSu = isSu & file.delete();
            else if (file.isDirectory()) {
                File[] list = file.listFiles(cacheFilter);
                if (list == null || list.length == 0)
                    isSu = isSu & file.delete();
                else isSu = isSu & clearCache(list);
            }
        }
        return isSu;
    }

    private static boolean isCache(File file) {
        if (file == null || !file.isFile()) return false;
        return file.getName().toLowerCase().endsWith(cacheFileEx);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HandlerThread handlerThread = new HandlerThread("packge: " + getPackageName() + " Class: " + getClass().getSimpleName() + " is Activity Thread");
        handlerThread.start();
        thread = new Handler(handlerThread.getLooper());
        handler = new Handler(Looper.getMainLooper());
        setFullScreen(this);
        onInitialize();
    }

    /**
     * 判断服务是否已经在运行
     */
    protected boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (serviceClass.getName().equals(service.service.getClassName()))
                return true;
        return false;
    }

    protected void Toast(Object message) {
        Toast.makeText(this, message).show();
    }

    /**
     * 弹出一个提示框
     */
    protected void showTip(String message) {
        MyBuilder.make(this).setTitle("提示").setMessage(message)
                .addButton("确定").show();
    }

    /**
     * 弹出一个提示框
     */
    protected void showTip(String message, MyBuilder.OnClickListener listener) {
        MyBuilder.make(this).setTitle("提示").setMessage(message)
                .addButton("确定", listener).show();
    }

    /**
     * 调用异步线程执行操作
     *
     * @param runnable 需要执行的操作
     */
    public void thread(Runnable runnable) {
        thread(runnable, false);
    }

    /**
     * 调用异步线程执行操作
     *
     * @param runnable 需要执行的操作
     * @param isQueue  是否使用队列执行
     */
    public void thread(Runnable runnable, boolean isQueue) {
        if (isQueue) thread.post(runnable);
        else new Thread(runnable).start();
    }

    /**
     * 调用主线程执行操作
     *
     * @param runnable 需要执行的操作
     */
    public void handler(Runnable runnable) {
        handler.post(runnable);
    }

    /**
     * 调用主线程执行操作
     *
     * @param runnable 需要执行的操作
     */
    public void post(Runnable runnable) {
        handler(runnable);
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    /**
     * 请求权限
     *
     * @param permissions 需要请求的权限
     */
    protected void havePermission(String[] permissions) {
        if (Build.VERSION.SDK_INT < 23) {
            init();
            return;
        }
        List<String> list = new ArrayList<>();
        for (String p : permissions)
            if (!hasPermission(p)) list.add(p);
        if (!list.isEmpty()) {
            if (permissionsBuilder == null)
                permissionsBuilder = new ParBuilder(this, com.winfxk.winfxklia.dialog.Type.Progress);
            else permissionsBuilder.setType(com.winfxk.winfxklia.dialog.Type.Progress).clearButtons();
            permissionsBuilder.setTitle("提示").setMessage("请求授权中...");
            if (!permissionsBuilder.isShowing()) permissionsBuilder.show();
            requestPermissions(list.toArray(new String[]{}), hasPermissionsCode);
        } else init();
    }

    /**
     * 初始化入口，如果使用了请求权限操作，请求结束后会调用此方法
     */
    protected void init() {
    }

    /**
     * @return 是否将未授权的权限内容打印到message
     */
    protected boolean isShowPermissionsResultError() {
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsBuilder.clearButtons();
        if (requestCode == hasPermissionsCode) {
            StringBuilder deniedPermissions = new StringBuilder();
            if (isShowPermissionsResultError())
                for (int i = 0; i < grantResults.length; i++)
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        deniedPermissions.append(permissions[i]).append("\n");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionsBuilder.setType(com.winfxk.winfxklia.dialog.Type.SUCCESS).setMessage("授权成功！").addButton("确定", (bu) -> init());
            } else {
                permissionsBuilder.setType(com.winfxk.winfxklia.dialog.Type.ERROR).
                        setMessage("授权失败！本程序需授权后才能使用！\n" + deniedPermissions)
                        .addButton("退出", (bu) -> finish())
                        .addButton("重试", (bu) -> {
                            bu.setNotClose();
                            havePermission(permissions);
                        });
                if (UnauthorisedAccess()) permissionsBuilder.addButton("强行进入", bu -> init());
            }
        }
    }

    /**
     * @return 当未获取到全部权限时，是否允许用户进入
     */
    protected boolean UnauthorisedAccess() {
        return false;
    }

    /**
     * @param permissions 需要判断的权限
     * @return 判断是否拥有权限
     */
    private boolean hasPermission(String permissions) {
        return Build.VERSION.SDK_INT < 23 || (this.checkSelfPermission(permissions) == PackageManager.PERMISSION_GRANTED);
    }

    public String getTestTag() {
        return "Winfxk test by " + getTAG();
    }

    /**
     * 设置程序全屏
     *
     * @param activity activity
     */
    public static void setFullScreen(BaseActivity activity) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= 21) window.setStatusBarColor(Color.TRANSPARENT);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = window.getInsetsController();
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            window.setAttributes(params);
        }
    }
}
