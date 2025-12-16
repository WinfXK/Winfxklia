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
* Created PCUser: kc4064
* Web: http://winfxk.com
* Created Date: 2025/12/16 15:10 */
package cn.winfxk.android.mylibrary.view.menu

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import java.io.File
import java.util.UUID
import kotlin.properties.Delegates

class ModernMenuItem internal constructor(
    private val uiUpdater: (ModernMenuItem) -> Unit
) {
    val id: String = UUID.randomUUID().toString()
    var title: String by Delegates.observable("") { _, old, new ->
        if (old != new) uiUpdater(this)
    }
    var onClick: ModernMenuItem. () -> Unit = {}
    var iconDrawable: Drawable? by Delegates.observable(null) { _, _, _ -> uiUpdater(this) }
    var iconUrl: String? by Delegates.observable(null) { _, old, new -> if (old != new) uiUpdater(this) }
    var iconFile: File? by Delegates.observable(null) { _, old, new -> if (old != new) uiUpdater(this) }
    @delegate:ColorInt
    var backgroundColor: Int by Delegates.observable(0xFF544336.toInt()) { _, old, new -> if (old != new) uiUpdater(this) }
    @delegate:ColorInt
    var titleTextColor: Int by Delegates.observable(0xFFFFFFFF.toInt()) { _, old, new -> if (old != new) uiUpdater(this) }
    @delegate:ColorInt
    var titleBackgroundColor: Int by Delegates.observable(0xCC000000.toInt()) { _, old, new -> if (old != new) uiUpdater(this) }

    /**
     * 确保颜色包含 Alpha 通道
     */
    @ColorInt
    fun getFixedBackgroundColor(): Int {
        return if (backgroundColor ushr 24 == 0) backgroundColor or 0xFF000000.toInt() else backgroundColor
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ModernMenuItem
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}