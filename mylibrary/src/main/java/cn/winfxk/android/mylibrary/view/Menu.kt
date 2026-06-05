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
* Created Date: 2026/06/05 08:15
*/
package cn.winfxk.android.mylibrary.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import androidx.core.view.setMargins
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.view.menu.MenuItemBuilder
import cn.winfxk.android.mylibrary.view.menu.MenuItemView
import cn.winfxk.android.mylibrary.view.menu.ModernMenuItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * ```
 * <cn.winfxk.android.mylibrary.view.Menu
 *  android:id="@+id/menu"
 *  android:layout_width="match_parent"
 *  android:layout_height="match_parent"
 *  style="@style/Theme.Winfxklia.Menu" />
 *  ```
 */
@Suppress("UNUSED")
class Menu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var itemFabSizePx: Int = 56.dp
    var animationDuration = 350L
    var itemSpacing = 8.dp
    var menuRotationAngle = 135f
    var labelMargin = 12.dp
    var ishapticFeedbackEnabled = true

    @ColorInt
    var defaultItemColor: Int = 0xFF544336.toInt()
    private var isExpanded = false
    private val items = mutableListOf<ModernMenuItem>()
    internal var viewScope: CoroutineScope? = null
    private val mainFab: FloatingActionButton
    private val itemsContainer: LinearLayout
    private val overlayView: View
    private var onStateChangeListener: ((Boolean) -> Unit)? = null
    private val springInterpolator = PathInterpolator(0.34f, 1.56f, 0.64f, 1f)
    private val accelerateInterpolator = AccelerateInterpolator()
    var mainFabSize: Int = FloatingActionButton.SIZE_NORMAL
        set(value) {
            field = value; updateFabSize()
        }

    init {
        clipChildren = false
        clipToPadding = false
        overlayView = View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setBackgroundColor("#B3000000".toColorInt())
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
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM or Gravity.END
            }
        }
        super.addView(itemsContainer)
        mainFab = FloatingActionButton(context).apply {
            id = generateViewId()
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(dp24)
            }
            size = mainFabSize
            setImageResource(R.drawable.winfxklia_efm_ic_add)
            setOnClickListener {
                performHaptic()
                toggle()
            }
            imageTintList = null
            elevation = dp12f
            compatElevation = dp12f
        }
        super.addView(mainFab)
        post { alignContainer() }
    }

    /**
     * 更加现代、优雅的 DSL 初始化方式
     */
    fun setup(init: Menu.() -> Unit) {
        this.init()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        for (i in 0 until itemsContainer.childCount)
            (itemsContainer.getChildAt(i) as? MenuItemView)?.bind()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope?.cancel()
        viewScope = null
    }

    private fun alignContainer() {
        val fabParams = mainFab.layoutParams as LayoutParams
        val containerParams = itemsContainer.layoutParams as LayoutParams
        val fabWidth = if (mainFab.size == FloatingActionButton.SIZE_MINI) dp40 else dp56
        val diff = (fabWidth - itemFabSizePx) / 2
        containerParams.rightMargin = fabParams.rightMargin + diff
        val fabHeight = if (mainFab.size == FloatingActionButton.SIZE_MINI) dp40 else dp56
        itemsContainer.setPadding(0, dp50, 0, fabHeight + fabParams.bottomMargin + itemSpacing)
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
        mainFab.animate().rotation(menuRotationAngle).setDuration(animationDuration).setInterpolator(springInterpolator).start()
        overlayView.visibility = VISIBLE
        overlayView.animate().alpha(1f).setDuration(animationDuration).start()
        itemsContainer.visibility = VISIBLE
        val childCount = itemsContainer.childCount
        for (i in 0 until childCount) {
            val child = itemsContainer.getChildAt(i)
            child.visibility = VISIBLE
            child.alpha = 0f
            child.translationY = dp50f
            child.scaleX = 0.7f
            child.scaleY = 0.7f
            child.animate().alpha(1f).translationY(0f)
                .scaleX(1f).scaleY(1f).setDuration(animationDuration)
                .setStartDelay((childCount - 1 - i) * 35L).setInterpolator(springInterpolator).start()
        }
    }

    fun collapse() {
        if (! isExpanded) return
        isExpanded = false
        onStateChangeListener?.invoke(false)
        performHaptic()
        mainFab.animate().rotation(0f).setDuration(animationDuration).setInterpolator(springInterpolator).start()
        overlayView.animate().alpha(0f).setDuration(animationDuration)
            .withEndAction { overlayView.visibility = GONE }.start()
        val childCount = itemsContainer.childCount
        for (i in 0 until childCount) {
            val child = itemsContainer.getChildAt(i)
            child.animate().alpha(0f)
                .translationY(dp30f).scaleX(0.8f).scaleY(0.8f).setDuration(animationDuration - 50L)
                .setStartDelay(i * 20L).setInterpolator(accelerateInterpolator).withEndAction { child.visibility = INVISIBLE }.start()
        }
        postDelayed({ if (! isExpanded) itemsContainer.visibility = INVISIBLE }, animationDuration)
    }

    /**
     * 添加菜单项 (DSL)
     */
    fun addItem(init: MenuItemBuilder.() -> Unit): String {
        val builder = MenuItemBuilder(context).apply(init)
        val item = builder.build { refreshItemView(it) }
        items.add(item)
        val itemView = MenuItemView(context, item, this)
        itemsContainer.addView(itemView, 0)
        itemView.bind()
        return item.id
    }

    private fun refreshItemView(item: ModernMenuItem) {
        for (i in 0 until itemsContainer.childCount) {
            val child = itemsContainer.getChildAt(i) as? MenuItemView
            if (child?.item?.id == item.id) {
                child.bind()
                break
            }
        }
    }

    internal fun performHaptic() {
        if (! ishapticFeedbackEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        else performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun clear() {
        items.clear()
        for (i in 0 until itemsContainer.childCount)
            (itemsContainer.getChildAt(i) as? MenuItemView)?.cancelLoad()
        itemsContainer.removeAllViews()
    }


    @Deprecated("不可直接添加子 View，请使用 setup 或 addItem DSL", level = DeprecationLevel.HIDDEN)
    override fun addView(child: View?) = super.addView(child)

    @Deprecated("不可直接添加子 View，请使用 setup 或 addItem DSL", level = DeprecationLevel.HIDDEN)
    override fun addView(child: View?, index: Int) = super.addView(child, index)

    @Deprecated("不可直接添加子 View，请使用 setup 或 addItem DSL", level = DeprecationLevel.HIDDEN)
    override fun addView(child: View?, width: Int, height: Int) = super.addView(child, width, height)

    @Deprecated("请使用 clear 动态控制", level = DeprecationLevel.HIDDEN)
    override fun removeView(view: View?) = super.removeView(view)

    companion object {
        private val dp24 by lazy { 24.dp }
        private val dp40 by lazy { 40.dp }
        private val dp56 by lazy { 56.dp }
        private val dp50 by lazy { 50.dp }
        private val dp30f by lazy { 30.dp.toFloat() }
        private val dp50f by lazy { dp50.toFloat() }
        private val dp12f by lazy { 12.dp.toFloat() }
    }
}