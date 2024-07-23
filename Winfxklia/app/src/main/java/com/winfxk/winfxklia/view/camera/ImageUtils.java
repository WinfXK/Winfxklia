/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/6/28  下午1:33*/
package com.winfxk.winfxklia.view.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Log;
import com.winfxk.winfxklia.BaseActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageUtils {
    /**
     * 将Bitmap保存到指定路径的文件中。
     *
     * @param image 要保存的Image对象。
     * @param file  保存文件的完整路径。
     */
    public static void saveBitmapToFile(BaseActivity activity, Image image, File file) throws IOException {
        saveBitmapToFile(activity, image, file, Bitmap.CompressFormat.JPEG, 100);
    }

    /**
     * 将Bitmap保存到指定路径的文件中。
     *
     * @param bitmap 要保存的Bitmap对象。
     * @param file   保存文件的完整路径。
     */
    public static void saveBitmapToFile(BaseActivity activity, Bitmap bitmap, File file) throws IOException {
        saveBitmapToFile(activity, bitmap, file, Bitmap.CompressFormat.JPEG, 100);
    }

    /**
     * 将Bitmap保存到指定路径的文件中。
     *
     * @param image   要保存的Image对象。
     * @param file    保存文件的完整路径。
     * @param format  保存的格式，例如Bitmap.CompressFormat.PNG。
     * @param quality 压缩质量，范围是0（最差）到100（最好）。
     */
    public static void saveBitmapToFile(BaseActivity activity, Image image, File file, Bitmap.CompressFormat format, int quality) throws IOException {
        saveBitmapToFile(activity, convertImageToBitmap(image), file, format, quality);
    }

    /**
     * 将Bitmap保存到指定路径的文件中。
     *
     * @param bitmap  要保存的Bitmap对象。
     * @param file    保存文件的完整路径。
     * @param format  保存的格式，例如Bitmap.CompressFormat.PNG。
     * @param quality 压缩质量，范围是0（最差）到100（最好）。
     */
    public static void saveBitmapToFile(BaseActivity activity, Bitmap bitmap, File file, Bitmap.CompressFormat format, int quality) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            boolean isSaved = bitmap.compress(format, quality, fos);
            if (isSaved) Log.i(activity.getTab(), "图像保存成功！");
            else throw new IllegalStateException("image is already saved");
        }
    }

    public static Bitmap convertImageToBitmap(Image image) {
        Bitmap bitmap;
        ByteBuffer buffer;
        byte[] bytes;
        if (image.getFormat() == ImageFormat.JPEG) {
            // 对于JPEG格式，可以直接从Image的ByteBuffer转换
            buffer = image.getPlanes()[0].getBuffer();
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            // 对于YUV格式，需要进行颜色空间转换
            int width = image.getWidth();
            int height = image.getHeight();
            buffer = image.getPlanes()[0].getBuffer();
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            // 以下仅为YUV420的转换，其他格式需要不同的处理
            int[] yuvBytes = new int[width * height];
            int i = 0;
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++) yuvBytes[i++] = bytes[y * width + x] & 0xFF;
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            int[] argbBytes = new int[width * height];
            convertYUV420ToARGB8888(yuvBytes, argbBytes, width, height);
            bitmap.setPixels(argbBytes, 0, width, 0, 0, width, height);
        }
        // 释放Image资源
        image.close();
        return bitmap;
    }

    private static void convertYUV420ToARGB8888(int[] yuvBytes, int[] argbBytes, int width, int height) {
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int y = (0xff & (yuvBytes[j * width + i]));
                int index = width * height + (j >> 1) * width + (i & ~1);
                int u = (0xff & (yuvBytes[index]));
                index = width * height + (j >> 1) * width + (i & ~1) + 1;
                int v = (0xff & (yuvBytes[index]));
                int r = y + ((v - 128) * 359) / 256;
                int g = y - ((u - 128) * 88) / 256 - ((v - 128) * 183) / 256;
                int b = y + ((u - 128) * 454) / 256;
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
                argbBytes[j * width + i] = (255 << 24) | (r << 16) | (g << 8) | b;
            }
        }
    }
}
