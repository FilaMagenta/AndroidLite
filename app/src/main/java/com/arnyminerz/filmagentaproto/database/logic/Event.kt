package com.arnyminerz.filmagentaproto.database.logic

import android.content.Context
import androidx.annotation.WorkerThread
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.local.AppDatabase

@WorkerThread
suspend fun Event.isConfirmed(context: Context, customer: Customer): Boolean {
    val database = AppDatabase.getInstance(context)
    val wooCommerceDao = database.wooCommerceDao()
    val orders = wooCommerceDao.getAllOrders()
    val entries = orders
        .filter { it.customerId == customer.id }
        .map { it.items }
        .mapNotNull { items -> items.find { it.productId == id } }
    return entries.isNotEmpty()
}
