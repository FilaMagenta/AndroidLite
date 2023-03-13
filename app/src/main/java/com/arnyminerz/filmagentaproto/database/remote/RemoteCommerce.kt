package com.arnyminerz.filmagentaproto.database.remote

import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.annotation.WorkerThread
import com.arnyminerz.filmagentaproto.BuildConfig
import com.arnyminerz.filmagentaproto.database.data.woo.AvailablePayment
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializer
import com.arnyminerz.filmagentaproto.utils.divideMoney
import com.arnyminerz.filmagentaproto.utils.io
import com.arnyminerz.filmagentaproto.utils.mapObjects
import com.arnyminerz.filmagentaproto.utils.toJSONObjectsArray
import com.arnyminerz.filmagentaproto.utils.toURL
import java.io.IOException
import javax.net.ssl.HttpsURLConnection
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

private const val TAG = "RemoteCommerce"

object RemoteCommerce {
    private val BaseEndpoint = Uri.Builder()
        .scheme("https")
        .authority(BuildConfig.HOST)
        .appendPath("wp-json")
        .appendPath("wc")
        .appendPath("v3")
        .build()

    private val ProductsEndpoint = BaseEndpoint.buildUpon()
        .appendPath("products")
        .build()

    private val OrdersEndpoint = BaseEndpoint.buildUpon()
        .appendPath("orders")
        .build()

    private val CustomersEndpoint = BaseEndpoint.buildUpon()
        .appendPath("customers")
        .build()

    private const val CATEGORY_EVENTOS = 21

    private const val CATEGORY_PAGO_FULLA = 38

    @WorkerThread
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun get(endpoint: Uri): String = io {
        val url = endpoint.toURL()
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = "GET"
        connection.readTimeout = 60 * 1000
        connection.connectTimeout = 60 * 1000
        connection.instanceFollowRedirects = true

        val auth = BuildConfig.WOO_CONSUMER_KEY + ":" + BuildConfig.WOO_CONSUMER_SECRET
        val authBytes = auth.toByteArray(Charsets.UTF_8)
        val encodedAuthBytes = Base64.encode(authBytes, Base64.NO_WRAP)
        val encodedAuth = encodedAuthBytes.toString(Charsets.UTF_8)
        connection.setRequestProperty("Authorization", "Basic $encodedAuth")

        try {
            connection.connect()

            when (connection.responseCode) {
                in 200 until 300 -> {
                    return@io connection.inputStream.bufferedReader().readText()
                }
                else -> throw IOException("Request failed (${connection.responseCode}): ${connection.responseMessage}")
            }
        } finally {
            connection.disconnect()
        }
    }

    @WorkerThread
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun post(endpoint: Uri, body: JSONObject): String = io {
        val url = endpoint.toURL()
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = "POST"
        connection.readTimeout = 60 * 1000
        connection.connectTimeout = 60 * 1000
        connection.instanceFollowRedirects = true

        val auth = BuildConfig.WOO_CONSUMER_KEY + ":" + BuildConfig.WOO_CONSUMER_SECRET
        val authBytes = auth.toByteArray(Charsets.UTF_8)
        val encodedAuthBytes = Base64.encode(authBytes, Base64.NO_WRAP)
        val encodedAuth = encodedAuthBytes.toString(Charsets.UTF_8)
        connection.setRequestProperty("Authorization", "Basic $encodedAuth")

        val bodyString = body.toString()
        val bodyBytes = bodyString.toByteArray()
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Content-Length", bodyBytes.size.toString())

        try {
            connection.connect()

            connection.outputStream.use {
                it.write(bodyBytes)
            }

            when (connection.responseCode) {
                in 200 until 300 -> {
                    return@io connection.inputStream.bufferedReader().readText()
                }
                else -> {
                    throw IOException("Request failed (${connection.responseCode}): ${connection.responseMessage}.")
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Performs a GET request on a multi-page endpoint.
     * @param uri The uri to perform the request to
     * @param page The current page of the request. Should not be modified.
     * @param perPage The amount of elements to get for each page.
     * @param pageProcessor Will get called on each element of the response for converting into [T].
     */
    private suspend fun <T> multiPageGet(
        uri: Uri,
        page: Int = 1,
        perPage: Int = 40,
        pageProcessor: (json: JSONObject) -> T,
    ): List<T> {
        Log.d(TAG, "Getting page $page of $uri...")
        val endpoint = uri.buildUpon()
            .appendQueryParameter("page", "$page")
            .appendQueryParameter("per_page", "$perPage")
            .build()
        val raw = get(endpoint)
        val json = JSONArray(raw)
        val objects = json.mapObjects(pageProcessor).toMutableList()
        if (objects.size >= perPage)
            objects.addAll(multiPageGet(uri, page + 1, perPage, pageProcessor))
        return objects
    }

    /**
     * Performs a GET request on a multi-page endpoint.
     * @param uri The uri to perform the request to
     * @param page The current page of the request. Should not be modified.
     * @param perPage The amount of elements to get for each page.
     * @param serializer Used for converting each entry of the response into [T].
     */
    private suspend fun <T : Any> multiPageGet(
        uri: Uri,
        serializer: JsonSerializer<T>,
        page: Int = 1,
        perPage: Int = 40,
    ): List<T> = multiPageGet(uri, page, perPage) { serializer.fromJSON(it) }

    /**
     * Fetches all the events available from the server.
     * @return A list of all the events currently published.
     * @throws JSONException If the returned answer's format is not correct.
     * @throws NullPointerException If there's an invalid field in the response.
     */
    @WorkerThread
    suspend fun eventList(): List<Event> {
        val endpoint = ProductsEndpoint.buildUpon()
            .appendQueryParameter("status", "publish")
            .appendQueryParameter("category", CATEGORY_EVENTOS.toString())
            .build()
        return multiPageGet(endpoint, Event.Companion)
    }

    /**
     * Fetches all the orders made by the given customer.
     */
    @WorkerThread
    suspend fun orderList(customerId: Long): List<Order> {
        val endpoint = OrdersEndpoint.buildUpon()
            .appendQueryParameter("customer", customerId.toString())
            .build()
        return multiPageGet(endpoint, Order.Companion)
    }

    @WorkerThread
    suspend fun customersList(page: Int = 1): List<Customer> {
        Log.d(TAG, "Getting page $page of customers...")
        val endpoint = CustomersEndpoint.buildUpon()
            .appendQueryParameter("context", "view")
            .appendQueryParameter("role", "all")
            .build()
        return multiPageGet(endpoint, Customer.Companion, perPage = 50)
    }


    /**
     * Fetches all the events available from the server.
     * @return A list of all the events currently published.
     * @throws JSONException If the returned answer's format is not correct.
     * @throws NullPointerException If there's an invalid field in the response.
     */
    @WorkerThread
    suspend fun paymentsList(): List<AvailablePayment> {
        val endpoint = ProductsEndpoint.buildUpon()
            .appendQueryParameter("status", "publish")
            .appendQueryParameter("category", CATEGORY_PAGO_FULLA.toString())
            .build()
        return multiPageGet(endpoint, AvailablePayment.Companion)
    }

    /**
     * Creates a new payment as the given user, for the given amount.
     * Divides the amount into the different [availablePayments].
     * @param amount The amount of money to transfer.
     * @param concept If any, some extra notes to leave together with the transfer.
     * @param availablePayments The available packages of money transferable.
     * @param customer The customer that is making the payment.
     * @return The URL for making the payment.
     */
    @WorkerThread
    suspend fun transferAmount(
        amount: Double,
        concept: String,
        availablePayments: List<AvailablePayment>,
        customer: Customer,
    ): String {
        val packs = divideMoney(amount, availablePayments)
        val lineItems = packs
            .map { (productId, amount) ->
                JSONObject().apply {
                    put("product_id", productId)
                    put("quantity", amount)
                }
            }
            .toJSONObjectsArray()
        val body = JSONObject().apply {
            put("customer_id", customer.id)
            put("customer_note", concept)
            put("billing", customer.billing.toJSON())
            put("shipping", customer.shipping.toJSON())
            put("paid", false)
            put("line_items", lineItems)
        }
        Log.d(TAG, "Making POST request to $OrdersEndpoint with: $body")
        val response = post(OrdersEndpoint, body)
        val json = JSONObject(response)
        return json.getString("payment_url")
    }

    /**
     * Signs up the customer to the desired event.
     * @param notes Some extra notes, if any, to leave.
     * @param variants If any, variants selected for the event. Key is product id, and value variant id.
     * @param customer The customer that is making the request.
     */
    @WorkerThread
    suspend fun eventSignup(customer: Customer, notes: String, variants: Map<Long, Long>) {
        val items = variants.map { (key, value) ->
            JSONObject().apply {
                put("product_id", key)
                put("variation_id", value)
                put("quantity", 1)
            }
        }

        val body = JSONObject().apply {
            put("customer_id", customer.id)
            put("customer_note", notes.takeIf { it.isNotBlank() })
            put("billing", customer.billing.toJSON())
            put("shipping", customer.shipping.toJSON())
            put("set_paid", true)
            put("line_items", items.toJSONObjectsArray())
        }
        Log.d(TAG, "Making POST request to $OrdersEndpoint with: $body")
        post(OrdersEndpoint, body)
    }
}
