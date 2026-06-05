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
* Created Date: 2026/06/05 16:11 */
package cn.winfxk.android.mylibrary.view.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.winfxk.android.mylibrary.view.settings.items.ButtonItem
import cn.winfxk.android.mylibrary.view.settings.items.HeaderItem
import cn.winfxk.android.mylibrary.view.settings.items.InputItem
import cn.winfxk.android.mylibrary.view.settings.items.LineItem
import cn.winfxk.android.mylibrary.view.settings.items.MultiChoiceItem
import cn.winfxk.android.mylibrary.view.settings.items.RatingItem
import cn.winfxk.android.mylibrary.view.settings.items.SettingItem
import cn.winfxk.android.mylibrary.view.settings.items.SingleChoiceItem
import cn.winfxk.android.mylibrary.view.settings.items.SliderItem
import cn.winfxk.android.mylibrary.view.settings.items.SwitchItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SettingItemUI(item: SettingItem<*>, scope: CoroutineScope) {
    when (item) {
        is InputItem  -> SettingInputItem(
            item.title, item.value ?: "", ! item.isLoading,
            hint = if (item.isLoading) "加载中..." else item.hint,
            onValueChange = { item.value = it }
        )
        is SwitchItem -> SettingSwitchItem(
            item.title, item.value ?: false, ! item.isLoading,
            onCheckedChange = { newVal ->
                item.value = newVal
                triggerSave(item, scope)
            }
        )
        is SliderItem       -> SettingSliderItem(
            title = item.title,
            value = item.value ?: item.range.start,
            range = item.range,
            steps = item.steps,
            enabled = ! item.isLoading,
            onValueChange = { item.value = it },
            onValueChangeFinished = { triggerSave(item, scope) }
        )
        is RatingItem       -> SettingRatingItem(
            title = item.title,
            value = item.value ?: 0,
            maxStars = item.maxStars,
            enabled = ! item.isLoading,
            onValueChange = { newVal ->
                item.value = newVal
                triggerSave(item, scope)
            }
        )
        is SingleChoiceItem -> SettingSingleChoiceItem(
            title = item.title,
            value = item.value ?: "",
            options = item.options,
            enabled = ! item.isLoading,
            onValueChange = { newVal ->
                item.value = newVal
                triggerSave(item, scope)
            }
        )
        is MultiChoiceItem -> SettingMultiChoiceItem(
            title = item.title,
            value = item.value ?: emptySet(),
            options = item.options,
            enabled = ! item.isLoading,
            onValueChange = { newVal ->
                item.value = newVal
                triggerSave(item, scope)
            }
        )
        is HeaderItem -> SettingHeaderItem(title = item.title)
        is ButtonItem -> SettingButtonItem(title = item.title, onClick = item.onClick)
        is LineItem   -> SettingLineItem()
    }
}

/** 统一处理保存态 */
private fun <T> triggerSave(item: SettingItem<T>, scope: CoroutineScope) {
    scope.launch {
        item.isLoading = true
        try {
            item.save()
        } finally {
            item.isLoading = false
        }
    }
}

@Composable
private fun SettingItemContainer(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp) // 加大触摸面积，提升现代感
            .then(if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun SettingSwitchItem(
    title: String, checked: Boolean, enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier
) {
    SettingItemContainer(modifier = modifier, enabled = enabled, onClick = { if (enabled) onCheckedChange(! checked) }) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W500),
            modifier = Modifier.weight(1f),
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun SettingInputItem(
    title: String, value: String, enabled: Boolean,
    onValueChange: (String) -> Unit, modifier: Modifier = Modifier, hint: String
) {
    SettingItemContainer(modifier = modifier, enabled = enabled) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W500),
            modifier = Modifier.padding(end = 16.dp),
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        BasicTextField(
            value = value, onValueChange = onValueChange, enabled = enabled, modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 16.sp, textAlign = TextAlign.End
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(text = hint, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun SettingSliderItem(
    title: String, value: Float, range: ClosedFloatingPointRange<Float>, steps: Int, enabled: Boolean,
    onValueChange: (Float) -> Unit, onValueChangeFinished: () -> Unit, modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W500),
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = String.format(java.util.Locale.CHINA, "%.2f", value),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value, onValueChange = onValueChange, onValueChangeFinished = onValueChangeFinished,
            valueRange = range, steps = steps, enabled = enabled, modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

@Composable
private fun SettingRatingItem(
    title: String, value: Int, maxStars: Int, enabled: Boolean,
    onValueChange: (Int) -> Unit, modifier: Modifier = Modifier
) {
    val starActiveColor = Color(0xFFFFC107)
    val starInactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    SettingItemContainer(modifier = modifier, enabled = enabled) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W500),
            modifier = Modifier.weight(1f),
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { // 增加星星之间的间距
            for (i in 1 .. maxStars) {
                val isSelected = i <= value
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 0.85f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "starScale"
                )
                Icon(
                    imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star $i",
                    tint = if (! enabled) starInactiveColor.copy(alpha = 0.5f)
                    else if (isSelected) starActiveColor
                    else starInactiveColor,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = enabled
                        ) { onValueChange(i) }
                        .padding(4.dp)
                        .size(32.dp)
                        .scale(scale)
                )
            }
        }
    }
}

@Composable
private fun SettingSingleChoiceItem(
    title: String, value: String, options: List<String>, enabled: Boolean,
    onValueChange: (String) -> Unit, modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    SettingItemContainer(modifier = modifier, enabled = enabled, onClick = { if (enabled) expanded = true }) {
        Text(text = title,
             modifier = Modifier.weight(1f),
             style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W500),
             color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Box {
            Text(text = value.ifEmpty { "请选择" },
                 style = MaterialTheme.typography.bodyMedium,
                 color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                 modifier = Modifier.padding(8.dp))
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { onValueChange(option); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun SettingMultiChoiceItem(
    title: String, value: Set<String>, options: List<String>, enabled: Boolean,
    onValueChange: (Set<String>) -> Unit, modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(text = title,
             style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W500),
             color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)) {
            options.forEach { option ->
                val isChecked = value.contains(option)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = enabled) { onValueChange(if (isChecked) value - option else value + option) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isChecked, onCheckedChange = null, enabled = enabled)
                    Text(text = option,
                         style = MaterialTheme.typography.bodyLarge,
                         modifier = Modifier.padding(start = 12.dp),
                         color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun SettingHeaderItem(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(), // 分组名大写更具现代感
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingButtonItem(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingItemContainer(modifier = modifier, onClick = onClick) {
        Text(text = title,
             modifier = Modifier.fillMaxWidth(),
             textAlign = TextAlign.Center,
             style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600, color = MaterialTheme.colorScheme.primary))
    }
}

@Composable
private fun SettingLineItem(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(vertical = 8.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}