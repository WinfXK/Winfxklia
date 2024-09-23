/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/6/28  下午4:14*/
package com.winfxk.winfxklia.view.camera;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.winfxk.winfxklia.dialog.MyBuilder;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CaptureSessionStateCallback extends CameraCaptureSession.StateCallback {
    private final Camera2Helper main;

    protected CaptureSessionStateCallback(Camera2Helper main) {
        this.main = main;
    }

    @Override
    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
        if (null == main.mCameraDevice) {
            Log.w(main.getTAG(), "摄像头已被关闭！");
            return;
        }
        Log.i(main.getTAG(), "摄像头已准备好！");
        main.mCaptureSession = cameraCaptureSession;
        try {
            Log.i(main.getTAG(), "已启用自动对焦");
            main.builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            main.request = main.builder.build();
            main.mCaptureSession.setRepeatingRequest(main.request, main.mCaptureCallback, main.handler);
        } catch (CameraAccessException e) {
            Log.e(main.getTAG(), "创建摄像头会话失败！");
            MyBuilder builder = new MyBuilder(main.main);
            builder.setTitle("提示");
            builder.setMessage("配置会话失败");
            builder.addButton("确定", main::finishActivity);
            builder.show();
        }
    }

    @Override
    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
        Log.e(main.getTAG(), "配置会话失败！");
        MyBuilder builder = new MyBuilder(main.main);
        builder.setTitle("提示");
        builder.setMessage("配置会话失败");
        builder.addButton("确定", main::finishActivity);
        builder.show();
    }
}
