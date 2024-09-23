/*Created by IntelliJ IDEA.
  Author： Winfxk
  PCUser: kc4064 
  Date: 2024/8/13  下午1:38*/
package com.winfxk.winfxklia.view.setting.os

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.TextView
import com.winfxk.winfxklia.R
import com.winfxk.winfxklia.view.ImageView
import com.winfxk.winfxklia.view.setting.BaseSetting
import com.winfxk.winfxklia.view.setting.SettingItem
import com.winfxk.winfxklia.view.setting.SettingListener
import com.winfxk.winfxklia.view.setting.Type

abstract class OpenSource : BaseSetting(), View.OnClickListener {
    private lateinit var title: TextView;
    private lateinit var back: ImageView;
    private lateinit var listView: ListView;
    override fun onInitialize() {
        setContentView(R.layout.winfxkliba_open_source)
        title = findViewById(R.id.textView1)
        back = findViewById(R.id.imageView1)
        listView = findViewById(R.id.listView1)
        styleByTitle(title)
        styleByBack(back)
        styleByListView(listView)
    }

    open fun getDefaultItems(): ArrayList<ItemData> {
        return list;
    }

    abstract fun getItems(): ArrayList<ItemData>
    open fun styleByTitle(title: TextView) {

    }

    open fun styleByBack(back: ImageView) {
        back.setOnClickListener(this)
    }

    open fun styleByListView(listView: ListView) {
    }

    override fun getMenus(): MutableList<SettingItem> {
        val list = ArrayList<SettingItem>();
        val items = getDefaultItems()
        items.addAll(getItems())
        for (item in items)
            list.add(SettingItem(title = item.title, hint = item.hint, type = Type.Next, switchListener = object : SettingListener {
                override fun onSettingChanged(activity: BaseSetting, view: View) {
                    if (! item.url.isNullOrBlank()) openUrl(item.url);
                    if (item.listener != null) item.listener.onClickOpen(item);
                }
            }))
        return list;
    }

    override fun getListView(): ListView {
        return listView;
    }

    override fun onClick(v: View?) {
        finish()
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) })
        } catch (e: Exception) {
            Log.e(tag, "执行跳转操作时出现异常！", e)
        }
    }

    companion object {
        private val list = ArrayList<ItemData>();

        init {
            addItem("Winfxklia", "Regarding some encapsulated utility classes for Android, including Dialog and HTTP, etc.", "http://winfxk.cn")
            addItem("FastJson2", " FastJson2 is a Java JSON library with excellent performance.", "https://alibaba.github.io/fastjson2/")
            addItem("SnakeYaml", "a complete YAML 1.1 processor for JVM", "https://bitbucket.org/snakeyaml/workspace/repositories/")
            addItem("Appcompat", "Provides backwards-compatible implementations of UI-related Android SDK functionality, including dark mode and Material theming.", "https://developer.android.com/jetpack/androidx/releases/appcompat?hl=zh-cn")
            addItem("Material", "Modular and customizable Material Design UI components for Android", "https://github.com/material-components/material-components-android")
            addItem("AndroidX", "Jetpack is a suite of libraries, tools, and guidance to help developers write high-quality apps easier. These components help you follow best practices, free you from writing boilerplate code, and simplify complex tasks, so you can focus on the code you care about.", "https://github.com/androidx/androidx")
            addItem("Multidex", "Multidex is an Android library that allows an application to use more than the 65,536 methods limit. It is useful for large apps with many classes and methods.", "https://developer.android.com/studio/build/multidex");
        }

        private fun addItem(title: String, hint: String, url: String? = null) {
            list.add(ItemData(title, hint, url))
        }
    }
}