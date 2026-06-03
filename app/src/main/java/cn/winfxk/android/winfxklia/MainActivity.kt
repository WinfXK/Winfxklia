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
* Created Date: 2026/6/2  14:14 */
package cn.winfxk.android.winfxklia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import cn.winfxk.android.mylibrary.tip.Toast
import cn.winfxk.android.mylibrary.tip.TopTip
import cn.winfxk.android.mylibrary.tip.applyForm
import cn.winfxk.android.mylibrary.tip.dialog.DialogType
import cn.winfxk.android.mylibrary.tip.dialog.MyBuilder
import cn.winfxk.android.mylibrary.tip.showFormDialog
import cn.winfxk.android.winfxklia.databinding.MainActivityBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private val bind by lazy { MainActivityBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)
        bind.confirm5.setOnClickListener {
            val dialog = MyBuilder(this) {
                type = DialogType.Loading
                title = "请稍候"
                message = "正在拼命加载数据中..."
            }
            dialog.show()
            lifecycleScope.launch {
                delay(3000)
                dialog.type = DialogType.Success
                dialog.message = "加载成功"
                dialog.addButton("确定")
                dialog.addButton("测试") {
                    isDismissOnClick = false
                    onClick {
                        dialog.type = DialogType.Loading
                        dialog.message = "正在拼命加载数据中..."
                        dialog.clearButtons()
                        lifecycleScope.launch {
                            delay(3000)
                            dialog.type = DialogType.Success
                            dialog.message = "加载成功"
                            dialog.addButton("确定")
                        }
                    }
                }
            }
        }
        bind.confirm4.setOnClickListener {
            // 第一步：直接打开初始表单
            this.showFormDialog("填写用户信息") {
                input(id = "username", label = "用户名", hint = "请输入您的账号")
                password(id = "pwd", label = "密码", hint = "请输入登录密码")

                onConfirm("提交注册") { result, dialog ->
                    // 获取数据
                    val username = result.getString("username")
                    if (username.isBlank()) {
                        dialog.type = DialogType.Error
                        dialog.message = "用户名不能为空！"
                        return@onConfirm
                    }

                    // 第二步：清理视图，进入 Loading
                    dialog.clearCustomView()
                    dialog.clearButtons()
                    dialog.type = DialogType.Loading
                    dialog.message = "正在为您注册账号，请稍候..."

                    // 模拟网络请求耗时
                    lifecycleScope.launch {
                        delay(2000)

                        // 第三步：注册成功，注入“选择分组”的新表单！
                        dialog.title = "选择您的分组"
                        dialog.message = "" // 清理掉刚才的 Loading 文字
                        dialog.applyForm {
                            select(id = "group", label = "可用分组", options = listOf("开发组", "测试组", "设计组"))

                            onConfirm("确认加入") { groupResult, finalDialog ->
                                // 第四步：进入最终 Loading
                                finalDialog.clearCustomView()
                                finalDialog.clearButtons()
                                finalDialog.type = DialogType.Loading
                                finalDialog.message = "正在为您分配至: ${groupResult.getString("group")}..."

                                lifecycleScope.launch {
                                    delay(1500)
                                    // 第五步：全部完成！
                                    finalDialog.type = DialogType.Success
                                    finalDialog.message = "全部设置成功！欢迎加入 ${groupResult.getString("group")}"
                                    finalDialog.addButton("进入系统")
                                }
                            }
                        }
                    }
                }
            }
        }
        bind.confirm3.setOnClickListener {
            Toast.makeText(this, "嘿嘿嘿，你好哇骚年").show()
        }
        bind.confirm2.setOnClickListener {
            TopTip.makeText(this, "收到一条新消息，请注意查收！").show()
        }
    }
}