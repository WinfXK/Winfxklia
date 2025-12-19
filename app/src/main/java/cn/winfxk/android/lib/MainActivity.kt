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
import cn.winfxk.android.mylibrary.tip.Toast
import cn.winfxk.android.mylibrary.tip.dialog.input.InputBuilder
import cn.winfxk.android.mylibrary.view.menu.ModernFloatingMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.lazy

class MainActivity : BaseActivity() {
    private val efm by lazy { findViewById<ModernFloatingMenu>(R.id.floating_menu) }
    override fun getLayoutId(): Int = R.layout.activity_main

    override fun initializeView() {
        efm.addItem {
            var index1 = 0
            val name1 = { "SB$index1" }
            title = name1()
            iconRes = cn.winfxk.android.mylibrary.R.drawable.winfxklia_toast_icon1
            onClick = {
                index1 ++
                title = name1()
                Toast.makeText(this@MainActivity, "我点击的是SB(${name1()})").show()
            }
        }
        efm.addItem {
            var index2 = 0
            val name2 = { "DSB$index2" }
            title = name2()
            iconUrl = "https://avatars.githubusercontent.com/u/17727619?s=64&v=4"
            onClick = {
                index2 ++
                title = name2()
                Toast.makeText(this@MainActivity, "我点击的是DSB").show()
            }
        }
        scope.launch(Dispatchers.Main) {
            delay(1000)
            val builder = InputBuilder(this@MainActivity);
            builder.add {
                title = "名称"
                iconRes = cn.winfxk.android.mylibrary.R.drawable.winfxklia_cite
                hint = "请输入名称"
                onClick = {
                    Toast.makeText(this@MainActivity, "我点击了$title").show()
                    false
                }
            }
            builder.add {
                title="密码"
                hint = "请输入密码"
                onClick = {
                    Toast.makeText(this@MainActivity, "我点击了$title").show()
                    false
                }
            }
            builder.add {
                hint = "请输入描述"
                onClick = {
                    Toast.makeText(this@MainActivity, "我点击了$title").show()
                    false
                }
            }
            builder.addButton("保存") {
                Toast.makeText(this@MainActivity, "点击了保存").show()
                !it;
            }
            builder.show()
        }
    }
}