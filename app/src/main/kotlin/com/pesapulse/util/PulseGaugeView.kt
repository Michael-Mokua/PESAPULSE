package com.pesapulse.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class PulseGaugeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var score = 0
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 40f
        strokeCap = Paint.Cap.ROUND
    }

    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 100f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    fun setScore(value: Int) {
        score = value.coerceIn(0, 1000)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val radius = (width.coerceAtMost(height) / 2) - 60f
        val centerX = width / 2
        val centerY = height / 2 + 50f

        val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        // Draw Arc Background
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        arcPaint.color = Color.parseColor("#33FFFFFF")
        arcPaint.clearShadowLayer()
        canvas.drawArc(rect, 150f, 240f, false, arcPaint)

        // Draw Progress Arc with Glow
        val sweepAngle = (score / 1000f) * 240f
        val gradient = SweepGradient(centerX, centerY, intArrayOf(Color.RED, Color.YELLOW, Color.GREEN), floatArrayOf(0f, 0.5f, 1f))
        val matrix = Matrix()
        matrix.postRotate(150f, centerX, centerY)
        gradient.setLocalMatrix(matrix)
        arcPaint.shader = gradient
        arcPaint.setShadowLayer(20f, 0f, 0f, Color.GREEN)
        canvas.drawArc(rect, 150f, sweepAngle, false, arcPaint)
        arcPaint.shader = null
        arcPaint.clearShadowLayer()

        // Draw Score Text
        canvas.drawText(score.toString(), centerX, centerY, textPaint)
        canvas.drawText("PULSE SCORE", centerX, centerY + 60f, labelPaint)

        // Draw Needle with Glow
        val angle = (150 + sweepAngle).toDouble()
        val radian = Math.toRadians(angle)
        val endX = centerX + (radius - 20) * cos(radian).toFloat()
        val endY = centerY + (radius - 20) * sin(radian).toFloat()
        needlePaint.setShadowLayer(15f, 0f, 0f, Color.WHITE)
        canvas.drawLine(centerX, centerY, endX, endY, needlePaint)
        needlePaint.clearShadowLayer()
    }
}
