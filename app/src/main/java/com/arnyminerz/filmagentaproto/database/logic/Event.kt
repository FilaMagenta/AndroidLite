package com.arnyminerz.filmagentaproto.database.logic

import android.content.Context
import androidx.annotation.WorkerThread
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.local.AppDatabase

@WorkerThread
suspend fun Event.isConfirmed(context: Context, customer: Customer): Boolean =
    getProductOrNull(context, customer) != null

/**
 * Tries to get the [Event]'s matching [Order.Product]. If null, it means that the given [customer]
 * has not signed up for this event. Otherwise the returned product is the reservation made.
 */
@WorkerThread
suspend fun Event.getProductOrNull(context: Context, customer: Customer): Order.Product? {
    val entries = getOrderOrNull(context, customer)?.items
    return entries?.firstOrNull()
}

/**
 * Tries to get the [Event]'s matching [Order.Product]. If null, it means that the given [customer]
 * has not signed up for this event. Otherwise the returned product is the reservation made.
 */
@WorkerThread
suspend fun Event.getOrderOrNull(context: Context, customer: Customer): Order? {
    val database = AppDatabase.getInstance(context)
    val wooCommerceDao = database.wooCommerceDao()
    val orders = wooCommerceDao.getAllOrders()
    val entries = orders
        .filter { it.customerId == customer.id }
        .filter { order -> order.items.find { it.productId == id } != null }
    return entries.firstOrNull()
}

/**
 * Gets all the orders made for the given event.
 */
@WorkerThread
suspend fun Event.getOrders(context: Context): List<Order> {
    val database = AppDatabase.getInstance(context)
    val wooCommerceDao = database.wooCommerceDao()
    val orders = wooCommerceDao.getAllOrders()
    return orders
        .filter { order -> order.items.find { it.productId == id } != null }
}
