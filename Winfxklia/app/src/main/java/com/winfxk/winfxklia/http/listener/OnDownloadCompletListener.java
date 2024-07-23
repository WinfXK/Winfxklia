/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/29  下午2:19*/
package com.winfxk.winfxklia.http.listener;

import com.winfxk.winfxklia.http.HttpDownload;

import java.io.File;

public interface OnDownloadCompletListener {
    void onDownloadComplet(HttpDownload download, File file, long titk, boolean isComplete, Throwable throwable);
}
