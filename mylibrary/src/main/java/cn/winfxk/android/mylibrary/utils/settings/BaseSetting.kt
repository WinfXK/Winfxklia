/*
* Copyright Notice
* © [2024 - 2026] Winfxk. All rights reserved.
* The software, its source code, and all related documentation are the intellectual property of Winfxk. Any reproduction or distribution of this software or any part thereof must be clearly attributed to Winfxk and the original author. Unauthorized copying, reproduction, or distribution without proper attribution is strictly prohibited.
* For inquiries, support, or to request permission for use, please contact us at:
* Email: admin@winfxk.cn
* QQ: 2508543202
* Visit our homepage for more information: http://Winfxk.cn
*
* --------- Create message ---------
* Created by IntelliJ ID
* Author： Winfxk
* Web: http://winfxk.com
* Created Date: 2026/06/05 10:28 */
package cn.winfxk.android.mylibrary.utils.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.winfxk.android.mylibrary.utils.settings.items.ButtonItem
import cn.winfxk.android.mylibrary.utils.settings.items.InputItem
import cn.winfxk.android.mylibrary.utils.settings.items.LineItem
import cn.winfxk.android.mylibrary.utils.settings.items.SettingItem
import cn.winfxk.android.mylibrary.utils.settings.items.SwitchItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


/**
 * 现代化的混合设置基类。
 * 兼容 DSL 方式添加 Item，内部封装 Compose 渲染引擎。
 * 支持两种接入模式：
 * 1. XML 模式：重写 [recyclerView] 返回实例，框架会自动注入 Compose Adapter。
 * 2. 纯 Compose 模式：[recyclerView] 返回 null，使用 [setContent] 并调用内置的 [SettingLayout]。
 */
abstract class BaseSetting : AppCompatActivity() {
    protected val scope: CoroutineScope get() = lifecycleScope
    private val _items = mutableStateListOf<SettingItem<*>>()
    /**
     * 【抽象成员属性】：由具体的业务实现类自己实现。
     * 允许使用传统 XML 并绑定 RecyclerView。如果使用 Compose，返回 null。
     */
    abstract val recyclerView: RecyclerView?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        recyclerView?.let { rv ->
            if (rv.layoutManager == null) rv.layoutManager = LinearLayoutManager(this)
            rv.adapter = SettingComposeAdapter(_items, scope)
        }
        _items.forEach { scope.launch { it.load() } }
    }

    /**
     * 界面初始化
     */
    abstract fun initView()

    /**
     * 增加输入框项。初始化时若有耗时 IO，输入框会保持 disabled 状态。
     */
    fun addInput(
        id: String,
        text: String,
        hint: String = "请输入...",
        getValue: suspend () -> String,
        saveValue: suspend (String) -> Unit
    ) = _items.add(InputItem(id, text, hint, getValue, saveValue))

    /**
     * 增加开关项
     */
    fun addSwitch(
        id: String,
        text: String,
        getValue: suspend () -> Boolean,
        saveValue: suspend (Boolean) -> Unit
    ) = _items.add(SwitchItem(id, text, getValue, saveValue))

    /**
     * 增加按钮/点击项
     */
    fun addButton(id: String, text: String, onClick: () -> Unit) =
        _items.add(ButtonItem(id, text, onClick))
    /**
     * 增加分割线
     */
    fun addLine(id: String = "line_${System.currentTimeMillis()}") = _items.add(LineItem(id))

    /**
     * 根据 ID 获取组件以进行进一步操作
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : SettingItem<*>> getItem(id: String): T? {
        return _items.find { it.id == id } as? T
    }

    /**
     * 统一保存：通常由底部的“保存配置”按钮触发。
     * @param all 是否保存所有项，包括 immediateSave == true 的项
     */
    fun saveAll(all: Boolean = false) {
        scope.launch {
            _items.filter { ! it.immediateSave || all }.forEach { it.save() }
        }
    }

    /**
     * 纯 Compose 模式使用的懒加载列表。
     * 如果不使用XmlLayout，在 initView 中调用 setContent { SettingLayout() } 即可。
     */
    @Composable
    fun SettingLayout(modifier: Modifier = Modifier) {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(_items.size) { SettingItemUI(item = _items[it], scope = scope) }
        }
    }
}
