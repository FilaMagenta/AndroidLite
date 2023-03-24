package com.arnyminerz.filmagentaproto.documents

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.icu.text.SimpleDateFormat
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.local.WooCommerceDao
import java.io.FileOutputStream
import java.util.Locale
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
        val qr: Bitmap,
        val customerName: String,
        val eventName: String,
        val eventDate: String?,
    ) {
        companion object {
            private val simpleDateFormat: SimpleDateFormat
                get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            suspend fun fromOrders(
                wooCommerceDao: WooCommerceDao,
                event: Event,
                orders: List<Order>,
                onProgress: suspend (current: Int, max: Int) -> Unit = { _, _ -> },
            ): List<TicketData> =
                orders.mapIndexed { index, order ->
                    onProgress(index, orders.size)

                    val customer = wooCommerceDao.getCustomer(order.customerId)!!

                    val qr = order.getQRCode(customer)
                    val customerName = customer.firstName + " " + customer.lastName
                    val eventName = event.name
                    val eventDate = event.eventDate?.let { simpleDateFormat.format(it) }

                    TicketData(qr, customerName, eventName, eventDate)
                }
        }
    }

    context(Canvas, Context) private fun TicketData.draw(x: Millimeter, y: Millimeter) {
        // Prepare paints
        val strokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.BLACK
        }
        val title = TextPaint().apply {
            color = Color.BLACK
            typeface = ResourcesCompat.getFont(this@Context, R.font.roboto_condensed_regular)
            textSize = 12f
        }
        val subtitle = TextPaint().apply {
            color = Color.BLACK
            typeface = ResourcesCompat.getFont(this@Context, R.font.roboto_condensed_light)
            textSize = 12f
        }

        // Draw the QR code
        drawBitmap(qr, x, y, TICKET_HEIGHT.mm, TICKET_HEIGHT.mm)

        // Draw the borders
        drawRect(x, y, TICKET_WIDTH.mm, TICKET_HEIGHT.mm, strokePaint)

        val textWidth = (TICKET_WIDTH - TICKET_HEIGHT).mm
        var textY = y + 2.mm

        // Draw the customer name
        val nameHeight = drawText(
            customerName,
            x + TICKET_HEIGHT.mm,
            textY,
            textWidth,
            title,
        )

        // Draw the event name
        val eventNameHeight = drawText(
            eventName,
            x + TICKET_HEIGHT.mm,
            (textY + Millimeter.fromPx(nameHeight)).also { textY = it },
            textWidth,
            subtitle,
        )

        // Draw the event date
        if (eventDate != null)
            drawText(
                eventDate,
                x + TICKET_HEIGHT.mm,
                (textY + Millimeter.fromPx(eventNameHeight)).also { textY = it },
                textWidth,
                subtitle,
            )
    }

    context (Context)
            private suspend fun newPage(
        document: PdfDocument,
        pageNumber: Int,
        pageWidth: Int,
        pageHeight: Int,
        allTickets: List<TicketData>,
        ticketsPerPage: Int,
        onProgress: suspend (current: Int, max: Int) -> Unit,
    ): Boolean {
        val index = ticketsPerPage * (pageNumber - 1)
        val end = min(index + ticketsPerPage, allTickets.size)
        val tickets = allTickets.subList(index, end)

        val pageInfo = PdfDocument.PageInfo.Builder(
            pageWidth, pageHeight, pageNumber,
        ).create()
        val page = document.startPage(pageInfo)

        with(page) {
            canvas.apply {
                var counter = 0
                val groups = tickets.chunked(TICKETS_HORIZONTAL)
                for ((yIndex, group) in groups.withIndex())
                    for ((xIndex, ticket) in group.withIndex()) {
                        onProgress(ticketsPerPage * pageNumber + (counter++), allTickets.size)
                        ticket.draw(
                            (RULE_X_LEFT + xIndex * TICKET_WIDTH).mm,
                            (RULE_Y_TOP + yIndex * TICKET_HEIGHT).mm,
                        )
                    }
            }
        }

        document.finishPage(page)

        // If end's length is equal to allTickets.size, it means that we have reached the end
        return end != allTickets.size
    }

    context (Context)
            suspend fun generatePDF(
        tickets: List<TicketData>,
        target: FileOutputStream,
        onProgress: suspend (current: Int, max: Int) -> Unit
    ) {
        val document = PdfDocument()

        val pageWidth = PAGE_WIDTH.mm.psPoints.toInt()
        val pageHeight = PAGE_HEIGHT.mm.psPoints.toInt()

        val ticketHeight = TICKET_HEIGHT.mm.psPoints
        val verticalPadding = RULE_Y.mm.psPoints
        val ticketsVertical = ((pageHeight - 2 * verticalPadding) / ticketHeight).toInt()
        val ticketsPerPage = TICKETS_HORIZONTAL * ticketsVertical

        var pageCounter = 1
        while (newPage(
                document,
                pageCounter,
                pageWidth,
                pageHeight,
                tickets,
                ticketsPerPage,
                onProgress
            )
        ) {
            pageCounter++
        }

        document.writeTo(target)
    }
}