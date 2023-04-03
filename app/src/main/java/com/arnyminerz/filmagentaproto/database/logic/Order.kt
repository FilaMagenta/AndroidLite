package com.arnyminerz.filmagentaproto.database.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Base64
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRect
import com.arnyminerz.filamagenta.core.database.data.woo.Customer
import com.arnyminerz.filamagenta.core.database.data.woo.Order
import com.arnyminerz.filamagenta.core.utils.getIntOrNull
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.documents.RectD
import com.arnyminerz.filmagentaproto.qr.BarcodeEncoder
import com.arnyminerz.filmagentaproto.security.AESEncryption
import com.google.zxing.BarcodeFormat
import org.json.JSONObject
import timber.log.Timber

data class QRVerification(
    val customerName: String,
    val hash: String,
    val version: Int,
)

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
const val QR_VERSION = 1

/**
 * Verifies the contents of a scanned QR code.
 * @param contents What was scanned from the QR code.
 * @return `null` if the code is not valid, the customer's name otherwise.
 */
fun Order.Companion.verifyQRCode(contents: String): QRVerification? {
    val decoded = Base64.decode(contents, Base64.NO_WRAP).decodeToString()
    val json = JSONObject(decoded)
    if (!json.has("customer") || !json.has("confirmation_code") || !json.has("confirmation_hash")) {
        Timber.d("JSON missing keys.")
        return null
    }

    val code = json.getString("confirmation_code")
    val hash = json.getString("confirmation_hash")
    val version = json.getIntOrNull("version")
    val customer = json.getString("customer")

    // Decode de hash code
    val decodedCode = Base64.decode(code, Base64.NO_WRAP).decodeToString()
    // And decrypt the confirmation hash
    val decryptedHash = AESEncryption.decrypt(hash)

    // If they are not equal, return null
    if (decodedCode != decryptedHash) return null

    if (version != QR_VERSION) throw SecurityException("Scanned a QR code from an older version.")

    return QRVerification(customer, decodedCode, version)
}

/**
 * Obtains a QR code with the data of the order. Contains a QR code with the customer's name,
 * with key "customer"; and another key named "confirmation", that contains the confirmation
 * code to be scanned.
 * @param customer The customer that is holder of this QR.
 * @param size The size of the image generated.
 * @param logoSize The size of the logo added inside of the QR.
 */
context(Context)
fun Order.getQRCode(customer: Customer, size: Int = 400, logoSize: Int = 80): Bitmap {
    val barcodeEncoder = BarcodeEncoder()
    val content = JSONObject().apply {
        val encodedCode = Base64.encodeToString(hash.toByteArray(), Base64.NO_WRAP)

        put("customer", customer.fullName)
        put("version", QR_VERSION)
        put("confirmation_code", encodedCode)
        put("confirmation_hash", AESEncryption.encrypt(hash))
    }
    val encodedContents = Base64.encodeToString(content.toString().toByteArray(), Base64.NO_WRAP)
    // Create the QR code
    val bitmap = barcodeEncoder.encodeBitmap(encodedContents, BarcodeFormat.QR_CODE, size, size)
    // Return the modified QR code (with the logo embedded)
    return bitmap.applyCanvas {
        val halfLogo = logoSize / 2

        val logoLeft = width / 2f - halfLogo
        val logoTop = height / 2f - halfLogo
        val logoRight = width / 2f + halfLogo
        val logoBottom = height / 2f + halfLogo

        // Draw a background white square with size [logoSize]
        drawRect(
            logoLeft,
            logoTop,
            logoRight,
            logoBottom,
            Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
        )

        // Now draw the monochrome icon
        ContextCompat.getDrawable(this@Context, R.drawable.logo_magenta_mono)
            ?.apply {
                setTint(Color.BLACK)
            }
            ?.toBitmap(logoSize, logoSize)
            ?.let { logo ->
                val srcRect = RectD.Size(logo.width, logo.height)
                val targetRect = RectF(logoLeft, logoTop, logoRight, logoBottom)
                drawBitmap(
                    logo,
                    srcRect.toRect(),
                    targetRect,
                    Paint().apply {
                        isAntiAlias = true
                        isFilterBitmap = true
                        isDither = true
                    },
                )
            }
    }
}
