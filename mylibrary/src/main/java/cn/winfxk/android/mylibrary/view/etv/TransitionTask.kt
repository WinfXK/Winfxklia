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
* Created PCUser: Winfx 
* Web: http://winfxk.com
* Created Date: 2025/12/23  09:32 */
package cn.winfxk.android.mylibrary.view.etv

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.Gravity
import kotlin.random.Random
import kotlin.ranges.step

class TransitionTask(val textView: EffectTextView, val oldText: String, newText: String, val tPaint: TextPaint) {
    var progress = 0f
    var isFinished = false
    private var particleData = FloatArray(0)
    private var particleAlphas = IntArray(0)
    private var particleActive = BooleanArray(0)
    private var particleThresholds = FloatArray(0) // 每个粒子独立的激活阈值
    private var particleCount = 0
    private val oldLayout: StaticLayout = createStaticLayout(oldText)
    private val newLayout: StaticLayout = createStaticLayout(newText)

    // 计算垂直居中偏移量
    private val verticalOffset: Float by lazy {
        val totalTextHeight = oldLayout.height.coerceAtLeast(newLayout.height)
        val availableHeight = textView.height - textView.paddingTop - textView.paddingBottom
        when (textView.gravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.CENTER_VERTICAL -> (availableHeight - totalTextHeight) / 2f
            Gravity.BOTTOM          -> (availableHeight - totalTextHeight).toFloat()
            else                    -> 0f
        } + textView.paddingTop
    }

    private fun createStaticLayout(source: CharSequence): StaticLayout {
        val availableWidth = textView.width - textView.compoundPaddingLeft - textView.compoundPaddingRight
        val alignment = when (textView.gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.CENTER_HORIZONTAL -> Layout.Alignment.ALIGN_CENTER
            Gravity.RIGHT             -> Layout.Alignment.ALIGN_OPPOSITE
            else                      -> Layout.Alignment.ALIGN_NORMAL
        }
        return StaticLayout.Builder.obtain(source, 0, source.length, tPaint, availableWidth.coerceAtLeast(1))
            .setAlignment(alignment)
            .setLineSpacing(textView.lineSpacingExtra, textView.lineSpacingMultiplier)
            .setIncludePad(textView.includeFontPadding)
            .build()
    }

    init {
        prepareParticles()
    }

    private fun prepareParticles() {
        if (oldText.isEmpty()) return
        val bitmap = Bitmap.createBitmap(textView.width.coerceAtLeast(1), textView.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(bitmap)
        // 应用居中偏移进行采样绘制
        tempCanvas.translate(textView.compoundPaddingLeft.toFloat(), verticalOffset)
        oldLayout.draw(tempCanvas)

        val rawList = mutableListOf<Float>()
        val thresholds = mutableListOf<Float>()
        val step = textView.config.samplingStep

        val totalLines = oldLayout.lineCount

        for (line in 0 until totalLines) {
            val lineTop = oldLayout.getLineTop(line)
            val lineBottom = oldLayout.getLineBottom(line)
            val lineLeft = oldLayout.getLineLeft(line).toInt()
            val lineRight = oldLayout.getLineRight(line).toInt()

            // 遍历当前行的像素区域
            for (y in (verticalOffset + lineTop).toInt() .. (verticalOffset + lineBottom).toInt() step step) {
                for (x in (textView.compoundPaddingLeft + lineLeft) .. (textView.compoundPaddingLeft + lineRight) step step) {
                    val safeX = x.coerceIn(0, bitmap.width - 1)
                    val safeY = y.coerceIn(0, bitmap.height - 1)
                    val color = bitmap.getPixel(safeX, safeY)

                    if (Color.alpha(color) > 120) {
                        rawList.add(safeX.toFloat())
                        rawList.add(safeY.toFloat())
                        rawList.add(Random.nextFloat() * 1.5f - 0.75f + textView.config.driftSpeed)
                        rawList.add(Random.nextFloat() * - 2f - 0.5f)

                        // 核心：计算该粒子的激活阈值。阅读序逻辑：(行进度 + 行内进度)
                        val xInLineProgress = (x - (textView.compoundPaddingLeft + lineLeft)).toFloat() / (lineRight - lineLeft).coerceAtLeast(1)
                        // 这里的 0.6 是指旧文字在总进度的 60% 内完成消融
                        val threshold = ((line.toFloat() + xInLineProgress) / totalLines) * 0.6f
                        thresholds.add(threshold)
                    }
                }
            }
        }

        particleCount = thresholds.size
        particleData = rawList.toFloatArray()
        particleThresholds = thresholds.toFloatArray()
        particleAlphas = IntArray(particleCount) { 255 }
        particleActive = BooleanArray(particleCount) { false }
        bitmap.recycle()
    }

    fun update(p: Float) {
        progress = p
        for (i in 0 until particleCount) {
            // 根据粒子独立的阈值激活
            if (! particleActive[i] && progress > particleThresholds[i]) {
                particleActive[i] = true
            }
            if (particleActive[i]) {
                particleData[i * 4] += particleData[i * 4 + 2]
                particleData[i * 4 + 1] += particleData[i * 4 + 3]
                particleAlphas[i] = (particleAlphas[i] - textView.config.fadeSpeed).coerceAtLeast(0)
            }
        }
        if (progress >= 1f && (particleCount == 0 || particleAlphas.all { it <= 0 })) isFinished = true
    }

    fun draw(canvas: Canvas) {
        // 1. 绘制旧文本 (按行序列化消融)
        for (line in 0 until oldLayout.lineCount) {
            val lineStartP = (line.toFloat() / oldLayout.lineCount) * 0.6f
            val lineEndP = ((line + 1).toFloat() / oldLayout.lineCount) * 0.6f

            // 计算当前行的局部扫描进度
            val lineProgress = ((progress - lineStartP) / (lineEndP - lineStartP)).coerceIn(0f, 1.2f)
            val lineLeft = oldLayout.getLineLeft(line)
            val lineRight = oldLayout.getLineRight(line)
            val lineWidth = lineRight - lineLeft
            val scanLineX = textView.compoundPaddingLeft + lineLeft + (lineWidth * lineProgress)

            canvas.save()
            // 只剪裁当前行区域
            val lineClipTop = verticalOffset + oldLayout.getLineTop(line)
            val lineClipBottom = verticalOffset + oldLayout.getLineBottom(line)
            // 消融边缘梯度效果
            val edge = lineWidth * 0.2f
            val shader = LinearGradient(scanLineX - edge, 0f, scanLineX, 0f,
                Color.TRANSPARENT, textView.currentTextColor, Shader.TileMode.CLAMP)

            tPaint.shader = shader
            tPaint.alpha = 255
            canvas.clipRect(0f, lineClipTop, textView.width.toFloat(), lineClipBottom)
            canvas.translate(textView.compoundPaddingLeft.toFloat(), verticalOffset)
            oldLayout.draw(canvas)
            tPaint.shader = null
            canvas.restore()
        }

        // 2. 绘制粒子
        val pPaint = Paint().apply {
            strokeWidth = textView.config.particleSize
            strokeCap = Paint.Cap.ROUND
            color = textView.currentTextColor
        }
        for (i in 0 until particleCount) {
            if (particleActive[i] && particleAlphas[i] > 0) {
                pPaint.alpha = particleAlphas[i]
                canvas.drawPoint(particleData[i * 4], particleData[i * 4 + 1], pPaint)
            }
        }

        // 3. 绘制新文本 (按行序列化渐显)
        // 新文本在 0.4 进度后开始，与旧文本形成交替感
        val revealStart = 0.4f
        if (progress > revealStart) {
            val totalRevealP = ((progress - revealStart) / (1f - revealStart)).coerceIn(0f, 1f)
            canvas.save()
            canvas.translate(textView.compoundPaddingLeft.toFloat(), verticalOffset)

            for (line in 0 until newLayout.lineCount) {
                // 每行独立的渐显阈值
                val lineRevealStart = line.toFloat() / newLayout.lineCount * 0.5f
                val lineAlpha = ((totalRevealP - lineRevealStart) / 0.5f).coerceIn(0f, 1f)

                tPaint.alpha = (lineAlpha * 255).toInt()
                // 局部裁剪绘制当前行
                canvas.save()
                canvas.clipRect(0, newLayout.getLineTop(line), textView.width, newLayout.getLineBottom(line))
                newLayout.draw(canvas)
                canvas.restore()
            }
            canvas.restore()
        }
    }
}