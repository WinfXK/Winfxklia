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
* Created Date: 2026/6/3  08:23 */
package cn.winfxk.android.mylibrary.tip.dialog


/**
 * 智能表单返回结果集合
 */
@Suppress("UNUSED")
class DialogResult(private val data: Map<String, String>) {
    /**
     * 获取表单结果（字符串）
     * @param id 表单ID
     * @param default 默认值
     */
    fun getString(id: String, default: String = ""): String = data[id] ?: default

    /**
     * 获取表单结果（数字）
     * @param id 表单ID
     * @param default 默认值
     */
    fun getInt(id: String, default: Int = 0): Int = data[id]?.toIntOrNull() ?: default

    /**
     * 获取表单结果（小数）
     * @param id 表单ID
     * @param default 默认值
     */
    fun getDouble(id: String, default: Double = 0.0): Double = data[id]?.toDoubleOrNull() ?: default

    /**
     * 获取表单结果（布尔值）
     * @param id 表单ID
     * @param default 默认值
     */
    fun getBoolean(id: String, default: Boolean = false): Boolean = data[id]?.toBooleanStrictOrNull() ?: default

    /** 打印所有结果，方便调试 */
    override fun toString(): String = data.toString()
}