/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/20  上午10:19*/
package com.winfxk.winfxklia.tool;

import androidx.annotation.NonNull;
import com.winfxk.winfxklia.tool.able.Tabable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class MyFile implements Tabable {
    private final String path;

    public MyFile(@NotNull String path) {
        this(path, null);
    }

    public MyFile(@NotNull String path, String name) {
        path = path.replace("\\", "/");
        if (path.isEmpty()) path = "/";
        if (name == null || name.isEmpty()) {
            this.path = path;
        } else {
            name = name.replace("\\", "/");
            this.path = path + (path.endsWith("/") ? "" : "/") + name;
        }
    }

    public MyFile getParent() {
        if (path.isEmpty() || path.equals("/")) return null;
        int lastSlashIndex = path.lastIndexOf("/");
        if (path.endsWith("/")) lastSlashIndex = path.substring(0, lastSlashIndex).lastIndexOf("/");
        if (lastSlashIndex == 0) return new MyFile("/");
        else if (lastSlashIndex < 0) return null;
        else return new MyFile(path.substring(0, lastSlashIndex));
    }

    public String getName() {
        if (path.contains("/")) {
            if (path.endsWith("/")) {
                return path.substring(path.lastIndexOf("/", path.length() - 2) + 1, path.length() - 1);
            } else return path.substring(path.lastIndexOf("/") + 1);
        } else return path;
    }

    public String getPath() {
        return path;
    }

    @NonNull
    @NotNull
    @Override
    public String toString() {
        return getPath();
    }

    public static String getTag() {
        return MyFile.class.getSimpleName();
    }

    @Override
    public String getTAG() {
        return getClass().getSimpleName();
    }
}
