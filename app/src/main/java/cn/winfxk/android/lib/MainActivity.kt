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
* Created Date: 2025/11/5  10:33 */
package cn.winfxk.android.lib

import android.util.Log
import cn.winfxk.android.mylibrary.BaseActivity
import cn.winfxk.android.mylibrary.tip.Toast
import cn.winfxk.android.mylibrary.tip.dialog.input.InputBuilder

class MainActivity : BaseActivity() {
    override fun getLayoutId(): Int = R.layout.main_activity

    init {
        Log.i(tag, "SB")
    }

    override fun initializeView() {
        val builder = InputBuilder(this);
        builder.message = "请输入用户名和密码！"
        val user = builder.add("user", "请输入用户名", "用户名")
        val passwd = builder.add("passwd", "请输入密码", icon = cn.winfxk.android.mylibrary.R.drawable.winfxklia_succeed)
        builder.addButton("确定") {
            val username = user.text;
            if (username.isBlank()) {
                builder.message = "用户名不能为空！";
                user.editText.requestFocus()
                return@addButton false
            }
            val password = passwd.text;
            if (password.isBlank()) {
                builder.message = "密码不能为空！";
                passwd.editText.requestFocus()
                return@addButton false
            }
            Toast.makeText(this, "用户：$username, 密码：$password").show()
            return@addButton true
        }
        builder.show();
    }
}