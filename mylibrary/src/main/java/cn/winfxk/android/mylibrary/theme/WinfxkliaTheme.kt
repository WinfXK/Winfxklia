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
* Web: http://winfxk.com
* Created Date: 2026/06/05 14:23 */

package cn.winfxk.android.mylibrary.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import cn.winfxk.android.mylibrary.R

@Composable
fun WinfxkliaTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) darkColorScheme(
        primary = colorResource(id = R.color.winfxklia_primary),
        primaryContainer = colorResource(id = R.color.winfxklia_primary_variant),
        onPrimary = colorResource(id = R.color.winfxklia_black),
        secondary = colorResource(id = R.color.winfxklia_teal_200),
        secondaryContainer = colorResource(id = R.color.winfxklia_teal_200),
        onSecondary = colorResource(id = R.color.winfxklia_black),
        onBackground = colorResource(id = R.color.winfxklia_textColor1),
        onSurface = colorResource(id = R.color.winfxklia_textColor1)
    ) else lightColorScheme(
        primary = colorResource(id = R.color.winfxklia_primary),
        primaryContainer = colorResource(id = R.color.winfxklia_primary_variant),
        onPrimary = colorResource(id = R.color.winfxklia_white),
        secondary = colorResource(id = R.color.winfxklia_teal_200),
        secondaryContainer = colorResource(id = R.color.winfxklia_teal_700),
        onSecondary = colorResource(id = R.color.winfxklia_black),
        onBackground = colorResource(id = R.color.winfxklia_textColor1),
        onSurface = colorResource(id = R.color.winfxklia_textColor1)
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}