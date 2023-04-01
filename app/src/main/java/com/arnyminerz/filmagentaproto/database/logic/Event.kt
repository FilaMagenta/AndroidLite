package com.arnyminerz.filmagentaproto.database.logic

import android.content.Context
import androidx.annotation.WorkerThread
import com.arnyminerz.filamagenta.core.database.data.woo.EventProto
import com.arnyminerz.filamagenta.core.database.data.woo.OrderProto
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.local.AppDatabase

@WorkerThread
suspend fun EventProto.isConfirmed(context: Context, customer: Customer): Boolean =
    getProductOrNull(context, customer) != null

/**
 * Tries to get the [EventProto]'s matching [OrderProto.Product]. If null, it means that the given [customer]
 * has not signed up for this event. Otherwise the returned product is the reservation made.
 */
@WorkerThread
suspend fun EventProto.getProductOrNull(context: Context, customer: Customer): OrderProto.Product? {
    val entries = getOrderOrNull(context, customer)?.items
    return entries?.firstOrNull()
}

/**
 * Tries to get the [EventProto]'s matching [OrderProto.Product]. If null, it means that the given [customer]
 * has not signed up for this event. Otherwise the returned product is the reservation made.
 */
@WorkerThread
suspend fun EventProto.getOrderOrNull(context: Context, customer: Customer): Order? {
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
suspend fun EventProto.getOrders(context: Context): List<Order> {
    val database = AppDatabase.getInstance(context)
    val wooCommerceDao = database.wooCommerceDao()
    val orders = wooCommerceDao.getAllOrders()
    return orders
        .filter { order -> order.items.find { it.productId == id } != null }
}
