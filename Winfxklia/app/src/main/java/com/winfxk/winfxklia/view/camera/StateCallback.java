/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/6/28  下午3:51*/
package com.winfxk.winfxklia.view.camera;

import android.hardware.camera2.CameraDevice;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.winfxk.winfxklia.dialog.MyBuilder;
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class StateCallback extends CameraDevice.StateCallback {
    private final Camera2Helper main;

    protected StateCallback(Camera2Helper main) {
        this.main = main;
    }

    @Override
    public void onOpened(@NonNull CameraDevice cameraDevice) {
        main.mCameraOpenCloseLock.release();
        main.mCameraDevice = cameraDevice;
        main.createCameraPreviewSession();
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
        main.mCameraOpenCloseLock.release();
        cameraDevice.close();
        main.mCameraDevice = null;
    }

    @Override
    public void onError(@NonNull CameraDevice cameraDevice, int error) {
        main.mCameraOpenCloseLock.release();
        cameraDevice.close();
        main.mCameraDevice = null;
        if (main.mOpenErrorListener != null) main.mOpenErrorListener.onOpenError();
        else {
            MyBuilder builder = new MyBuilder(main.main);
            builder.setTitle("授权失败");
            builder.setMessage("请检查您的相机授权并重试。");
            builder.addButton("关闭", main::finishActivity);
            builder.show();
        }
    }
}
