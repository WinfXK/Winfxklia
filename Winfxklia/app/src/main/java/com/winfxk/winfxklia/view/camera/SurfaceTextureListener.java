/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/6/28  下午3:46*/
package com.winfxk.winfxklia.view.camera;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;

public class SurfaceTextureListener implements TextureView.SurfaceTextureListener {
    private final Camera2Helper main;

    public SurfaceTextureListener(Camera2Helper main) {
        this.main = main;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
        Log.i(main.getTAG(), "启动摄像头捕获");
        main.openCamera(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        main.configureTransform(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
    }
}
