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
* Created Date: 2025/12/16 15:15 */
package cn.winfxk.android.mylibrary.view.menu

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColorInt
import androidx.core.view.setMargins
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.utils.dp
import cn.winfxk.android.mylibrary.utils.image.BitmapUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *
 *      <cn.winfxk.android.mylibrary.view.menu.ModernFloatingMenu
 *             android:id="@+id/floating_menu"
 *             android:layout_width="match_parent"
 *             android:layout_height="match_parent"
 *             style="@style/Theme.Winfxklia.ModernFloatingMenu" />
 */
class ModernFloatingMenu @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    var mainFabSize: Int = FloatingActionButton.SIZE_NORMAL
        set(value) {
            field = value; updateFabSize()
        }
    var itemFabSizePx: Int = 56.dp
    var animationDuration = 300L
    var itemSpacing = 5.dp // 增加间距
    var menuRotationAngle = 45f
    var labelMargin = 12.dp
    var ishapticFeedbackEnabled = true
    @ColorInt
    var defaultItemColor: Int = 0xFF544336.toInt()
    // --- 内部状态 ---
    private var isExpanded = false
    private val items = mutableListOf<ModernMenuItem>()
    private val itemViewMap = mutableMapOf<String, View>()
    private val viewJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + viewJob)
    private val mainFab: FloatingActionButton
    private val itemsContainer: LinearLayout
    private val overlayView: View
    private var onStateChangeListener: ((Boolean) -> Unit)? = null

    init {
        clipChildren = false
        clipToPadding = false
        overlayView = View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setBackgroundColor("#66000000".toColorInt())
            alpha = 0f
            visibility = GONE
            setOnClickListener { if (isExpanded) collapse() }
        }
        super.addView(overlayView)
        itemsContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END or Gravity.BOTTOM
            visibility = INVISIBLE
            clipChildren = false
            clipToPadding = false
            setPadding(0, 100.dp, 0, 80.dp)
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM or Gravity.END
            }
        }
        super.addView(itemsContainer)
        mainFab = FloatingActionButton(context).apply {
            id = generateViewId()
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(24.dp)
            }
            size = mainFabSize
            setImageResource(R.drawable.winfxklia_efm_ic_add)
            setOnClickListener {
                performHaptic()
                toggle()
            }
            imageTintList = null
            elevation = 12.dp.toFloat()
            compatElevation = 12.dp.toFloat()
        }
        super.addView(mainFab)
        post { alignContainer() }
    }

    /**
     * 校准容器位置，确保子菜单中心与 FAB 中心垂直对齐
     */
    private fun alignContainer() {
        val fabParams = mainFab.layoutParams as LayoutParams
        val containerParams = itemsContainer.layoutParams as LayoutParams
        val fabWidth = if (mainFab.size == FloatingActionButton.SIZE_MINI) 40.dp else 56.dp
        val itemWidth = itemFabSizePx
        val fabRightMargin = fabParams.rightMargin
        val diff = (fabWidth - itemWidth) / 2
        containerParams.rightMargin = fabRightMargin + diff
        val fabHeight = if (mainFab.size == FloatingActionButton.SIZE_MINI) 40.dp else 56.dp
        itemsContainer.setPadding(0, 50.dp, 0, fabHeight + fabParams.bottomMargin + itemSpacing)
        itemsContainer.layoutParams = containerParams
    }

    private fun updateFabSize() {
        mainFab.size = mainFabSize
        post { alignContainer() }
    }


    fun toggle() {
        if (isExpanded) collapse() else expand()
    }

    fun expand() {
        if (isExpanded) return
        isExpanded = true
        onStateChangeListener?.invoke(true)
        performHaptic()
        mainFab.animate()
            .rotation(menuRotationAngle)
            .setDuration(animationDuration)
            .setInterpolator(OvershootInterpolator())
            .start()
        overlayView.visibility = VISIBLE
        overlayView.animate().alpha(1f).setDuration(animationDuration).start()
        itemsContainer.visibility = VISIBLE
        for (i in 0 until itemsContainer.childCount) {
            val child = itemsContainer.getChildAt(i)
            child.visibility = VISIBLE
            child.alpha = 0f
            child.translationY = 40f.dp
            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(animationDuration)
                .setStartDelay((itemsContainer.childCount - 1 - i) * 30L)
                .setInterpolator(OvershootInterpolator(1.5f)) // 弹性系数加大
                .start()
        }
    }

    fun collapse() {
        if (! isExpanded) return
        isExpanded = false
        onStateChangeListener?.invoke(false)
        performHaptic()
        mainFab.animate()
            .rotation(0f)
            .setDuration(animationDuration)
            .setInterpolator(OvershootInterpolator())
            .start()
        overlayView.animate().alpha(0f).setDuration(animationDuration)
            .withEndAction { overlayView.visibility = GONE }
            .start()
        for (i in 0 until itemsContainer.childCount) {
            val child = itemsContainer.getChildAt(i)
            child.animate()
                .alpha(0f)
                .translationY(30f.dp)
                .setDuration(animationDuration / 2)
                .setStartDelay(i * 20L)
                .start()
        }
        postDelayed({ if (! isExpanded) itemsContainer.visibility = INVISIBLE }, animationDuration)
    }


    /**
     * 添加菜单项 (DSL)
     */
    fun addItem(init: MenuItemBuilder.() -> Unit): String {
        val builder = MenuItemBuilder(context)
        builder.init()
        val item = ModernMenuItem { refreshItemView(it) }.apply {
            title = builder.title
            onClick = builder.onClick
            iconDrawable = builder.finalDrawable
            iconUrl = builder.iconUrl
            iconFile = builder.iconFile
            backgroundColor = builder.backgroundColor
            titleTextColor = builder.titleTextColor
            titleBackgroundColor = builder.titleBackgroundColor
        }
        items.add(item)
        addItemView(item)
        return item.id
    }

    private fun addItemView(item: ModernMenuItem) {
        val rowLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
            layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = itemSpacing
            }
            clipChildren = false
            clipToPadding = false
        }
        val labelCard = CardView(context).apply {
            radius = 6.dp.toFloat()
            cardElevation = 2.dp.toFloat()
            setCardBackgroundColor(item.titleBackgroundColor)
            layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                rightMargin = labelMargin
            }
            alpha = if (item.title.isEmpty()) 0f else 1f
        }
        val textView = TextView(context).apply {
            text = item.title
            setTextColor(item.titleTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f) // 加大字号
            setPadding(12.dp, 6.dp, 12.dp, 6.dp) // 加大Padding
            setShadowLayer(0f, 0f, 0f, 0)
            includeFontPadding = false
        }
        labelCard.addView(textView)
        rowLayout.addView(labelCard)
        val fabIcon = ImageView(context).apply {
            val size = itemFabSizePx
            layoutParams = LinearLayout.LayoutParams(size, size)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(item.getFixedBackgroundColor())
            }
            elevation = 6.dp.toFloat()
            val iconPadding = if (size > 50.dp) 14.dp else 10.dp
            setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
        }
        loadIconIntoView(item, fabIcon)
        val clickListener = OnClickListener {
            performHaptic()
            item.onClick.invoke(item)
            collapse()
        }
        fabIcon.setOnClickListener(clickListener)
        rowLayout.setOnClickListener(clickListener)
        rowLayout.addView(fabIcon)
        rowLayout.visibility = INVISIBLE
        rowLayout.tag = item.id
        itemViewMap[item.id] = rowLayout
        itemsContainer.addView(rowLayout, 0)
    }

    /**
     * 动态刷新单个 Item 的 UI
     */
    private fun refreshItemView(item: ModernMenuItem) {
        val rowView = itemViewMap[item.id] as? LinearLayout ?: return
        val card = rowView.getChildAt(0) as CardView
        val text = card.getChildAt(0) as TextView
        text.text = item.title
        text.setTextColor(item.titleTextColor)
        card.setCardBackgroundColor(item.titleBackgroundColor)
        card.visibility = if (item.title.isEmpty()) GONE else VISIBLE
        val icon = rowView.getChildAt(1) as ImageView
        (icon.background as? GradientDrawable)?.setColor(item.getFixedBackgroundColor())
        loadIconIntoView(item, icon)
    }

    private fun loadIconIntoView(item: ModernMenuItem, imageView: ImageView) {
        if (item.iconDrawable != null) {
            imageView.setImageDrawable(item.iconDrawable)
            return
        }
        scope.launch(Dispatchers.IO) {
            try {
                val bitmap = when {
                    item.iconFile != null -> BitmapUtils.decodeSampledBitmapFromFile(item.iconFile !!, 100, 100)
                    item.iconUrl != null  -> BitmapUtils.decodeSampledBitmapFromUrl(context, item.iconUrl !!, 100, 100)
                    else                  -> null
                }
                withContext(Dispatchers.Main) {
                    if (bitmap != null) imageView.setImageBitmap(bitmap)
                    else imageView.setImageResource(R.drawable.winfxklia_imageloading_error)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun performHaptic() {
        if (ishapticFeedbackEnabled) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun clear() {
        items.clear()
        itemsContainer.removeAllViews()
        itemViewMap.clear()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewJob.cancel()
    }

    // --- 隐藏不相关的父类 API ---

    @Deprecated("不应直接添加子 View，请使用 addItem", level = DeprecationLevel.HIDDEN)
    override fun addView(child: View?) {
        super.addView(child)
    }

    @Deprecated("不应直接添加子 View，请使用 addItem", level = DeprecationLevel.HIDDEN)
    override fun addView(child: View?, index: Int) {
        super.addView(child, index)
    }

    @Deprecated("不应直接添加子 View，请使用 addItem", level = DeprecationLevel.HIDDEN)
    override fun addView(child: View?, width: Int, height: Int) {
        super.addView(child, width, height)
    }

    @Deprecated("请勿移除内部组件", level = DeprecationLevel.HIDDEN)
    override fun removeView(view: View?) {
        super.removeView(view)
    }
}