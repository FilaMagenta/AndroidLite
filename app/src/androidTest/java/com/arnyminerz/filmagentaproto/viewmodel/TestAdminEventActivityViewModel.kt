package com.arnyminerz.filmagentaproto.viewmodel

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arnyminerz.filamagenta.core.database.data.woo.Customer
import com.arnyminerz.filamagenta.core.database.data.woo.Event
import com.arnyminerz.filamagenta.core.database.data.woo.Order
import com.arnyminerz.filamagenta.core.database.data.woo.OrderStatus
import com.arnyminerz.filamagenta.core.database.data.woo.ROLE_SUBSCRIBER
import com.arnyminerz.filamagenta.core.database.data.woo.customer.DeliveryInformation
import com.arnyminerz.filmagentaproto.activity.AdminEventActivity
import com.arnyminerz.filmagentaproto.utils.DatabaseTest
import com.arnyminerz.filmagentaproto.utils.await
import com.arnyminerz.filmagentaproto.utils.createQRAndDecode
import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestAdminEventActivityViewModel: DatabaseTest() {
    companion object {
        private const val qrSize = 400

        private val customer = Customer(
            id = 123,
            dateCreated = Date(),
            dateModified = Date(),
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
            dateCreated = Date(),
            dateModified = Date(),
            total = 0.0,
            customerId = 123,
            payment = null,
            items = listOf(),
        )
    }

    private val viewModel: AdminEventActivity.ViewModel by lazy {
        AdminEventActivity.ViewModel(context.applicationContext as Application)
    }

    private val event = Event.EXAMPLE

    @Before
    fun prepare_events() = runBlocking {
        viewModel.wooCommerceDao.insert(event)
    }

    @Test
    fun test_load() {
        // Run the viewModel.load method and wait until completion
        viewModel.load(eventId = event.id).await()

        val event = viewModel.event.value
        assertNotNull(event)
        assertEquals(this.event, event)
    }

    @Test
    fun test_decodeQR() {
        // Create a new QR for the stored order and decode it
        val result = createQRAndDecode(context, order, customer, qrSize)
        // Pass the result through the viewModel
        viewModel.decodeQR(result?.text).await()

        // Check result and make sure the scan result is ok
        var scanResult = viewModel.scanResult.value
        var storedHash = runBlocking { viewModel.adminDao.getFromHash(order.hash) }
        assertEquals(scanResult, AdminEventActivity.SCAN_RESULT_OK)
        assertNotNull(storedHash)
        assertNotEquals(0, storedHash?.id) // Make sure a proper id has been set

        // Scanning again should return scan repeated error
        viewModel.decodeQR(result?.text).await()
        scanResult = viewModel.scanResult.value
        storedHash = runBlocking { viewModel.adminDao.getFromHash(order.hash) }
        assertEquals(scanResult, AdminEventActivity.SCAN_RESULT_REPEATED)
        assertNotNull(storedHash)
    }

}
