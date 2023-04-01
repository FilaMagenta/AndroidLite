package com.arnyminerz.filamagenta.core.remote

import com.arnyminerz.filamagenta.core.Logger
import com.arnyminerz.filamagenta.core.database.data.woo.AvailablePaymentProto
import com.arnyminerz.filamagenta.core.database.data.woo.CustomerProto
import com.arnyminerz.filamagenta.core.database.data.woo.EventProto
import com.arnyminerz.filamagenta.core.database.data.woo.OrderProto
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.divideMoney
import com.arnyminerz.filamagenta.core.utils.getDateGmt
import com.arnyminerz.filamagenta.core.utils.getStringJSONArray
import com.arnyminerz.filamagenta.core.utils.io
import com.arnyminerz.filamagenta.core.utils.mapObjects
import com.arnyminerz.filamagenta.core.utils.mapObjectsIndexed
import com.arnyminerz.filamagenta.core.utils.toJSON
import com.arnyminerz.filamagenta.core.utils.toJSONObjectsArray
import com.arnyminerz.filamagenta.core.utils.uri.UriBuilder
import com.arnyminerz.filamagenta.core.utils.uri.buildUpon
import java.io.IOException
import java.net.URI
import javax.net.ssl.HttpsURLConnection
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

abstract class RemoteCommerceProto <Order: OrderProto, Customer: CustomerProto, Event: EventProto, AvailablePayment: AvailablePaymentProto> {
    companion object {
        private const val CATEGORY_EVENTOS = 21

        private const val CATEGORY_PAGO_FULLA = 38
    }

    /** The hostname of the base server. */
    protected abstract val host: String

    /** Defined together with [wooConsumerSecret]. Provides access to the server. */
    protected abstract val wooConsumerKey: String

    /** Defined together with [wooConsumerKey]. Provides access to the server. */
    protected abstract val wooConsumerSecret: String

    val baseEndpoint: URI by lazy {
        UriBuilder(host = host)
            .appendPath("wp-json", "wc", "v3")
            .build()
    }

    val productsEndpoint by lazy {
        baseEndpoint.buildUpon()
            .appendPath("products")
            .build()
    }

    val attributesEndpoint by lazy {
        productsEndpoint.buildUpon()
            .appendPath("attributes")
            .build()
    }

    val ordersEndpoint: URI by lazy {
        baseEndpoint.buildUpon()
            .appendPath("orders")
            .build()
    }

    val customersEndpoint by lazy {
        baseEndpoint.buildUpon()
            .appendPath("customers")
            .build()
    }

    protected abstract fun base64Encode(input: ByteArray): ByteArray

    private fun <R> URI.openConnection(
        method: String,
        beforeConnection: (HttpsURLConnection) -> Unit = {},
        block: (HttpsURLConnection) -> R,
    ): R {
        Logger.d("$method > $this")
        val url = toURL()
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = method
        connection.readTimeout = 45 * 1000
        connection.connectTimeout = 20 * 1000
        connection.instanceFollowRedirects = true

        val auth = "$wooConsumerKey:$wooConsumerSecret"
        val authBytes = auth.toByteArray(Charsets.UTF_8)
        val encodedAuthBytes = base64Encode(authBytes)
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

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun get(endpoint: URI): String = io {
        endpoint.openConnection("GET") { connection ->
            when (connection.responseCode) {
                in 200 until 300 -> connection.inputStream.bufferedReader().readText()
                else -> throw IOException("Request failed (${connection.responseCode}): ${connection.responseMessage}")
            }
        }
    }

    private suspend fun <T : Any> getList(endpoint: URI, serializer: JsonSerializer<T>): List<T> =
        JSONArray(get(endpoint)).mapObjects { serializer.fromJSON(it) }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun post(endpoint: URI, body: JSONObject): String = io {
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

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun delete(endpoint: URI): String = io {
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
     * @param URI The URI to perform the request to
     * @param page The current page of the request. Should not be modified.
     * @param perPage The amount of elements to get for each page.
     * @param pageProcessor Will get called on each element of the response for converting into [T].
     */
    private suspend fun <T> multiPageGet(
        URI: URI,
        page: Int = 1,
        perPage: Int = 40,
        pageProcessor: suspend (json: JSONObject, progress: Pair<Int, Int>) -> T,
    ): List<T> {
        Logger.d("Getting page $page of $URI...")
        val endpoint = URI.buildUpon()
            .appendQueryParameter("page", "$page")
            .appendQueryParameter("per_page", "$perPage")
            .build()
        val raw = get(endpoint)
        val json = JSONArray(raw)
        val objects = json.mapObjectsIndexed { obj, index ->
            pageProcessor(obj, index to json.length())
        }.toMutableList()
        if (objects.size >= perPage)
            objects.addAll(multiPageGet(URI, page + 1, perPage, pageProcessor))
        return objects
    }

    /**
     * Performs a GET request on a multi-page endpoint.
     * @param URI The URI to perform the request to
     * @param page The current page of the request. Should not be modified.
     * @param perPage The amount of elements to get for each page.
     * @param serializer Used for converting each entry of the response into [T].
     */
    private suspend fun <T : Any> multiPageGet(
        URI: URI,
        serializer: JsonSerializer<T>,
        page: Int = 1,
        perPage: Int = 40,
    ): List<T> = multiPageGet(URI, page, perPage) { json, _ -> serializer.fromJSON(json) }

    /**
     * Fetches all the events available from the server.
     * @return A list of all the events currently published.
     * @throws JSONException If the returned answer's format is not correct.
     * @throws NullPointerException If there's an invalid field in the response.
     */
    suspend fun eventList(
        cachedEvents: List<Event>,
        progressCallback: suspend (progress: Pair<Int, Int>) -> Unit,
    ): List<Event> {
        val endpoint = productsEndpoint.buildUpon()
            .appendQueryParameter("status", "publish")
            .appendQueryParameter("category", CATEGORY_EVENTOS.toString())
            .build()

        return multiPageGet(endpoint, perPage = 100) { eventJson, progress ->
            progressCallback(progress)

            Logger.d("Parsing event.")
            val eventId = eventJson.getLong("id")
            val price = eventJson.getDouble("price")

            // Check if the event is cached
            cachedEvents.find { it.id == eventId }?.let { event ->
                // If it has been cached, compare the modification dates
                val newModificationDate = eventJson.getDateGmt("date_modified_gmt")
                if (newModificationDate <= event.dateModified) {
                    // If the event has not been updated, return the cached one
                    Logger.d("Event #$eventId has not been updated ($newModificationDate). Taking cache.")
                    return@multiPageGet event
                }
            }

            Logger.d("Getting variations...")
            val variationEndpoint = productsEndpoint.buildUpon()
                .appendPath(eventId.toString())
                .appendPath("variations")
                .build()
            val variations = getList(variationEndpoint, EventProto.Variation.Companion)
            Logger.d("Got ${variations.size} variations for event #$eventId: $variations")

            Logger.d("Event parsing. Processing attributes...")
            val attributes = eventJson.getJSONArray("attributes").mapObjects { attributeJson ->
                val options = attributeJson.getStringJSONArray("options")

                val attribute = EventProto.Attribute.fromJSON(attributeJson)
                Logger.d("Processing event attributes...")
                val variation = variations.find { variation ->
                    variation.attributes.find { it.id == attribute.id } != null
                }
                attribute.copy(
                    options = options.map { optionName ->
                        val optionVar = variations.find { variation ->
                            variation.attributes.find { it.option == optionName } != null
                        }
                        EventProto.Attribute.Option(
                            optionVar?.id,
                            optionName,
                            optionVar?.price ?: price,
                        )
                    },
                    variation = variation,
                )
            }
            Logger.d("Converting JSON to Event...")
            EventProto.fromJSON(eventJson).copy(attributes = attributes) as Event
        }
    }

    /**
     * Fetches all the orders made by the given customer.
     * @param customerId If not null, only the orders made by the customer with this ID will be
     * fetched, otherwise all the orders will be retrieved.
     */
    suspend fun orderList(customerId: Long?): List<Order> {
        val endpoint = ordersEndpoint.buildUpon()
            .let { builder ->
                if (customerId != null)
                    builder.appendQueryParameter("customer", customerId.toString())
                else
                    builder
            }
            .build()
        return multiPageGet(endpoint, OrderProto.Companion, perPage = 100) as List<Order>
    }

    suspend fun customersList(page: Int = 1): List<Customer> {
        Logger.d("Getting page $page of customers...")
        val endpoint = customersEndpoint.buildUpon()
            .appendQueryParameter("context", "view")
            .appendQueryParameter("role", "all")
            .build()
        return multiPageGet(endpoint, CustomerProto.Companion, perPage = 100) as List<Customer>
    }


    /**
     * Fetches all the events available from the server.
     * @return A list of all the events currently published.
     * @throws JSONException If the returned answer's format is not correct.
     * @throws NullPointerException If there's an invalid field in the response.
     */
    suspend fun paymentsList(): List<AvailablePayment> {
        val endpoint = productsEndpoint.buildUpon()
            .appendQueryParameter("status", "publish")
            .appendQueryParameter("category", CATEGORY_PAGO_FULLA.toString())
            .build()
        return multiPageGet(endpoint, AvailablePaymentProto.Companion, perPage = 100) as List<AvailablePayment>
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
        Logger.d("Making POST request to $ordersEndpoint with: $body")
        val response = post(ordersEndpoint, body)
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
    suspend fun eventSignup(
        customer: Customer,
        notes: String,
        event: Event,
        metadata: List<OrderProto.Metadata>
    ): Pair<String, Order> {
        Logger.d("Creating item for event...")
        val item = JSONObject().apply {
            put("product_id", event.id)
            put("quantity", 1)
            put("meta_data", metadata.toJSON())
        }

        Logger.d("Building body for request...")
        val body = JSONObject().apply {
            put("customer_id", customer.id)
            put("customer_note", notes.takeIf { it.isNotBlank() })
            put("billing", customer.billing.toJSON())
            put("shipping", customer.shipping.toJSON())
            put("set_paid", event.price <= 0.0)
            put("line_items", JSONArray().apply { put(item) })
        }
        Logger.d("Making POST request to $ordersEndpoint with: $body")
        val response = post(ordersEndpoint, body)
        val json = JSONObject(response)
        val responseOrderProto = OrderProto.fromJSON(json)
        return json.getString("payment_url") to responseOrderProto as Order
    }

    /**
     * Deletes the order with the given ID.
     */
    suspend fun eventCancel(orderId: Long) {
        val endpoint = ordersEndpoint.buildUpon()
            .appendPath(orderId.toString())
            .build()
        delete(endpoint)
    }
}
