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
* Created Date: 2025/11/4  16:58 */
package cn.winfxk.android.mylibrary.tip.dialog

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import cn.winfxk.android.mylibrary.R
@Suppress("unused")
@SuppressLint("InflateParams")
enum class Type(@field:DrawableRes val id: Int) {
    /**
     * 默认图标
     */
    Message(R.drawable.winfxklia_message),
    /**
     * 黄色问号
     */
    Ask(R.drawable.winfxklia_ask),
    /**
     * 蓝色感叹号
     */
    Caution(R.drawable.winfxklia_caution),
    /**
     * 奖章
     */
    Cite(R.drawable.winfxklia_cite),
    /**
     * 关闭
     */
    Close(R.drawable.winfxklia_close),
    /**
     * 确定
     */
    Confirm(R.drawable.winfxklia_confirm),
    /**
     * 空图标
     */
    Empty(R.drawable.winfxklia_empty),
    /**
     * 错误图标(红色感叹号)
     */
    Error(R.drawable.winfxklia_error),
    /**
     * 失败图标(红色叉)
     */
    Fail(R.drawable.winfxklia_fail),
    /**
     * 提示信息(小铃铛)
     */
    Info(R.drawable.winfxklia_info),
    /**
     * 输入图标
     */
    Input(R.drawable.winfxklia_input),
    /**
     * 选择图标
     */
    Select(R.drawable.winfxklia_select),
    /**
     * 成功图标
     */
    Success(R.drawable.winfxklia_succeed),
    /**
     * 黄色感叹号
     */
    Warning(R.drawable.winfxklia_warn),
    /**
     * 加载图标
     */
    Loading(R.drawable.winfxklia_loading);

    companion object {
        val Progress = Loading;
        val Parload = Loading;
    }
}