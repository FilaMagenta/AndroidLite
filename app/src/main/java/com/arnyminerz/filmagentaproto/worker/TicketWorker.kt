package com.arnyminerz.filmagentaproto.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextPaint
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.local.WooCommerceDao
import com.arnyminerz.filmagentaproto.database.logic.getQRCode
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializable
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializer
import com.arnyminerz.filmagentaproto.documents.DocumentUtils
import com.arnyminerz.filmagentaproto.documents.Millimeter
import com.arnyminerz.filmagentaproto.documents.drawBitmap
import com.arnyminerz.filmagentaproto.documents.drawRect
import com.arnyminerz.filmagentaproto.documents.drawText
import com.arnyminerz.filmagentaproto.documents.mm
import com.arnyminerz.filmagentaproto.utils.decodeBitmapBase64
import com.arnyminerz.filmagentaproto.utils.encodeBase64
import com.arnyminerz.filmagentaproto.utils.getStringOrNull
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.min
import org.json.JSONObject

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

private val LOGO_SIZE = 28.mm

class TicketWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context.applicationContext, params) {

    private object Transfer {
        @Volatile
        var tickets: List<TicketData>? = null
    }

    companion object {
        const val TAG = "TicketWorker"

        private const val INPUT_TARGET = "target"

        const val RESULT_ERROR = "error"

        const val ERROR_MISSING_TARGET = "target"
        const val ERROR_MISSING_TICKETS = "tickets"
        const val ERROR_TARGET_NOT_FOUND = "not_found"

        const val PROGRESS_CURRENT = "current"
        const val PROGRESS_MAX = "max"

        fun generate(context: Context, tickets: List<TicketData>, target: Uri): Operation {
            Transfer.tickets = tickets
            val data = workDataOf(
                INPUT_TARGET to target.toString(),
            )
            val request = OneTimeWorkRequestBuilder<TicketWorker>()
                .setInputData(data)
                .addTag(TAG)
                .build()
            return WorkManager.getInstance(context).enqueue(request)
        }

        fun workerState(context: Context) = WorkManager.getInstance(context)
            .getWorkInfosByTagLiveData(TAG)

        private fun errorResult(error: String): Result =
            Result.failure(workDataOf(RESULT_ERROR to error))
    }

    data class TicketData(
        val qr: Bitmap,
        val customerName: String,
        val eventName: String,
        val eventDate: String?,
    ) : JsonSerializable {
        companion object : JsonSerializer<TicketData> {
            private val simpleDateFormat: SimpleDateFormat
                get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            context(Context)
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

            override fun fromJSON(json: JSONObject): TicketData = TicketData(
                json.getString("qr").decodeBitmapBase64(),
                json.getString("customer_name"),
                json.getString("event_name"),
                json.getStringOrNull("event_date"),
            )
        }

        override fun toJSON(): JSONObject = JSONObject().apply {
            put("qr", qr.encodeBase64())
            put("customer_name", customerName)
            put("event_name", eventName)
            put("event_date", eventDate)
        }
    }

    override suspend fun doWork(): Result {
        val target = inputData.getString(INPUT_TARGET)
            ?: return errorResult(ERROR_MISSING_TARGET)
        val uri = Uri.parse(target)

        val tickets = Transfer.tickets
            ?: return errorResult(ERROR_MISSING_TICKETS)

        val descriptor = applicationContext.contentResolver.openFileDescriptor(uri, "w")
            ?: return errorResult(ERROR_TARGET_NOT_FOUND)

        generatePDF(tickets, descriptor)

        return Result.success()
    }

    context(Canvas) private fun TicketData.draw(x: Millimeter, y: Millimeter) =
        with(applicationContext) {
            // Prepare paints
            val strokePaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.BLACK
            }
            val title = TextPaint().apply {
                color = Color.BLACK
                typeface = ResourcesCompat.getFont(this@with, R.font.roboto_condensed_regular)
                textSize = 12f
            }
            val subtitle = TextPaint().apply {
                color = Color.BLACK
                typeface = ResourcesCompat.getFont(this@with, R.font.roboto_condensed_light)
                textSize = 12f
            }

            // Calculate the text boundaries
            val textWidth = (TICKET_WIDTH - TICKET_HEIGHT).mm
            val textX = x + TICKET_HEIGHT.mm
            var textY = y + 2.mm

            // Draw the QR code
            drawBitmap(qr, x, y, TICKET_HEIGHT.mm, TICKET_HEIGHT.mm)

            // Draw the logo (inside of the text boundaries)
            ResourcesCompat.getDrawable(resources, R.drawable.logo_magenta_mono, theme)
                ?.let { logo ->
                    drawBitmap(
                        logo.toBitmap(),
                        textX + (textWidth / 2) - (LOGO_SIZE / 2),
                        y + (TICKET_HEIGHT / 2).mm - (LOGO_SIZE / 2),
                        LOGO_SIZE,
                        LOGO_SIZE,
                    )
                }

            // Draw the customer name
            val nameHeight = drawText(
                customerName,
                textX,
                textY,
                textWidth,
                title,
            )

            // Draw the event name
            val eventNameHeight = drawText(
                eventName,
                textX,
                (textY + Millimeter.fromPx(nameHeight)).also { textY = it },
                textWidth,
                subtitle,
            )

            // Draw the event date
            if (eventDate != null)
                drawText(
                    eventDate,
                    textX,
                    (textY + Millimeter.fromPx(eventNameHeight)).also { textY = it },
                    textWidth,
                    subtitle,
                )

            // Draw the borders
            drawRect(x, y, TICKET_WIDTH.mm, TICKET_HEIGHT.mm, strokePaint)
        }

    private suspend fun newPage(
        document: PdfDocument,
        pageNumber: Int,
        pageWidth: Int,
        pageHeight: Int,
        allTickets: List<TicketData>,
        ticketsPerPage: Int,
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
                var counter = ticketsPerPage * pageNumber
                val groups = tickets.chunked(TICKETS_HORIZONTAL)
                for ((yIndex, group) in groups.withIndex())
                    for ((xIndex, ticket) in group.withIndex()) {
                        setProgress(counter++, allTickets.size)
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

    private suspend fun generatePDF(
        tickets: List<TicketData>,
        target: ParcelFileDescriptor,
    ) {
        Log.i(TAG, "Generating PDF for ${tickets.size}...")

        val document = PdfDocument()

        val pageWidth = PAGE_WIDTH.mm.psPoints.toInt()
        val pageHeight = PAGE_HEIGHT.mm.psPoints.toInt()

        val ticketHeight = TICKET_HEIGHT.mm.psPoints
        val verticalPadding = RULE_Y.mm.psPoints
        val ticketsVertical = ((pageHeight - 2 * verticalPadding) / ticketHeight).toInt()
        val ticketsPerPage = TICKETS_HORIZONTAL * ticketsVertical

        var pageCounter = 1
        while (
            newPage(
                document,
                pageCounter,
                pageWidth,
                pageHeight,
                tickets,
                ticketsPerPage,
            )
        ) pageCounter++

        Log.d(TAG, "Writing file...")
        target.use { parcelFileDescriptor ->
            val descriptor = parcelFileDescriptor.fileDescriptor
            FileOutputStream(descriptor).use { stream ->
                document.writeTo(stream)
            }
        }
        Log.d(TAG, "PDF written successfully.")
    }

    private suspend fun setProgress(current: Int, max: Int) {
        setProgress(
            workDataOf(
                PROGRESS_CURRENT to current,
                PROGRESS_MAX to max,
            )
        )
    }
}