package com.arnyminerz.filmagentaproto.documents

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import androidx.core.graphics.toRect
import androidx.core.graphics.withTranslation

object DocumentUtils {
    /**
     * The equivalence between 1 mm and Postscript points.
     * @see <a href="http://www.unitconversion.org/typography/postscript-points-to-millimeters-conversion.html">Reference</a>
     */
    private const val MM_IN_POSTSCRIPT = 2.834645669

    /** The height of a DIN-A4 in mm */
    const val DIN_A4_HEIGHT = 297

    /** The width of a DIN-A4 in mm */
    const val DIN_A4_WIDTH = 210

    /**
     * Converts the given distance into Postscript points.
     * @param size The amount of millimeters.
     */
    fun mmToPostscript(size: Double) = size * MM_IN_POSTSCRIPT
}

data class Millimeter(
    val value: Double
) {
    constructor(value: Float): this(value.toDouble())

    companion object {
        context(Context)
        fun fromPx(value: Int): Millimeter {
            val dm = resources.displayMetrics
            val converted = value / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, dm)
            return Millimeter(converted)
        }
    }

    fun toPostscriptPoints() = DocumentUtils.mmToPostscript(value)

    /**
     * Alias for [toPostscriptPoints]
     */
    val psPoints: Double
        get() = toPostscriptPoints()

    context (Context)
    val px: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_MM, value.toFloat(),
            resources.displayMetrics
        )

    override fun toString(): String = "${value}mm"

    operator fun plus(other: Millimeter): Millimeter = Millimeter(value + other.value)
    operator fun minus(other: Millimeter): Millimeter = Millimeter(value - other.value)
    operator fun div(other: Int): Millimeter = Millimeter(value / other)
    operator fun times(other: Int): Millimeter = Millimeter(value * other)
}

open class RectD(left: Double, top: Double, right: Double, bottom: Double) :
    RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat()) {
    class Size(width: Double, height: Double) : RectD(0.0, 0.0, width, height) {
        constructor(width: Int, height: Int) : this(width.toDouble(), height.toDouble())
    }
}

val Double.mm: Millimeter
    get() = Millimeter(this)

val Int.mm: Millimeter
    get() = Millimeter(toDouble())

fun Canvas.drawRect(
    x: Millimeter,
    y: Millimeter,
    width: Millimeter,
    height: Millimeter,
    paint: Paint
) {
    val left = x.psPoints.toFloat()
    val top = y.psPoints.toFloat()
    val right = (x.psPoints + width.psPoints).toFloat()
    val bottom = (y.psPoints + height.psPoints).toFloat()

    return drawRect(left, top, right, bottom, paint)
}

fun Canvas.drawBitmap(
    bitmap: Bitmap,
    x: Millimeter,
    y: Millimeter,
    width: Millimeter,
    height: Millimeter,
) {
    val srcRect = RectD.Size(bitmap.width, bitmap.height)
    val targetRect = RectD(
        x.psPoints,
        y.psPoints,
        (x + width).psPoints,
        (y + height).psPoints
    )
    val paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
    }

    drawBitmap(bitmap, srcRect.toRect(), targetRect, paint)
}

context (Context)
fun Canvas.drawText(
    text: String,
    x: Millimeter,
    y: Millimeter,
    width: Millimeter,
    paint: TextPaint
): Int {
    val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width.psPoints.toInt())
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setIncludePad(false)
        .build()

    withTranslation(x.px, y.px) {
        layout.draw(this)
    }

    return layout.height
}
