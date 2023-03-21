package com.arnyminerz.filmagentaproto.database.local

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.arnyminerz.filmagentaproto.database.data.woo.AvailablePayment
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order

@Dao
interface WooCommerceDao {
    @WorkerThread
    @Query("SELECT * FROM events WHERE id=:id")
    suspend fun getEvent(id: Long): Event?

    @WorkerThread
    @Query("SELECT * FROM events")
    suspend fun getAllEvents(): List<Event>

    @Query("SELECT * FROM events")
    fun getAllEventsLive(): LiveData<List<Event>>

    @Insert
    suspend fun insert(vararg data: Event)

    @Delete
    suspend fun delete(vararg data: Event)

    @Query("DELETE FROM events WHERE id=:id")
    suspend fun deleteEvent(id: Long)

    @Update
    suspend fun update(vararg data: Event)


    @WorkerThread
    @Query("SELECT * FROM orders")
    suspend fun getAllOrders(): List<Order>

    @Query("SELECT * FROM orders")
    fun getAllOrdersLive(): LiveData<List<Order>>

    @Insert
    suspend fun insert(vararg data: Order)

    @Delete
    suspend fun delete(vararg data: Order)

    @Update
    suspend fun update(vararg data: Order)



    @WorkerThread
    @Query("SELECT * FROM customers WHERE id=:id")
    suspend fun getCustomer(id: Long): Customer?

    @WorkerThread
    @Query("SELECT * FROM customers")
    suspend fun getAllCustomers(): List<Customer>

    @Insert
    suspend fun insert(vararg data: Customer)

    @Delete
    suspend fun delete(vararg data: Customer)

    @Update
    suspend fun update(vararg data: Customer)



    @WorkerThread
    @Query("SELECT * FROM available_payments")
    suspend fun getAllAvailablePayments(): List<AvailablePayment>

    @Insert
    suspend fun insert(vararg data: AvailablePayment)

    @Delete
    suspend fun delete(vararg data: AvailablePayment)

    @Update
    suspend fun update(vararg data: AvailablePayment)

}