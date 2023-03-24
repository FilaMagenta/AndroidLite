package com.arnyminerz.filmagentaproto.documents

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.FileOutputStream

object Ticket {
    fun generatePDF(target: FileOutputStream) {
        val document = PdfDocument()

        val paint = Paint()
        val title = Paint().apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 15f
        }

        val height = DocumentUtils.DIN_A4_HEIGHT
        val width = DocumentUtils.DIN_A4_WIDTH
        val pageInfo = PdfDocument.PageInfo.Builder(
            width.mm.toPostscriptPoints(), height.mm.toPostscriptPoints(), 1
        ).create()
        val page = document.startPage(pageInfo)

        page.canvas.apply {
            drawText("This is some testing text", 50f, 50f, title)
        }

        document.finishPage(page)

        document.writeTo(target)
    }
}