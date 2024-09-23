/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/6/28  上午11:14*/
package com.winfxk.winfxklia.view.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.*;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import com.winfxk.winfxklia.BaseActivity;
import com.winfxk.winfxklia.dialog.MyBuilder;
import com.winfxk.winfxklia.dialog.Toast;
import com.winfxk.winfxklia.tool.able.Tabable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Helper implements Tabable {
    protected final BaseActivity main;
    protected final AutoFitTextureView preview;
    protected HandlerThread thread;
    protected Handler handler;
    protected CaptureRequest.Builder builder;
    protected CaptureRequest request;
    protected static final int REQUEST_CAMERA_PERMISSION = 1;
    protected ImageReader imageReader;
    protected static final int STATE_PREVIEW = 0;
    protected static final int STATE_WAITING_LOCK = 1;
    protected static final int STATE_WAITING_PRECAPTURE = 2;
    protected static final int STATE_WAITING_NON_PRECAPTURE = 3;
    protected static final int STATE_PICTURE_TAKEN = 4;
    protected static final int MAX_PREVIEW_WIDTH = 1920;
    protected static final int MAX_PREVIEW_HEIGHT = 1080;
    protected int mImageFormat = ImageFormat.JPEG;
    protected int mState = STATE_PREVIEW;
    protected int mSensorOrientation;
    protected String mCameraId;
    protected final Semaphore mCameraOpenCloseLock = new Semaphore(1);
    protected CameraCaptureSession mCaptureSession;
    protected CameraDevice mCameraDevice;
    protected Size mPreviewSize;
    private final OnImageAvailableListener mOnImageAvailableListener = new OnImageAvailableListener(this);
    protected OnOpenErrorListener mOpenErrorListener; //打开错误，不设置才用默认策略
    protected OnPreviewCallbackListener mImageAvaiableListener; //摄像头画面可达的时候

    public Camera2Helper(@NonNull BaseActivity activity, @NonNull AutoFitTextureView textureView) {
        this.main = activity;
        this.preview = textureView;
    }

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener(this);

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new StateCallback(this);
    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    protected final CameraCaptureSession.CaptureCallback mCaptureCallback = new SessionCaptureCallback(this);

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {}.
     */
    protected void runPrecaptureSequence() {
        try {
            builder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(builder.build(), mCaptureCallback, handler);
        } catch (CameraAccessException e) {
            Log.e(getTAG(), "捕获数据时出现异常！", e);
            Toast.makeText(main, "程序出现异常！\n" + e.getLocalizedMessage());
        }
    }

    protected void finishActivity(MyBuilder builder) {
        main.finish();
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    protected void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = preview.getSurfaceTexture();
            if (texture == null) {
                MyBuilder builder = new MyBuilder(main);
                builder.setTitle("错误");
                builder.setMessage("摄像头初始化失败！请稍后重试！");
                builder.addButton("确定", this::finishActivity);
                builder.show();
                Log.e(getTAG(), "无法获取SurfaceTexture!获取到的SurfaceTexture为空！");
                return;
            }
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(texture);
            Log.i(getTAG(), "开始输出画面到TextureView");
            builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            builder.addTarget(imageReader.getSurface());
            mCameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CaptureSessionStateCallback(this), null);
        } catch (CameraAccessException e) {
            Log.e(getTAG(), "创建CameraCaptureSession时出现异常！", e);
            MyBuilder builder = new MyBuilder(main);
            builder.setTitle("错误");
            builder.setMessage("程序出现异常！\n" + e.getLocalizedMessage());
            builder.addButton("确定", this::finishActivity);
            builder.show();
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(main, CameraActivity.CameraPermissions, REQUEST_CAMERA_PERMISSION);
    }

    public void open() {
        startBackgroundThread();
        if (preview.isAvailable()) openCamera(preview.getWidth(), preview.getHeight());
        else preview.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    /**
     * Opens the camera specified
     */
    @SuppressLint("MissingPermission")
    protected void openCamera(int width, int height) {
        
        if (ActivityCompat.checkSelfPermission(main, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) main.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) return;
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                MyBuilder builder = new MyBuilder(main);
                builder.setTitle("错误");
                builder.setMessage("摄像头初始化失败！请检查您的应用是否有相机权限或摄像头已被占用！");
                builder.addButton("确定", this::finishActivity);
                builder.show();
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, handler);
        } catch (CameraAccessException e) {
            Log.e(getTAG(), "相机打开失败！", e);
            MyBuilder builder = new MyBuilder(main);
            builder.setTitle("错误");
            builder.setMessage("程序出现异常！\n" + e.getLocalizedMessage());
            builder.addButton("确定", this::finishActivity);
            builder.show();
        } catch (InterruptedException e) {
            Log.e(getTAG(), "相机打开线程被中断！", e);
            MyBuilder builder = new MyBuilder(main);
            builder.setTitle("错误");
            builder.setMessage("摄像头被意外占用！\n" + e.getLocalizedMessage());
            builder.addButton("确定", this::finishActivity);
            builder.show();
        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {
        
        CameraManager manager = (CameraManager) main.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) return;
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) continue;
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) continue;
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(mImageFormat)), new CompareSizesByArea());
                imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), mImageFormat, /*maxImages*/2);
                imageReader.setOnImageAvailableListener(mOnImageAvailableListener, handler);
                int displayRotation = main.getWindowManager().getDefaultDisplay().getRotation();
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) swappedDimensions = true;
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) swappedDimensions = true;
                        break;
                    default:
                        Log.e(getTAG(), "无法获取正确的旋转角度：" + displayRotation);
                }
                Point displaySize = new Point();
                main.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;
                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }
                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) maxPreviewWidth = MAX_PREVIEW_WIDTH;
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight,
                        maxPreviewWidth, maxPreviewHeight, largest);
                int orientation = main.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    preview.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else preview.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            Log.e(getTAG(), "相机参数获取失败！", e);
            MyBuilder builder = new MyBuilder(main);
            builder.setTitle("错误");
            builder.setMessage("程序出现异常！无法获取相机参数\n" + e.getLocalizedMessage());
            builder.addButton("确定", this::finishActivity);
            builder.show();
        } catch (NullPointerException e) {
            Log.e(getTAG(), "相机参数获取失败！", e);
            MyBuilder builder = new MyBuilder(main);
            builder.setTitle("错误");
            builder.setMessage("程序出现异常！无法解析相机参数\n" + e.getLocalizedMessage());
            builder.addButton("确定", this::finishActivity);
            builder.show();
        }
    }

    private void startBackgroundThread() {
        thread = new HandlerThread(main.getTAG() + " CameraActivity");
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    protected void configureTransform(int viewWidth, int viewHeight) {
        
        if (null == mPreviewSize) return;
        int rotation = main.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(), (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) matrix.postRotate(180, centerX, centerY);
        preview.setTransform(matrix);
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    public void closeCamera() {
        
        stopBackgroundThread();
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) try {
                mCaptureSession.close();
                mCaptureSession = null;
            } catch (Exception e) {
                Log.e(getTAG(), "关闭摄像头资源时出现异常！", e);
            }
            if (null != mCameraDevice) try {
                mCameraDevice.close();
                mCameraDevice = null;
            } catch (Exception e) {
                Log.e(getTAG(), "关闭摄像头资源时出现异常！", e);
            }
            if (null != imageReader) try {
                imageReader.close();
                imageReader = null;
            } catch (Exception e) {
                Log.e(getTAG(), "关闭摄像头资源时出现异常！", e);
            }
        } catch (InterruptedException e) {
            Log.e(getTAG(), "尝试关闭摄像头时被异常终止！", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        thread.quitSafely();
        try {
            thread.join();
            thread = null;
            handler = null;
        } catch (InterruptedException e) {
            Log.e(getTAG(), "关闭后台服务和管理器时被异常终止！", e);
        }
    }

    /**
     * 返回摄像头权限的请求码
     *
     * @return 返回请求码
     */
    public int getCameraRequestCode() {
        return REQUEST_CAMERA_PERMISSION;
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<>();
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight && option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight)
                    bigEnough.add(option);
                else notBigEnough.add(option);
            }
        }
        if (!bigEnough.isEmpty()) return Collections.min(bigEnough, new CompareSizesByArea());
        else if (!notBigEnough.isEmpty()) return Collections.max(notBigEnough, new CompareSizesByArea());
        else {
            Log.e(getTAG(), "找不到适合的预览尺寸");
            return choices[0];
        }
    }

    /**
     * 设置视频预览后的回调格式
     *
     * @param format 遵循ImageFormat格式
     */
    public Camera2Helper setImageFormat(int format) {
        mImageFormat = format;
        return this;
    }

    /**
     * 获取摄像头方向
     *
     * @return 方向，详见mSensorOrientation赋值 {@link Camera2Helper#setUpCameraOutputs}
     */
    public int getSensorOrientation() {
        return mSensorOrientation;
    }

    /**
     * 打开错误的回调，可以不设置，不设置采用默认策略
     *
     * @param listener 回调listener
     */
    public Camera2Helper setOnOpenErrorListener(OnOpenErrorListener listener) {
        mOpenErrorListener = listener;
        return this;
    }

    /**
     * 摄像头图像回调，类似于Camera1的PreviewCallback
     *
     * @param listener 回调listener
     */
    public Camera2Helper setOnImageAvailableListener(OnPreviewCallbackListener listener) {
        mImageAvaiableListener = listener;
        return this;
    }


    @Override
    public String getTab() {
        return getTAG();
    }

    @Override
    public String getTAG() {
        return main.getTAG() + " Camera";
    }
}
