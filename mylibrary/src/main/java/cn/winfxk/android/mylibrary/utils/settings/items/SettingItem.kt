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
* Created PCUser: Winfx 
* Web: http://winfxk.com
* Created Date: 2026/6/5  10:46 */
package cn.winfxk.android.mylibrary.utils.settings.items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 设置项数据基类。支持挂起函数、状态追踪。
 * @param id 组件ID, 用于唯一标识
 * @param title 显示的标题
 * @param getValue 获取填充值
 * @param saveValue 保存值
 * @param immediateSave 是否在状态改变时立即保存（Switch 一般是 true，Input 一般是 false）
 */
sealed class SettingItem<T>(
    val id: String,
    val title: String,
    val getValue: suspend () -> T,
    val saveValue: suspend (T) -> Unit,
    val immediateSave: Boolean
) {
    var value by mutableStateOf<T?>(null)
    var isLoading by mutableStateOf(true)
    suspend fun load() {
        isLoading = true
        try {
            value = getValue()
        } finally {
            isLoading = false
        }
    }

    suspend fun save() {
        value?.let { saveValue(it) }
    }
}
