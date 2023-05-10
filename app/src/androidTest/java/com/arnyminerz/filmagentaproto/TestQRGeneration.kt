package com.arnyminerz.filmagentaproto

import android.content.Context
import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import com.arnyminerz.filamagenta.core.database.data.woo.Customer
import com.arnyminerz.filamagenta.core.database.data.woo.Order
import com.arnyminerz.filamagenta.core.database.data.woo.OrderStatus
import com.arnyminerz.filamagenta.core.database.data.woo.ROLE_SUBSCRIBER
import com.arnyminerz.filamagenta.core.database.data.woo.customer.DeliveryInformation
import com.arnyminerz.filmagentaproto.database.logic.QR_VERSION
import com.arnyminerz.filmagentaproto.database.logic.getQRCode
import com.arnyminerz.filmagentaproto.database.logic.verifyQRCode
import com.arnyminerz.filmagentaproto.security.AESEncryption
import com.arnyminerz.filmagentaproto.utils.createQRAndDecode
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class TestQRGeneration {
    companion object {
        private const val qrSize = 400

        private val context: Context by lazy {
            InstrumentationRegistry.getInstrumentation().targetContext
        }
    }

    private val customer = Customer(
        id = 123,
        dateCreated = LocalDate.now(),
        dateModified = LocalDate.now(),
        email = "example@mail.com",
        firstName = "Example",
        lastName = "User",
        role = ROLE_SUBSCRIBER,
        username = "ExampleUser",
        billing = DeliveryInformation(
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
        shipping = DeliveryInformation(
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
        status = OrderStatus.PROCESSING,
        currency = "",
        dateCreated = LocalDate.now(),
        dateModified = LocalDate.now(),
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

    /** Checks that the created QR can be read and verified. */
    @Test
    fun test_QRValid() {
        val result = createQRAndDecode(context, order, customer, qrSize)
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

        val result = createQRAndDecode(context, order, customer, qrSize)
        assertNotNull(result)

        verify { AESEncryption.getSecretKey() }
        unmockkObject(AESEncryption)

        val contents = result!!.text

        val verification = Order.verifyQRCode(contents)
        assertNull(verification)
    }
}