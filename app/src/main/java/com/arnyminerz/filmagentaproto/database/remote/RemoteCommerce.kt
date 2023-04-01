package com.arnyminerz.filmagentaproto.database.remote

import android.net.Uri
import android.util.Base64
import androidx.annotation.WorkerThread
import com.arnyminerz.filmagentaproto.BuildConfig
import com.arnyminerz.filmagentaproto.database.data.woo.AvailablePayment
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializer
import com.arnyminerz.filmagentaproto.utils.divideMoney
import com.arnyminerz.filmagentaproto.utils.getDateGmt
import com.arnyminerz.filmagentaproto.utils.getStringJSONArray
import com.arnyminerz.filmagentaproto.utils.io
import com.arnyminerz.filmagentaproto.utils.mapObjects
import com.arnyminerz.filmagentaproto.utils.mapObjectsIndexed
import com.arnyminerz.filmagentaproto.utils.toJSON
import com.arnyminerz.filmagentaproto.utils.toJSONObjectsArray
import com.arnyminerz.filmagentaproto.utils.toURL
import java.io.IOException
import javax.net.ssl.HttpsURLConnection
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

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

    private val AttributesEndpoint = ProductsEndpoint.buildUpon()
        .appendPath("attributes")
        .build()

    private val OrdersEndpoint = BaseEndpoint.buildUpon()
        .appendPath("orders")
        .build()

    private val CustomersEndpoint = BaseEndpoint.buildUpon()
        .appendPath("customers")
        .build()

    private const val CATEGORY_EVENTOS = 21

    private const val CATEGORY_PAGO_FULLA = 38

    private fun <R> Uri.openConnection(
        method: String,
        beforeConnection: (HttpsURLConnection) -> Unit = {},
        block: (HttpsURLConnection) -> R,
    ): R {
        Timber.d("$method > $this")
        val url = toURL()
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = method
        connection.readTimeout = 45 * 1000
        connection.connectTimeout = 20 * 1000
        connection.instanceFollowRedirects = true

        val auth = BuildConfig.WOO_CONSUMER_KEY + ":" + BuildConfig.WOO_CONSUMER_SECRET
        val authBytes = auth.toByteArray(Charsets.UTF_8)
        val encodedAuthBytes = Base64.encode(authBytes, Base64.NO_WRAP)
        val encodedAuth = encodedAuthBytes.toString(Charsets.UTF_8)
        connection.setRequestProperty("Authorization", "Basic $encodedAuth")

        beforeConnection(connection)

        return try {
            connection.connect()

            block(connection)
        } catch (e: Exception) {
            throw e
        } finally {
            connection.disconnect()
        }
    }

    @WorkerThread
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun get(endpoint: Uri): String = io {
        endpoint.openConnection("GET") { connection ->
            when (connection.responseCode) {
                in 200 until 300 -> connection.inputStream.bufferedReader().readText()
                else -> throw IOException("Request failed (${connection.responseCode}): ${connection.responseMessage}")
            }
        }
    }

    @WorkerThread
    private suspend fun <T : Any> get(endpoint: Uri, serializer: JsonSerializer<T>): T =
        JSONObject(get(endpoint)).let { serializer.fromJSON(it) }

    @WorkerThread
    private suspend fun <T : Any> getList(endpoint: Uri, serializer: JsonSerializer<T>): List<T> =
        JSONArray(get(endpoint)).mapObjects { serializer.fromJSON(it) }

    @WorkerThread
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun post(endpoint: Uri, body: JSONObject): String = io {
        val bodyString = body.toString()
        val bodyBytes = bodyString.toByteArray()

        endpoint.openConnection("POST", { connection ->
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Content-Length", bodyBytes.size.toString())
        }) { connection ->
            connection.outputStream.use { it.write(bodyBytes) }

            when (connection.responseCode) {
                in 200 until 300 -> connection.inputStream.bufferedReader().readText()
                else -> throw IOException("Request failed (${connection.responseCode}): ${connection.responseMessage}")
            }
        }
    }

    @WorkerThread
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun delete(endpoint: Uri): String = io {
        endpoint.openConnection("POST", { connection ->
            connection.setRequestProperty("X-HTTP-Method-Override", "DELETE")
        }) { connection ->
            when (connection.responseCode) {
                in 200 until 300 -> connection.inputStream.bufferedReader().readText()
                else -> throw IOException("Request failed (${connection.responseCode}): ${connection.responseMessage}")
            }
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
        pageProcessor: suspend (json: JSONObject, progress: Pair<Int, Int>) -> T,
    ): List<T> {
        Timber.d("Getting page $page of $uri...")
        val endpoint = uri.buildUpon()
            .appendQueryParameter("page", "$page")
            .appendQueryParameter("per_page", "$perPage")
            .build()
        val raw = get(endpoint)
        val json = JSONArray(raw)
        val objects = json.mapObjectsIndexed { obj, index ->
            pageProcessor(obj, index to json.length())
        }.toMutableList()
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
    ): List<T> = multiPageGet(uri, page, perPage) { json, _ -> serializer.fromJSON(json) }

    /**
     * Fetches all the events available from the server.
     * @return A list of all the events currently published.
     * @throws JSONException If the returned answer's format is not correct.
     * @throws NullPointerException If there's an invalid field in the response.
     */
    @WorkerThread
    suspend fun eventList(
        cachedEvents: List<Event>,
        progressCallback: suspend (progress: Pair<Int, Int>) -> Unit,
    ): List<Event> {
        val endpoint = ProductsEndpoint.buildUpon()
            .appendQueryParameter("status", "publish")
            .appendQueryParameter("category", CATEGORY_EVENTOS.toString())
            .build()

        val cachedAttributes = getList(AttributesEndpoint, Event.Attribute)
            .associateBy { it.id }
            .toMutableMap()

        return multiPageGet(endpoint, perPage = 100) { eventJson, progress ->
            progressCallback(progress)

            Timber.d("Parsing event.")
            val eventId = eventJson.getLong("id")
            val price = eventJson.getDouble("price")

            // Check if the event is cached
            cachedEvents.find { it.id == eventId }?.let { event ->
                // If it has been cached, compare the modification dates
                val newModificationDate = eventJson.getDateGmt("date_modified_gmt")
                if (newModificationDate <= event.dateModified) {
                    // If the event has not been updated, return the cached one
                    Timber.d("Event #$eventId has not been updated ($newModificationDate). Taking cache.")
                    return@multiPageGet event
                }
            }

            Timber.d("Getting variations...")
            val variationEndpoint = ProductsEndpoint.buildUpon()
                .appendPath(eventId.toString())
                .appendPath("variations")
                .build()
            val variations = getList(variationEndpoint, Event.Variation.Companion)
            Timber.d("Got ${variations.size} variations for event #$eventId: $variations")

            Timber.d("Event parsing. Processing attributes...")
            val attributes = eventJson.getJSONArray("attributes").mapObjects { attributeJson ->
                val id = attributeJson.getLong("id")
                val options = attributeJson.getStringJSONArray("options")

                val attribute = cachedAttributes.getValue(id)
                Timber.d("Processing event attributes...")
                val variation = variations.find { variation ->
                    variation.attributes.find { it.id == attribute.id } != null
                }
                attribute.copy(
                    options = options.map { optionName ->
                        val optionVar = variations.find { variation ->
                            variation.attributes.find { it.option == optionName } != null
                        }
                        Event.Attribute.Option(optionName, optionVar?.price ?: price)
                    },
                    variation = variation,
                )
            }
            Timber.d("Converting JSON to Event...")
            Event.fromJSON(eventJson).copy(attributes = attributes)
        }
    }

    /**
     * Fetches all the orders made by the given customer.
     * @param customerId If not null, only the orders made by the customer with this ID will be
     * fetched, otherwise all the orders will be retrieved.
     */
    @WorkerThread
    suspend fun orderList(customerId: Long?): List<Order> {
        val endpoint = OrdersEndpoint.buildUpon()
            .let { builder ->
                if (customerId != null)
                    builder.appendQueryParameter("customer", customerId.toString())
                else
                    builder
            }
            .build()
        return multiPageGet(endpoint, Order.Companion, perPage = 100)
    }

    @WorkerThread
    suspend fun customersList(page: Int = 1): List<Customer> {
        Timber.d("Getting page $page of customers...")
        val endpoint = CustomersEndpoint.buildUpon()
            .appendQueryParameter("context", "view")
            .appendQueryParameter("role", "all")
            .build()
        return multiPageGet(endpoint, Customer.Companion, perPage = 100)
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
        return multiPageGet(endpoint, AvailablePayment.Companion, perPage = 100)
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
    @Deprecated("Use RedSys gateway")
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
        Timber.d("Making POST request to $OrdersEndpoint with: $body")
        val response = post(OrdersEndpoint, body)
        val json = JSONObject(response)
        return json.getString("payment_url")
    }

    /**
     * Signs up the customer to the desired event.
     * @param notes Some extra notes, if any, to leave.
     * @param event The event to sign up for.
     * @param customer The customer that is making the request.
     * @return The URL for making the payment for the event, and the order made.
     */
    @WorkerThread
    suspend fun eventSignup(
        customer: Customer,
        notes: String,
        event: Event,
        metadata: List<Order.Metadata>
    ): Pair<String, Order> {
        Timber.d("Creating item for event...")
        val item = JSONObject().apply {
            put("product_id", event.id)
            put("quantity", 1)
            put("meta_data", metadata.toJSON())
        }

        Timber.d("Building body for request...")
        val body = JSONObject().apply {
            put("customer_id", customer.id)
            put("customer_note", notes.takeIf { it.isNotBlank() })
            put("billing", customer.billing.toJSON())
            put("shipping", customer.shipping.toJSON())
            put("set_paid", event.price <= 0.0)
            put("line_items", JSONArray().apply { put(item) })
        }
        Timber.d( "Making POST request to $OrdersEndpoint with: $body")
        val response = post(OrdersEndpoint, body)
        val json = JSONObject(response)
        val responseOrder = Order.fromJSON(json)
        return json.getString("payment_url") to responseOrder
    }

    /**
     * Deletes the order with the given ID.
     */
    @WorkerThread
    suspend fun eventCancel(orderId: Long) {
        val endpoint = OrdersEndpoint.buildUpon()
            .appendPath(orderId.toString())
            .build()
        delete(endpoint)
    }
}
