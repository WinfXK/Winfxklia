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
* Created Date: 2026/06/05 14:46 */

package cn.winfxk.android.mylibrary.utils.settings

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import cn.winfxk.android.mylibrary.theme.WinfxkliaTheme
import cn.winfxk.android.mylibrary.utils.settings.items.SettingItem
import kotlinx.coroutines.CoroutineScope

internal class SettingComposeAdapter(private val items: List<SettingItem<*>>, private val scope: CoroutineScope) : RecyclerView.Adapter<ComposeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeViewHolder {
        val composeView = ComposeView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
        return ComposeViewHolder(composeView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ComposeViewHolder, position: Int) {
        val item = items[position]
        holder.composeView.setContent {
            WinfxkliaTheme {
                SettingItemUI(item = item, scope = scope)
            }
        }
    }
}