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
* Created Date: 2026/6/4  09:21 */
package cn.winfxk.android.mylibrary.view.textview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import cn.winfxk.android.mylibrary.view.TextView

internal class AnimatorAdapter(
    private val main: TextView,
    private val animator: ValueAnimator,
    private val task: TransitionTask) : AnimatorListenerAdapter() {
    override fun onAnimationEnd(animation: Animator) {
        main.animators.remove(animator)
        main.activeTasks.remove(task)
        if (main.activeTasks.isEmpty()) {
            main.text = main.lastTargetText
            main.onEffectFinishedListener?.invoke(main)
        }
    }
}