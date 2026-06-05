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

import cn.winfxk.android.mylibrary.tip.dialog.DialogType
import cn.winfxk.android.winfxklia.databinding.MainActivityBinding


class MainActivity : cn.winfxk.android.mylibrary.view.settings.BaseSetting() {
    override val binding by lazy { MainActivityBinding.inflate(layoutInflater) }
    override val recyclerView by lazy { binding.listView }

    override fun init() {
        setContentView(binding.root)
        settings {
            addHeader(title = "通用设置")
            val single = addSingleChoice(
                id = "theme",
                title = "应用主题",
                options = listOf("浅色", "深色", "跟随系统"),
                getValue = { "跟随系统" },
                saveValue = { /* 保存 */ }
            )
            val switch = addSwitch(id = "switch", text = "开启硬件加速", getValue = { true }, saveValue = { })
            val slider = addSlider(
                id = "volume",
                title = "音量大小",
                range = 0f .. 100f,
                getValue = { 50f },
                saveValue = { /* 保存 */ }
            )

            addLine()
            addHeader(title = "高级功能")

            val multi = addMultiChoice(
                id = "features",
                title = "启用特性",
                options = listOf("实验性UI", "后台唤醒", "硬件加速"),
                getValue = { setOf("后台唤醒") },
                saveValue = { /* 保存 */ }
            )

            val rating = addRating(
                id = "score",
                title = "给我们打分",
                maxStars = 5,
                getValue = { 4 },
                saveValue = { /* 保存 */ }
            )
            addButton(
                "save",
                "保存",
            ) {
                tip("""
                    应用主题: ${single.value}
                    音量大小: ${slider.value}
                    启用特性: ${multi.value}
                    评分: ${rating.value}
                    开启硬件加速: ${switch.value}
                    """.trimIndent(), DialogType.Message)
            }
        }

    }
}
