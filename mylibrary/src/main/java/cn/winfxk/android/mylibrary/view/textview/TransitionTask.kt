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
 * Created by IntelliJ IDEA
 * Author： Winfxk
 * Web: http://winfxk.com
 * Created Date: 2026/06/04 16:41
 */
package cn.winfxk.android.mylibrary.view.textview

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.Gravity
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import cn.winfxk.android.mylibrary.view.TextView
import kotlin.random.Random

/**
 * 过渡动画任务处理器
 */
internal class TransitionTask private constructor(
    private val textView: TextView,
    private val oldText: String,
    private val tPaint: TextPaint,
    private val oldLayout: StaticLayout,
    private val newLayout: StaticLayout,
    private val verticalOffset: Float
) {
    private var particleData = FloatArray(0)
    private var particleAlphas = IntArray(0)
    private var particleActive = BooleanArray(0)
    private var particleThresholds = FloatArray(0)
    private var particleCount = 0
    private val particlePaint = Paint().apply {
        strokeWidth = textView.config.particleSize
        strokeCap = Paint.Cap.ROUND
        color = textView.currentTextColor
    }
    var progress = 0f
        private set
    var isFinished = false
        private set
    private val gradientMatrix = Matrix()
    private val edgeGradient = LinearGradient(
        0f, 0f, 1f, 0f,
        intArrayOf(Color.TRANSPARENT, textView.currentTextColor),
        null,
        Shader.TileMode.CLAMP
    )

    companion object {
        fun build(textView: TextView, oldText: String, newText: String, tPaint: TextPaint): TransitionTask {
            val oldLayout = createStaticLayout(textView, oldText, tPaint)
            val newLayout = createStaticLayout(textView, newText, tPaint)
            val verticalOffset = calculateVerticalOffset(textView, oldLayout, newLayout)
            val task = TransitionTask(textView, oldText, tPaint, oldLayout, newLayout, verticalOffset)
            task.prepareParticlesAsynchronously()
            return task
        }

        @SuppressLint("RtlHardcoded")
        private fun createStaticLayout(textView: TextView, source: CharSequence, paint: TextPaint): StaticLayout {
            val availableWidth = (textView.width - textView.compoundPaddingLeft - textView.compoundPaddingRight).coerceAtLeast(1)
            val alignment = when (textView.gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.CENTER_HORIZONTAL -> Layout.Alignment.ALIGN_CENTER
                Gravity.RIGHT             -> Layout.Alignment.ALIGN_OPPOSITE
                else                      -> Layout.Alignment.ALIGN_NORMAL
            }
            return StaticLayout.Builder.obtain(source, 0, source.length, paint, availableWidth)
                .setAlignment(alignment)
                .setLineSpacing(textView.lineSpacingExtra, textView.lineSpacingMultiplier)
                .setIncludePad(textView.includeFontPadding)
                .build()
        }

        private fun calculateVerticalOffset(textView: TextView, oldL: StaticLayout, newL: StaticLayout): Float {
            val totalTextHeight = oldL.height.coerceAtLeast(newL.height)
            val availableHeight = textView.height - textView.paddingTop - textView.paddingBottom
            return when (textView.gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.CENTER_VERTICAL -> (availableHeight - totalTextHeight) / 2f
                Gravity.BOTTOM          -> (availableHeight - totalTextHeight).toFloat()
                else                    -> 0f
            } + textView.paddingTop
        }
    }

    /**
     * 批量提取并使用基础数组代替装箱集合
     */
    private fun prepareParticlesAsynchronously() {
        if (oldText.isEmpty()) return
        val width = textView.width.coerceAtLeast(1)
        val height = textView.height.coerceAtLeast(1)
        val bitmap = createBitmap(width, height)
        val tempCanvas = Canvas(bitmap)
        tempCanvas.translate(textView.compoundPaddingLeft.toFloat(), verticalOffset)
        oldLayout.draw(tempCanvas)
        val step = textView.config.samplingStep
        val totalLines = oldLayout.lineCount
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val maxPossibleParticles = (width / step + 1) * (height / step + 1)
        val tempParticleData = FloatArray(maxPossibleParticles * 4)
        val tempThresholds = FloatArray(maxPossibleParticles)
        var count = 0
        val driftSpeed = textView.config.driftSpeed
        for (line in 0 until totalLines) {
            val lineTop = oldLayout.getLineTop(line)
            val lineBottom = oldLayout.getLineBottom(line)
            val lineLeft = oldLayout.getLineLeft(line).toInt()
            val lineRight = oldLayout.getLineRight(line).toInt()
            val startY = (verticalOffset + lineTop).toInt().coerceIn(0, height - 1)
            val endY = (verticalOffset + lineBottom).toInt().coerceIn(0, height - 1)
            val startX = (textView.compoundPaddingLeft + lineLeft).coerceIn(0, width - 1)
            val endX = (textView.compoundPaddingLeft + lineRight).coerceIn(0, width - 1)
            val lineTotalWidth = (lineRight - lineLeft).coerceAtLeast(1)
            for (y in startY .. endY step step) {
                for (x in startX .. endX step step) {
                    val color = pixels[y * width + x]
                    if (Color.alpha(color) > 120) {
                        tempParticleData[count * 4] = x.toFloat()
                        tempParticleData[count * 4 + 1] = y.toFloat()
                        tempParticleData[count * 4 + 2] = Random.nextFloat() * 1.5f - 0.75f + driftSpeed
                        tempParticleData[count * 4 + 3] = Random.nextFloat() * - 2f - 0.5f
                        val xInLineProgress = (x - startX).toFloat() / lineTotalWidth
                        tempThresholds[count] = ((line.toFloat() + xInLineProgress) / totalLines) * 0.6f
                        count ++
                    }
                }
            }
        }
        particleCount = count
        particleData = tempParticleData.copyOf(count * 4)
        particleThresholds = tempThresholds.copyOf(count)
        particleAlphas = IntArray(count) { 255 }
        particleActive = BooleanArray(count) { false }
        bitmap.recycle()
    }

    fun update(p: Float) {
        progress = p
        var allDead = true
        val fadeSpeed = textView.config.fadeSpeed
        @Suppress("EmptyRange")
        for (i in 0 until particleCount) {
            if (! particleActive[i] && progress > particleThresholds[i]) particleActive[i] = true
            if (particleActive[i]) {
                val baseIdx = i * 4
                particleData[baseIdx] += particleData[baseIdx + 2]
                particleData[baseIdx + 1] += particleData[baseIdx + 3]
                val currentAlpha = particleAlphas[i]
                if (currentAlpha > 0) {
                    particleAlphas[i] = (currentAlpha - fadeSpeed).coerceAtLeast(0)
                    allDead = false
                }
            } else allDead = false
        }
        if (progress >= 1f && (particleCount == 0 || allDead)) isFinished = true
    }

    fun draw(canvas: Canvas) {
        val widthFloat = textView.width.toFloat()
        val paddingLeftFloat = textView.compoundPaddingLeft.toFloat()
        for (line in 0 until oldLayout.lineCount) {
            val lineStartP = (line.toFloat() / oldLayout.lineCount) * 0.6f
            val lineEndP = ((line + 1).toFloat() / oldLayout.lineCount) * 0.6f
            val lineProgress = ((progress - lineStartP) / (lineEndP - lineStartP)).coerceIn(0f, 1.2f)
            val lineLeft = oldLayout.getLineLeft(line)
            val lineRight = oldLayout.getLineRight(line)
            val lineWidth = lineRight - lineLeft
            val scanLineX = paddingLeftFloat + lineLeft + (lineWidth * lineProgress)
            canvas.withSave {
                val lineClipTop = verticalOffset + oldLayout.getLineTop(line)
                val lineClipBottom = verticalOffset + oldLayout.getLineBottom(line)
                val edge = lineWidth * 0.2f
                gradientMatrix.setScale(edge.coerceAtLeast(1f), 1f)
                gradientMatrix.postTranslate(scanLineX - edge, 0f)
                edgeGradient.setLocalMatrix(gradientMatrix)
                tPaint.shader = edgeGradient
                tPaint.alpha = 255
                clipRect(0f, lineClipTop, widthFloat, lineClipBottom)
                translate(paddingLeftFloat, verticalOffset)
                oldLayout.draw(this)
                tPaint.shader = null
            }
        }
        @Suppress("EmptyRange")
        for (i in 0 ..< particleCount) if (particleActive[i] && particleAlphas[i] > 0) {
            particlePaint.alpha = particleAlphas[i]
            canvas.drawPoint(particleData[i * 4], particleData[i * 4 + 1], particlePaint)
        }
        val revealStart = 0.4f
        if (progress > revealStart) {
            val totalRevealP = ((progress - revealStart) / (1f - revealStart)).coerceIn(0f, 1f)
            canvas.withTranslation(paddingLeftFloat, verticalOffset) {
                for (line in 0 until newLayout.lineCount) {
                    val lineRevealStart = line.toFloat() / newLayout.lineCount * 0.5f
                    val lineAlpha = ((totalRevealP - lineRevealStart) / 0.5f).coerceIn(0f, 1f)
                    tPaint.alpha = (lineAlpha * 255).toInt()
                    withClip(0, newLayout.getLineTop(line), textView.width, newLayout.getLineBottom(line)) {
                        newLayout.draw(this)
                    }
                }
            }
        }
    }
}