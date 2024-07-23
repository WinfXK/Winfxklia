/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/6/28  下午3:56*/
package com.winfxk.winfxklia.view.camera;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import androidx.annotation.NonNull;

public class SessionCaptureCallback extends CameraCaptureSession.CaptureCallback {
    private final Camera2Helper main;

    protected SessionCaptureCallback(Camera2Helper main) {
        this.main = main;
    }

    private void process(CaptureResult result) {
        switch (main.mState) {
            case Camera2Helper.STATE_PREVIEW: {
                //当摄像头正常工作时
                break;
            }
            case Camera2Helper.STATE_WAITING_LOCK: {
                Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                if (afState != null && (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                        CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState)) {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        main.mState = Camera2Helper.STATE_PICTURE_TAKEN;
                    } else main.runPrecaptureSequence();
                }
                break;
            }
            case Camera2Helper.STATE_WAITING_PRECAPTURE: {
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED)
                    main.mState = Camera2Helper.STATE_WAITING_NON_PRECAPTURE;
                break;
            }
            case Camera2Helper.STATE_WAITING_NON_PRECAPTURE: {
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE)
                    main.mState = Camera2Helper.STATE_PICTURE_TAKEN;
                break;
            }
        }
    }

    @Override
    public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
        process(partialResult);
    }

    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
        process(result);
    }

}
