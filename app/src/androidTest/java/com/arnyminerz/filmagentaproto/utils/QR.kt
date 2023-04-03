package com.arnyminerz.filmagentaproto.utils

import android.content.Context
import com.arnyminerz.filamagenta.core.database.data.woo.Customer
import com.arnyminerz.filamagenta.core.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.logic.getQRCode
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader

/** Creates a new QR code from [order], and runs [QRCodeReader.decode] on it */
fun createQRAndDecode(
    context: Context,
    order: Order,
    customer: Customer,
    size: Int
): Result? {
    val qr = with(context) {
        order.getQRCode(customer, size = size)
    }

    val intArray = IntArray(qr.width * qr.height)
    qr.getPixels(intArray, 0, qr.width, 0, 0, qr.width, qr.height)

    val source = RGBLuminanceSource(qr.width, qr.height, intArray)
    val bitmap = BinaryBitmap(HybridBinarizer(source))

    val reader = QRCodeReader()
    return reader.decode(bitmap)
}
