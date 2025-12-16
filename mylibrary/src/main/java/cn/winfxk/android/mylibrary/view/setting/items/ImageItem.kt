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
* Created Date: 2025/12/12  10:41 */
package cn.winfxk.android.mylibrary.view.setting.items

import android.view.View
import android.widget.TextView
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.view.ImageView

class ImageItem(title: String, val click: SettingItemClickListener) : BaseTitleItem(title, empGet, empSet), View.OnClickListener {
    override val view: View by lazy { View.inflate(context, R.layout.winfxklia_basesettings_item_image, null) }
    override val textView: TextView by lazy { view.findViewById(R.id.textView1) }
    val image: ImageView by lazy { view.findViewById(R.id.imageView1) }

    override fun reloadValue() {
    }

    override fun init() {
        image.setOnClickListener(this)
        super.init()
    }

    override fun save() {
    }

    override fun onClick(v: View?) {
        click.invoke(this);
    }
}