/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/5  上午10:30*/
package com.winfxk.winfxklia.view.setting

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import com.winfxk.winfxklia.config.File
import java.io.FileInputStream

class SettingItem(var title: String? = null, var hint: String? = null, var bitmap: Bitmap? = null, var listener: SettingListener? = null,
                  var switchListener: SettingListener? = null, var view: View? = null, val type: Type? = null, value: Any? = null,
                  var obj: BaseValue? = null) {
    var value: GetValue? = if (value is GetValue) value else object : GetValue {
        override fun getValue(): Any {
            return value ?: "";
        }
    }
    private var isResource: Boolean = false;
    private var resourceID = 0;
    private var url: String? = null;


    fun setBitmap(file: File): SettingItem {
        val fis = FileInputStream(file);
        bitmap = BitmapFactory.decodeStream(fis);
        isResource = false;
        url = null;
        return this;
    }

    fun setBitmap(resourcesID: Int): SettingItem {
        this.resourceID = resourcesID
        isResource = true
        url = null;
        return this;
    }

    fun setBitmap(url: String): SettingItem {
        this.url = url;
        isResource = true
        return this;
    }
}
