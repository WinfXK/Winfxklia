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
* Created Date: 2026/6/4  17:03 */
package cn.winfxk.android.mylibrary.utils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import java.io.File

/** 将任意 Bitmap 快速转换为圆形 */
fun Bitmap.toCircular(): Bitmap = BitmapUtils.createCircularBitmap(this)

/** 将任意 Bitmap 快速添加指定圆角 */
fun Bitmap.toRoundedCorner(radius: Float): Bitmap = BitmapUtils.createRoundedCornerBitmap(this, radius)

/** 将任意 Bitmap 补充透明边距 */
fun Bitmap.addPadding(padding: Int): Bitmap = BitmapUtils.addTransparentPadding(this, padding)

/** 提取任意 Drawable 为 Bitmap */
fun Drawable.toBitmap(): Bitmap = BitmapUtils.drawableToBitmap(this)

/** 直接将此 Bitmap 安全保存至文件 */
suspend fun Bitmap.saveToFile(file: File, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): Boolean {
    return BitmapUtils.saveBitmapToFile(this, file, format, quality)
}