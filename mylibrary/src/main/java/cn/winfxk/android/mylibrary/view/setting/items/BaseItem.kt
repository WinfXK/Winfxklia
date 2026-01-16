/* 
* Copyright Notice
* © [2024 - 2025] Winfxk. All rights reserved.
* The software, its source code, and all related documentation are the intellectual property of Winfxk. Any reproduction or distribution of this software or any part thereof must be clearly attributed to Winfxk and the original author. Unauthorized copying, reproduction, or distribution without proper attribution is strictly prohibited.
* For inquiries, support, or to request permission for use, please contact us at:
* Email: admin@winfxk.cn
* QQ: 2508543202
* Visit our homepage for more information: http://Winfxk.cn
* 
* --------- Create message ---------
* Created by IntelliJ ID
* Author： Winfxk
* Created PCUser: Winfx 
* Web: http://winfxk.com
* Created Date: 2025/12/12  09:31 */
package cn.winfxk.android.mylibrary.view.setting.items

import android.view.View
import cn.winfxk.android.mylibrary.view.setting.BaseSetting

typealias GetValue = BaseItem.() -> Any?
typealias SetValue = BaseItem.(Any?) -> Unit
typealias SettingItemClickListener = BaseItem.() -> Unit

val empGet: GetValue = { null }
val empSet: SetValue = {}
/**
 * @param title 选项标题
 */
abstract class BaseItem(title: String, val get: GetValue, val set: SetValue) {
    /**
     * 设置项的界面
     */
    abstract val view: View;
    /**
     * 设置或获取选项文本
     */
    abstract var text: String;
    /**
     * 关联的上下文
     */
    @Volatile lateinit var context: BaseSetting;
    /**
     * 重读Value时调用
     */
    abstract fun reloadValue();
    /**
     * 初始化时调用
     */
    abstract fun init();
    /**
     * 保存
     */
    abstract fun save();
    fun getItems() = context.items;
    /**
     * 所属Item被点击时调用
     */
    open fun onItemClick() {
    }
}