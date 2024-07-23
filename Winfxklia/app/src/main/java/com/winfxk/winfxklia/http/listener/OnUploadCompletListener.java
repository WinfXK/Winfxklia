/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/29  下午2:19*/
package com.winfxk.winfxklia.http.listener;

import com.winfxk.winfxklia.http.HttpUpload;

import java.io.File;
import java.util.List;

public interface OnUploadCompletListener {
    void onUploadComplet(HttpUpload upload, File file, long titk, boolean isComplete, Throwable throwable);
}
