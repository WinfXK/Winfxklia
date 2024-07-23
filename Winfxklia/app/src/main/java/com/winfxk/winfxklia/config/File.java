/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/5  上午8:08*/
package com.winfxk.winfxklia.config;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;

public class File extends java.io.File {
    private static final String tag = "Winfxk File";

    public File(java.io.File file) {
        this(file.getAbsolutePath());
    }

    public File(@NonNull @NotNull String pathname) {
        super(pathname);
    }

    public File(@Nullable @org.jetbrains.annotations.Nullable String parent, @NonNull @NotNull String child) {
        super(parent, child);
    }

    public File(@Nullable @org.jetbrains.annotations.Nullable java.io.File parent, @NonNull @NotNull String child) {
        super(parent, child);
    }

    public File(@NonNull @NotNull URI uri) {
        super(uri);
    }

    @Override
    public boolean createNewFile() throws IOException {
        File parent = getParentFile();
        if (parent != null && (!parent.exists() || !parent.isDirectory()))
            if (!parent.mkdirs()) Log.w(getTag(), "创建文件所属父文件夹(" + this + ")时出现异常！创建可能并未成功！");
        return super.createNewFile();
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public File getParentFile() {
        String parent = getParent();
        if (parent == null) return null;
        return new File(parent);
    }

    public String getTag() {
        return tag + " " + getName();
    }
}
