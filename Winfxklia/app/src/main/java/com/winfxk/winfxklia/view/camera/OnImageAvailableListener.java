/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/6/28  下午4:34*/
package com.winfxk.winfxklia.view.camera;

import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class OnImageAvailableListener implements ImageReader.OnImageAvailableListener {
    private final Camera2Helper main;

    public OnImageAvailableListener(Camera2Helper main) {
        this.main = main;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireNextImage();
        if (image == null) {
            Log.d(main.getTAG(), "获取的预览图像为空！");
            return;
        }
        if (main.mImageAvaiableListener != null) main.mImageAvaiableListener.onImageAvailable(image);
        image.close();
    }
}
