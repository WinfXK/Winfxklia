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
* Created by IntelliJ IDEA
* Author： Winfxk
* Web: http://winfxk.com
* Created Date: 2026/06/02 16:53 */
@file:Suppress("UNUSED")

package cn.winfxk.android.mylibrary.tip

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.ScrollView
import cn.winfxk.android.mylibrary.tip.dialog.DialogBuilder
import cn.winfxk.android.mylibrary.tip.dialog.DialogType
import cn.winfxk.android.mylibrary.tip.dialog.MyBuilder

/**
 * 唤出“智能组合表单”类型的弹窗
 */
fun Context.showFormDialog(
    title: String,
    builder: DialogBuilder.() -> Unit
): MyBuilder {
    val formBuilder = DialogBuilder(this).apply(builder)
    return MyBuilder(this) {
        this.title = title
        this.type = DialogType.Empty
        val scrollView = ScrollView(context);
        scrollView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (context.resources.displayMetrics.heightPixels * 0.6).toInt()
        )
        scrollView.addView(formBuilder.container)
        getCustomContainer().addView(scrollView)
        if (formBuilder.showCancel) addButton(formBuilder.cancelText) { textColor = Color.GRAY }
        addButton(formBuilder.confirmText) {
            isDismissOnClick = false
            onClick { formBuilder.onConfirmCallback?.invoke(formBuilder.buildResult(), it) }
        }
    }.apply { show() }
}

fun MyBuilder.applyForm(builder: DialogBuilder.() -> Unit) {
    val formBuilder = DialogBuilder(this.context).apply(builder)
    this.type = DialogType.Empty
    this.clearButtons()
    this.clearCustomView()
    val scrollView = ScrollView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (context.resources.displayMetrics.heightPixels * 0.6).toInt()
        )
        addView(formBuilder.container)
    }
    this.addCustomView(scrollView)
    if (formBuilder.showCancel) addButton(formBuilder.cancelText) { textColor = Color.GRAY }
    addButton(formBuilder.confirmText) {
        isDismissOnClick = false
        onClick { formBuilder.onConfirmCallback?.invoke(formBuilder.buildResult(), it) }
    }
}

/**
 * 弹出一个Toast提示
 */
fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

/**
 * 弹出一个顶部提示
 */
fun Context.toptip(text: String, duration: Int = TopTip.LENGTH_SHORT) {
    TopTip.makeText(this, text, duration).show()
}