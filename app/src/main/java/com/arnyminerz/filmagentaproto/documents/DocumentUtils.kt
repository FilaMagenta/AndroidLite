package com.arnyminerz.filmagentaproto.documents

import android.graphics.Canvas
import android.graphics.Paint

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
    fun toPostscriptPoints() = DocumentUtils.mmToPostscript(value)

    /**
     * Alias for [toPostscriptPoints]
     */
    val psPoints: Double
        get() = toPostscriptPoints()

    override fun toString(): String = "${value}mm"
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
