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
* Created Date: 2025/12/16  14:19 */
package cn.winfxk.android.mylibrary.view.menu

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import java.io.File

class MenuItemBuilder(val context: Context) {
    var title: String = ""
    var onClick: ModernMenuItem.() -> Unit = {}
    var iconDrawable: Drawable? = null
    var iconUrl: String? = null
    var iconFile: File? = null
    @ColorInt var backgroundColor: Int = 0xFF544336.toInt()
    @ColorInt var titleTextColor: Int = Color.WHITE
    @ColorInt var titleBackgroundColor: Int = 0xCC000000.toInt() // 80% 黑
    @DrawableRes var iconRes: Int? = null

    val finalDrawable: Drawable?
        get() = if (iconRes != null) AppCompatResources.getDrawable(context, iconRes !!) else iconDrawable
}