/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/6/28  下午4:33*/
package com.winfxk.winfxklia.view.camera;

import android.os.Build;
import android.util.Size;
import androidx.annotation.RequiresApi;

import java.util.Comparator;
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CompareSizesByArea implements Comparator<Size> {

    @Override
    public int compare(Size lhs, Size rhs) {
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
    }
}
