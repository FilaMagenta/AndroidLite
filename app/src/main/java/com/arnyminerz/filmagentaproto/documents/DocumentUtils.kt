package com.arnyminerz.filmagentaproto.documents

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
    fun mmToPostscript(size: Int) = size * MM_IN_POSTSCRIPT
}

data class Millimeter(
    val value: Int
) {
    fun toPostscriptPoints(): Int = DocumentUtils.mmToPostscript(value).toInt()
}

val Int.mm: Millimeter
    get() = Millimeter(this)
