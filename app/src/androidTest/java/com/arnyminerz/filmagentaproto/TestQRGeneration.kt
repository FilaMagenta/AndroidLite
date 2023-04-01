package com.arnyminerz.filmagentaproto

import android.content.Context
import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import com.arnyminerz.filamagenta.core.database.data.woo.CustomerProto
import com.arnyminerz.filamagenta.core.database.data.woo.PROCESSING
import com.arnyminerz.filamagenta.core.database.data.woo.ROLE_SUBSCRIBER
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.logic.QR_VERSION
import com.arnyminerz.filmagentaproto.database.logic.getQRCode
import com.arnyminerz.filmagentaproto.database.logic.verifyQRCode
import com.arnyminerz.filmagentaproto.security.AESEncryption
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TestQRGeneration {
    companion object {
        private const val qrSize = 400

        private val context: Context by lazy {
            InstrumentationRegistry.getInstrumentation().targetContext
        }
    }

    private val customer = Customer(
        id = 123,
        dateCreated = Date(),
        dateModified = Date(),
        email = "example@mail.com",
        firstName = "Example",
        lastName = "User",
        role = ROLE_SUBSCRIBER,
        username = "ExampleUser",
        billing = CustomerProto.DeliveryInformation(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        ),
        shipping = CustomerProto.DeliveryInformation(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        ),
        isPayingCustomer = true,
        avatarUrl = "",
    )
    private val order = Order(
        id = 1,
        status = PROCESSING,
        currency = "",
        dateCreated = Date(),
        dateModified = Date(),
        total = 0.0,
        customerId = 123,
        payment = null,
        items = listOf(),
    )

    @Test
    fun test_QRCreation() {
        val qr = with(context) {
            order.getQRCode(customer, size = qrSize)
        }

        assertEquals(qrSize, qr.width)
        assertEquals(qrSize, qr.height)
    }

    /** Creates a new QR code from [order], and runs [QRCodeReader.decode] on it */
    private fun createQRAndDecode(): Result? {
        val qr = with(context) {
            order.getQRCode(customer, size = qrSize)
        }

        val intArray = IntArray(qr.width * qr.height)
        qr.getPixels(intArray, 0, qr.width, 0, 0, qr.width, qr.height)

        val source = RGBLuminanceSource(qr.width, qr.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        val reader = QRCodeReader()
        return reader.decode(bitmap)
    }

    /** Checks that the created QR can be read and verified. */
    @Test
    fun test_QRValid() {
        val result = createQRAndDecode()
        assertNotNull(result)

        val contents = result!!.text

        val verification = Order.verifyQRCode(contents)
        assertNotNull(verification)
        assertEquals(customer.fullName, verification?.customerName)
        assertEquals(QR_VERSION, verification?.version)
        assertEquals(order.hash, verification?.hash)
    }

    /** Tests QRs generated with different keys. */
    @Test
    fun test_QRInvalid() {
        mockkObject(AESEncryption)
        every { AESEncryption.getSecretKey() } returns Base64.encodeToString("this-is-an-invalid-key".toByteArray(), Base64.NO_WRAP)

        val result = createQRAndDecode()
        assertNotNull(result)

        verify { AESEncryption.getSecretKey() }
        unmockkObject(AESEncryption)

        val contents = result!!.text

        val verification = Order.verifyQRCode(contents)
        assertNull(verification)
    }
}