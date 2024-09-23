/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/5/21  上午10:43*/
package com.winfxk.winfxklia.dialog;

import com.winfxk.winfxklia.R;

public enum Type {
    /**
     * 设置自定义图标时使用，
     */
    Image(R.drawable.winfxkliba_img),
    /**
     * 不需要图标时使用
     */
    Empty(R.drawable.winfxkliba_empty),
    /**
     * 普通提示类型(默认)
     */
    INFO(R.drawable.winfxkliba_info),
    /**
     * 警告信息
     */
    WARNING(R.drawable.winfxkliba_warn),
    /**
     * 错误信息
     */
    ERROR(R.drawable.winfxkliba_error),
    /**
     * 成功信息
     */
    SUCCESS(R.drawable.winfxkliba_succeed),
    /**
     * 提问信息(重要)
     */
    ASK(R.drawable.winfxkliba_ask),
    /**
     * 失败信息
     */
    Fail(R.drawable.winfxkliba_fail),
    /**
     * 禁用状态(手绘)
     */
    NG(R.drawable.winfxkliba_ng),
    /**
     * 确认信息(不重要)
     */
    Confirm(R.drawable.winfxkliba_confirm),
    /**
     * 确认选择信息
     */
    Select(R.drawable.winfxkliba_select),
    /**
     * 嘉奖信息
     */
    Cite(R.drawable.winfxkliba_cite),
    /**
     * 注意信息
     */
    Caution(R.drawable.winfxkliba_caution),
    /**
     * 指导信息
     */
    Tip(R.drawable.winfxkliba_info2),
    /**
     * 提示输入类容
     */
    Input(R.drawable.winfxkliba_input),
    /**
     * 等待信息（显示Progress动画)
     */
    Progress(R.drawable.winfxkliba_loading),
    /**
     * 等待信息（不显示动画）
     */
    Loading(R.drawable.winfxkliba_loading);

    private final int icon;

    Type(int icon) {
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    /**
     * 继承于Progress
     */
    public static final Type Parload = Progress;
}
