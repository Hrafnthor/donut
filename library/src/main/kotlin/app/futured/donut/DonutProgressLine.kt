package app.futured.donut

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.ceil

internal class DonutProgressLine(
    val name: String,
    icon: Drawable?,
    radius: Float,
    lineColor: Int,
    lineStrokeWidth: Float,
    lineStrokeCap: DonutStrokeCap,
    masterProgress: Float,
    length: Float,
    gapWidthDegrees: Float,
    gapAngleDegrees: Float,
    direction: DonutDirection
) {

    companion object {
        const val SIDES = 64
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = lineStrokeCap.cap
        strokeWidth = mLineStrokeWidth
        color = mLineColor
    }

    var mRadius: Float = 0.0f
        set(value) {
            field = value
            updatePath()
            updatePathEffect()
        }

    var mLineColor: Int = 0
        set(value) {
            field = value
            paint.color = value
        }

    var mLineStrokeWidth: Float = 0.0f
        set(value) {
            field = value
            paint.strokeWidth = value
            iconSize = (value * 0.6).toInt()
        }

    var mLineStrokeCap: DonutStrokeCap = DonutStrokeCap.ROUND
        set(value) {
            field = value
            paint.strokeCap = value.cap
        }

    var mMasterProgress: Float = 0.0f
        set(value) {
            field = value
            updatePathEffect()
        }

    var mLength: Float = 0.0f
        set(value) {
            field = value
            updatePathEffect()
        }

    var mGapWidthDegrees = 10f
        set(value) {
            field = value
            updatePath()
            updatePathEffect()
        }

    var mGapAngleDegrees = 270f
        set(value) {
            field = value
            updatePath()
            updatePathEffect()
        }

    var mDirection = DonutDirection.CLOCKWISE
        set(value) {
            field = value
            updatePath()
            updatePathEffect()
        }

    var mIcon: Drawable? = null
        set(value) {
            field = value
            updateIcon()
        }

    private var drawnLength: Float = 0.0f
    private var path: Path = createPath()
    private var iconSize: Int = 0
        set(value) {
            field = value
            updateIcon()
        }

    private var iconBitmap: Bitmap? = null
    private var iconPosition: PointF = PointF(0.0f, 0.0f)

    init {
        this.mRadius = radius
        this.mLineColor = lineColor
        this.mLineStrokeWidth = lineStrokeWidth
        this.mLineStrokeCap = lineStrokeCap
        this.mMasterProgress = masterProgress
        this.mLength = length
        this.mGapWidthDegrees = gapWidthDegrees
        this.mGapAngleDegrees = gapAngleDegrees
        this.mDirection = direction
        this.mIcon = icon
    }

    private fun createPath(): Path {
        val path = Path()

        val offset = mGapAngleDegrees.toRadians()

        val startAngle = when (mDirection) {
            DonutDirection.CLOCKWISE -> 0.0 + (mGapWidthDegrees / 2f).toRadians()
            DonutDirection.ANTICLOCKWISE -> Math.PI * 2.0 - (mGapWidthDegrees / 2f).toRadians()
        }
        val endAngle = when (mDirection) {
            DonutDirection.CLOCKWISE -> Math.PI * 2.0 - (mGapWidthDegrees / 2f).toRadians()
            DonutDirection.ANTICLOCKWISE -> 0.0 + (mGapWidthDegrees / 2f).toRadians()
        }
        val angleStep = (endAngle - startAngle) / SIDES

        path.moveTo(
            mRadius * Math.cos(startAngle + offset).toFloat(),
            mRadius * Math.sin(startAngle + offset).toFloat()
        )

        for (i in 1 until SIDES + 1) {
            path.lineTo(
                mRadius * Math.cos(i * angleStep + offset + startAngle).toFloat(),
                mRadius * Math.sin(i * angleStep + offset + startAngle).toFloat()
            )
        }

        return path
    }

    private fun updatePath() {
        this.path = createPath()
    }

    private fun updatePathEffect() {
        val pathLen = PathMeasure(path, false).length
        drawnLength = ceil(pathLen.toDouble() * mLength * mMasterProgress).toFloat()

        paint.pathEffect = ComposePathEffect(
            CornerPathEffect(pathLen / SIDES),
            DashPathEffect(
                floatArrayOf(
                    drawnLength,
                    pathLen - drawnLength
                ),
                0f
            )
        )
    }

    private fun updateIcon() {
        iconBitmap = mIcon?.toBitmap(iconSize, iconSize)
    }

    fun getDrawnLength(): Float = drawnLength

    fun updateIconLocation(obscuredPathLength: Float) {
        val pos = FloatArray(2)
        val tan = FloatArray(2)
        val measure = PathMeasure(path, false)
        if (measure.getPosTan((drawnLength - obscuredPathLength) / 2 + obscuredPathLength, pos, tan)) {
            iconPosition = PointF(pos[0] - iconSize / 2, pos[1] - iconSize / 2)
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)

        iconBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, iconPosition.x, iconPosition.y, null)
        }
    }

    private fun Float.toRadians() = Math.toRadians(this.toDouble())
}
