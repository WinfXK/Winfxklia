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
* Web: http://winfxk.com
* Created Date: 2025/12/12 17:30
*/

package cn.winfxk.android.lib

import cn.winfxk.android.mylibrary.BaseActivity
import cn.winfxk.android.mylibrary.tip.dialog.LoadingBuilder
import cn.winfxk.android.mylibrary.view.etv.EffectTextView
import kotlin.lazy

class MainActivity : BaseActivity() {
    override fun getLayoutId(): Int = R.layout.main_activity
    private val ev by lazy { findViewById<EffectTextView>(R.id.textView1) }
    private val array by lazy { resources.getStringArray(cn.winfxk.android.mylibrary.R.array.winfxklia_loading_motto).toMutableList() }
    override fun initializeView() {
        val builder = LoadingBuilder(this);
        builder.setCancelable(true)
        builder.show();
    }
}