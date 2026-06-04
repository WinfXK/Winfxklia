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
* Created Date: 2026/6/4  09:18 */
package cn.winfxk.android.mylibrary.view.textview

/**
 * 粒子特效配置类
 */
data class ParticleConfig(
    var particleSize: Float = 6f,
    var samplingStep: Int = 4,
    var duration: Long = 4500,
    var driftSpeed: Float = 0.8f,
    var fadeSpeed: Int = 3,
    var sequenceSmoothness: Float = 0.1f
)