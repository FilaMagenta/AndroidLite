package com.arnyminerz.filmagentaproto.database.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Base64
import androidx.core.content.ContextCompat
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRect
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.documents.RectD
import com.arnyminerz.filmagentaproto.security.AESEncryption
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.json.JSONObject
import timber.log.Timber

data class QRVerification(
    val customerName: String,
    val hashCode: Long,
)

/**
 * Verifies the contents of a scanned QR code.
 * @param contents What was scanned from the QR code.
 * @return `null` if the code is not valid, the customer's name otherwise.
 */
fun Order.Companion.verifyQRCode(contents: String): QRVerification? {
    Timber.d("Encoded QR code: $contents")
    val decoded = Base64.decode(contents, Base64.NO_WRAP).decodeToString()
    Timber.d("Decoded QR code: $decoded")
    val json = JSONObject(decoded)
    if (!json.has("customer") || !json.has("confirmation_code") || !json.has("confirmation_hash")) {
        Timber.d("JSON missing keys.")
        return null
    }
    val code = json.getString("confirmation_code")
    val decodedCode = Base64.decode(code, Base64.NO_WRAP).decodeToString()
    Timber.d("Decoded code: $decodedCode")
    val hash = json.getString("confirmation_hash")
    val decryptedHash = AESEncryption.decrypt(hash)
    Timber.d("Decrypted Hash: $decryptedHash")
    if (decodedCode != decryptedHash)
        return null

    val customer = json.getString("customer")
    val hashCode = code.toLong()

    return QRVerification(customer, hashCode)
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
        put("customer", customer.firstName + " " + customer.lastName)
        val encodedCode =
            Base64.encodeToString(hashCode().toString().toByteArray(), Base64.NO_WRAP)
        put("confirmation_code", encodedCode)
        put("confirmation_hash", AESEncryption.encrypt(hashCode().toString()))
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
