package com.arnyminerz.filmagentaproto.documents

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.local.WooCommerceDao
import java.io.FileOutputStream
import kotlin.math.min

object Ticket {
    private const val PAGE_WIDTH = DocumentUtils.DIN_A4_WIDTH
    private const val PAGE_HEIGHT = DocumentUtils.DIN_A4_HEIGHT

    private const val RULE_X = 21.0
    private const val RULE_X_LEFT = RULE_X
    private const val RULE_X_RIGHT = PAGE_WIDTH - RULE_X

    private const val RULE_Y = 29.7
    private const val RULE_Y_TOP = RULE_Y
    private const val RULE_Y_BOTTOM = PAGE_HEIGHT - RULE_Y

    /** Amount of tickets in the page horizontally */
    private const val TICKETS_HORIZONTAL = 2

    private const val TICKET_WIDTH = (RULE_X_RIGHT - RULE_X_LEFT) / TICKETS_HORIZONTAL
    private const val TICKET_HEIGHT = 35.0

    data class TicketData(
        val qr: Bitmap
    ) {
        companion object {
            suspend fun fromOrders(
                wooCommerceDao: WooCommerceDao,
                orders: List<Order>
            ): List<TicketData> =
                orders.map { order ->
                    val customer = wooCommerceDao.getCustomer(order.customerId)!!
                    val qr = order.getQRCode(customer)

                    TicketData(qr)
                }
        }
    }

    context(Canvas) private fun TicketData.draw(x: Millimeter, y: Millimeter) {
        val strokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.BLACK
        }

        // Draw the borders
        drawRect(x, y, TICKET_WIDTH.mm, TICKET_HEIGHT.mm, strokePaint)
    }

    private fun newPage(
        document: PdfDocument,
        pageNumber: Int,
        pageWidth: Int,
        pageHeight: Int,
        allTickets: List<TicketData>,
        ticketsPerPage: Int,
    ): Boolean {
        val title = Paint().apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 15f
        }

        val index = ticketsPerPage * (pageNumber - 1)
        val end = min(index + ticketsPerPage, allTickets.size)
        val tickets = allTickets.subList(index, end)

        val pageInfo = PdfDocument.PageInfo.Builder(
            pageWidth, pageHeight, pageNumber,
        ).create()
        val page = document.startPage(pageInfo)

        with(page) {
            canvas.apply {
                val groups = tickets.chunked(TICKETS_HORIZONTAL)
                for ((yIndex, group) in groups.withIndex())
                    for ((xIndex, ticket) in group.withIndex())
                        ticket.draw(
                            (RULE_X_LEFT + xIndex * TICKET_WIDTH).mm,
                            (RULE_Y_TOP + yIndex * TICKET_HEIGHT).mm,
                        )

                drawText("This is some testing text", 50f, 50f, title)
            }
        }

        document.finishPage(page)

        // If end's length is equal to allTickets.size, it means that we have reached the end
        return end != allTickets.size
    }

    fun generatePDF(tickets: List<TicketData>, target: FileOutputStream) {
        val document = PdfDocument()

        val pageWidth = PAGE_WIDTH.mm.psPoints.toInt()
        val pageHeight = PAGE_HEIGHT.mm.psPoints.toInt()

        val ticketsVertical = ((pageHeight - 2 * RULE_Y) / TICKET_HEIGHT - 1).toInt()
        val ticketsPerPage = TICKETS_HORIZONTAL * ticketsVertical

        var pageCounter = 1
        while (newPage(document, pageCounter, pageWidth, pageHeight, tickets, ticketsPerPage)) {
            pageCounter++
        }

        document.writeTo(target)
    }
}