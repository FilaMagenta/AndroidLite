package com.arnyminerz.filmagentaproto.database.remote

import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.annotation.WorkerThread
import com.arnyminerz.filmagentaproto.BuildConfig
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.utils.io
import com.arnyminerz.filmagentaproto.utils.mapObjects
import com.arnyminerz.filmagentaproto.utils.toURL
import java.io.IOException
import javax.net.ssl.HttpsURLConnection
import org.json.JSONArray
import org.json.JSONException

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
        val raw = get(endpoint)
        val json = JSONArray(raw)
        return json.mapObjects { Event.fromJSON(it) }
    }

    @WorkerThread
    suspend fun orderList(): List<Order> {
        val raw = get(OrdersEndpoint)
        val json = JSONArray(raw)
        return json.mapObjects { Order.fromJSON(it) }
    }

    @WorkerThread
    suspend fun customersList(page: Int = 1): List<Customer> {
        Log.d(TAG, "Getting page $page of customers...")
        val endpoint = CustomersEndpoint.buildUpon()
            .appendQueryParameter("context", "view")
            .appendQueryParameter("page", "$page")
            .appendQueryParameter("per_page", "40")
            .appendQueryParameter("role", "all")
            .build()
        val raw = get(endpoint)
        val json = JSONArray(raw)
        val objects = json.mapObjects { Customer.fromJSON(it) }.toMutableList()
        if (objects.size >= 10)
            objects.addAll(customersList(page + 1))
        return objects
    }
}
