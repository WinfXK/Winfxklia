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
* Created Date: 2026/06/05 15:37 */
package cn.winfxk.android.mylibrary.view.settings

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


/**
 * 设置项构建器
 * @property items 实际承载设置项列表的容器，构建好的 Item 会自动添加进该列表。
 */
@SettingDsl
@Suppress("UNUSED")
class SettingsBuilder(private val items: MutableList<SettingItem<*>>) {

    /**
     * 添加文本输入框设置项。
     * 适用于需要用户输入字符串、数字或其它文本信息的场景（通常失去焦点或手动保存时才落盘）。
     *
     * @param id 组件的唯一标识符，可通过 BaseSetting 的 getItem 方法检索。
     * @param text 显示在输入框左侧的标题/标签。
     * @param hint 当输入框值为空时显示的提示文案，默认为 "请输入..."。
     * @param getValue 异步获取输入框初始值的挂起函数。
     * @param saveValue 异步保存用户输入值的挂起函数。
     * @return 返回创建好的 [InputItem] 实例。
     */
    fun addInput(
        id: String,
        text: String,
        hint: String = "请输入...",
        getValue: suspend () -> String,
        saveValue: suspend (String) -> Unit
    ): InputItem = InputItem(id, text, hint, getValue, saveValue).also { items.add(it) }

    /**
     * 添加开关设置项。
     * 适用于布尔值类型的配置项。注意：开关状态一旦拨动，会立即触发 [saveValue]。
     *
     * @param id 组件的唯一标识符。
     * @param text 显示在开关左侧的标题/标签。
     * @param getValue 异步获取开关当前状态(开/关)的挂起函数。
     * @param saveValue 异步保存开关新状态的挂起函数。
     * @return 返回创建好的 [SwitchItem] 实例。
     */
    fun addSwitch(
        id: String,
        text: String,
        getValue: suspend () -> Boolean,
        saveValue: suspend (Boolean) -> Unit
    ): SwitchItem = SwitchItem(id, text, getValue, saveValue).also { items.add(it) }

    /**
     * 添加普通按钮/点击项。
     * 适用于需要执行特定操作（如清理缓存、检查更新等）而非单纯存储数据的场景。
     *
     * @param id 组件的唯一标识符。
     * @param text 按钮上显示的文字。
     * @param onClick 用户点击该行时触发的回调函数。
     * @return 返回创建好的 [ButtonItem] 实例。
     */
    fun addButton(
        id: String,
        text: String,
        onClick: () -> Unit
    ): ButtonItem = ButtonItem(id, text, onClick).also { items.add(it) }

    /**
     * 添加一条分割线。
     * 用于在视觉上分隔不同的设置区域，提升界面层次感。
     *
     * @param id 组件的唯一标识符（通常可忽略，系统会自动生成基于时间戳的默认 ID）。
     * @return 返回创建好的 [LineItem] 实例。
     */
    fun addLine(
        id: String = "line_${System.currentTimeMillis()}"
    ): LineItem = LineItem(id).also { items.add(it) }

    /**
     * 添加分组标题（Header）。
     * 用于标识下方一系列设置项的共同类别或归属。
     *
     * @param id 组件的唯一标识符。
     * @param title 标题显示的纯文本。
     * @return 返回创建好的 [HeaderItem] 实例。
     */
    fun addHeader(
        id: String = "header_${System.currentTimeMillis()}",
        title: String
    ): HeaderItem = HeaderItem(id, title).also { items.add(it) }

    /**
     * 添加滑动条设置项（Slider）。
     * 适用于需要在一个连续或离散的数值区间内进行调节的场景（如音量、亮度、阈值等）。
     * 拖拽结束时自动触发 [saveValue]。
     *
     * @param id 组件的唯一标识符。
     * @param title 滑动条上方或左侧显示的标题。
     * @param range 允许滑动的浮点数区间（默认为 0.0f 到 100.0f）。
     * @param steps 滑动条中的离散步数（锚点）。例如范围是 0..10，steps 是 9，则只能滑动整数。默认为 0（平滑滑动）。
     * @param getValue 异步获取滑动条初始位置值的挂起函数。
     * @param saveValue 滑动结束时，异步保存数值的挂起函数。
     * @return 返回创建好的 [SliderItem] 实例。
     */
    fun addSlider(
        id: String,
        title: String,
        range: ClosedFloatingPointRange<Float> = 0f..100f,
        steps: Int = 0,
        getValue: suspend () -> Float,
        saveValue: suspend (Float) -> Unit
    ): SliderItem = SliderItem(id, title, range, steps, getValue, saveValue).also { items.add(it) }

    /**
     * 添加星级评分/评级项。
     * 适用于等级选择或打分反馈场景。点击星星时会立即触发 [saveValue]。
     *
     * @param id 组件的唯一标识符。
     * @param title 评分条旁显示的说明文本。
     * @param maxStars 最大星星数量（默认 5 颗星）。
     * @param getValue 异步获取当前星星数量的挂起函数（1 到 maxStars）。
     * @param saveValue 用户点击评级后，异步保存所选星级的挂起函数。
     * @return 返回创建好的 [RatingItem] 实例。
     */
    fun addRating(
        id: String,
        title: String,
        maxStars: Int = 5,
        getValue: suspend () -> Int,
        saveValue: suspend (Int) -> Unit
    ): RatingItem = RatingItem(id, title, maxStars, getValue, saveValue).also { items.add(it) }

    /**
     * 添加单选项（通常表现为下拉菜单或底部弹窗形式）。
     * 适用于需要从预设的几个固定选项中挑选且只能选一个的场景（如主题切换：浅色/深色）。
     * 选择变更后会立即触发 [saveValue]。
     *
     * @param id 组件的唯一标识符。
     * @param title 该选项的描述标题。
     * @param options 可供选择的字符串选项列表。
     * @param getValue 异步获取当前选中项文本的挂起函数。
     * @param saveValue 用户进行选择后，异步保存所选项文本的挂起函数。
     * @return 返回创建好的 [SingleChoiceItem] 实例。
     */
    fun addSingleChoice(
        id: String,
        title: String,
        options: List<String>,
        getValue: suspend () -> String,
        saveValue: suspend (String) -> Unit
    ): SingleChoiceItem = SingleChoiceItem(id, title, options, getValue, saveValue).also { items.add(it) }

    /**
     * 添加多选项（复选框组形式）。
     * 适用于需要从预设选项中挑选零个或多个的场景（如特性开关、偏好标签等）。
     * 任何勾选状态的改变都会立即触发 [saveValue]。
     *
     * @param id 组件的唯一标识符。
     * @param title 多选项组的总体描述标题。
     * @param options 可供选择的所有选项字符串列表。
     * @param getValue 异步获取当前已选中的选项集合的挂起函数。
     * @param saveValue 集合发生增减时，异步保存最新选中集合的挂起函数。
     * @return 返回创建好的 [MultiChoiceItem] 实例。
     */
    fun addMultiChoice(
        id: String,
        title: String,
        options: List<String>,
        getValue: suspend () -> Set<String>,
        saveValue: suspend (Set<String>) -> Unit
    ): MultiChoiceItem = MultiChoiceItem(id, title, options, getValue, saveValue).also { items.add(it) }
}