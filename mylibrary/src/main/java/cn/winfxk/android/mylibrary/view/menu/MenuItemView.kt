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
* Created Date: 2026/6/5  08:22 */
package cn.winfxk.android.mylibrary.view.menu

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.utils.BitmapUtils
import cn.winfxk.android.mylibrary.utils.Tablabel
import cn.winfxk.android.mylibrary.view.Menu
import cn.winfxk.android.mylibrary.view.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ViewConstructor")
internal class MenuItemView(context: Context, val item: ModernMenuItem, val main: Menu) : LinearLayout(context), Tablabel {
    private val labelCard = CardView(context)
    private val labelText = TextView(context)
    private val iconView = ImageView(context)
    private var loadJob: Job? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL or Gravity.END
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutParams.bottomMargin = main.itemSpacing
        this.layoutParams = layoutParams;
        clipChildren = false
        clipToPadding = false
        visibility = INVISIBLE
        labelCard.apply {
            radius = dp8f
            cardElevation = dp2f
            this.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                rightMargin = main.labelMargin
            }
            labelText.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setPadding(dp14, dp8, dp14, dp8)
                includeFontPadding = false
            }
            addView(labelText)
        }
        addView(labelCard)
        iconView.apply {
            this.layoutParams = LayoutParams(main.itemFabSizePx, main.itemFabSizePx)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            elevation = dp6f
            val iconPadding = if (main.itemFabSizePx > dp50) dp14 else dp10
            setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
        }
        addView(iconView)
        val clickListener = OnClickListener {
            main.performHaptic()
            item.onClick.invoke(item)
            main.collapse()
        }
        iconView.setOnClickListener(clickListener)
        this.setOnClickListener(clickListener)
    }

    fun bind() {
        labelText.text = item.title
        labelText.setTextColor(item.titleTextColor)
        labelCard.setCardBackgroundColor(item.titleBackgroundColor)
        labelCard.visibility = if (item.title.isEmpty()) GONE else VISIBLE
        val rippleColor = ColorStateList.valueOf(Color.argb(50, 255, 255, 255))
        val gradientBg = GradientDrawable()
        gradientBg.shape = GradientDrawable.OVAL
        gradientBg.setColor(item.getFixedBackgroundColor())
        iconView.background = RippleDrawable(rippleColor, gradientBg, null)
        cancelLoad()
        if (item.iconDrawable != null) {
            iconView.setImageDrawable(item.iconDrawable)
            return
        }
        if (item.iconRes != null) {
            iconView.setImageResource(item.iconRes !!)
            return
        }
        val scope = main.viewScope ?: return
        loadJob = scope.launch(Dispatchers.IO) {
            try {
                val bitmap = when {
                    item.iconFile != null -> BitmapUtils.decodeSampledBitmapFromFile(item.iconFile !!, 100, 100)
                    item.iconUrl != null  -> BitmapUtils.decodeSampledBitmapFromUrl(context, item.iconUrl !!, 100, 100)
                    else                  -> null
                }
                withContext(Dispatchers.Main) {
                    if (bitmap != null) iconView.setImageBitmap(bitmap)
                    else iconView.setImageResource(R.drawable.winfxklia_tip_empty)
                }
            } catch (e: Exception) {
                Log.e(tab, "bind: 在加载图标时出现异常！", e)
                withContext(Dispatchers.Main) {
                    iconView.setImageResource(R.drawable.winfxklia_tip_empty)
                }
            }
        }
    }

    fun cancelLoad() {
        loadJob?.cancel()
    }

    companion object {
        private val dp8 by lazy { 8.dp }
        private val dp14 by lazy { 14.dp }
        private val dp8f by lazy { dp8.toFloat() }
        private val dp2f by lazy { 2.dp.toFloat() }
        private val dp10 by lazy { 10.dp }
        private val dp6f by lazy { 6.dp.toFloat() }
        private val dp50 by lazy { 50.dp }
    }
}