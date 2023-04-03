package com.arnyminerz.filmagentaproto.viewmodel

import android.accounts.AccountManager
import android.app.Application
import androidx.annotation.UiThread
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import com.arnyminerz.filamagenta.core.data.Transaction
import com.arnyminerz.filamagenta.core.database.data.woo.Customer
import com.arnyminerz.filamagenta.core.database.data.woo.Event
import com.arnyminerz.filamagenta.core.database.data.woo.order.OrderMetadata
import com.arnyminerz.filamagenta.core.utils.io
import com.arnyminerz.filamagenta.core.utils.ui
import com.arnyminerz.filmagentaproto.App
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.logic.isConfirmed
import com.arnyminerz.filmagentaproto.database.remote.RemoteCommerce
import com.arnyminerz.filmagentaproto.database.remote.protos.Socio
import com.arnyminerz.filmagentaproto.storage.SELECTED_ACCOUNT
import com.arnyminerz.filmagentaproto.storage.dataStore
import com.arnyminerz.filmagentaproto.utils.async
import com.arnyminerz.filmagentaproto.worker.SyncWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import timber.log.Timber

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val transactionsDao = database.transactionsDao()
    private val remoteDatabaseDao = database.remoteDatabaseDao()
    private val wooCommerceDao = database.wooCommerceDao()

    private val am = AccountManager.get(application)

    val selectedAccount = application
        .dataStore
        .data
        .map { preferences -> preferences[SELECTED_ACCOUNT] ?: 0 }

    val accounts = (application as App).accounts

    @OptIn(ExperimentalCoroutinesApi::class)
    val account = selectedAccount.flatMapLatest { i ->
        accounts.map { list -> list.getOrNull(i) }.asFlow()
    }

    val idSocio = account.map { am.getUserData(it, Authenticator.USER_DATA_ID_SOCIO)?.toLongOrNull() }

    val isAdmin = account.map { am.getUserData(it, Authenticator.USER_DATA_CUSTOMER_ADMIN)?.toBoolean() }

    val databaseData = remoteDatabaseDao.getAllLive()

    val workerState = SyncWorker.getLiveStates(application)

    val associatedAccountsTransactions = MutableLiveData<Map<Socio, List<Transaction>>>()

    val customer = selectedAccount
        .map { index ->
            accounts.value?.get(index)?.let { account ->
                val customerId: Long = am.getUserData(account, "customer_id")
                    ?.toLongOrNull() ?: return@let null
                wooCommerceDao.getAllCustomers().find { it.id == customerId }
            }
        }

    val events = wooCommerceDao.getAllEventsLive()

    val orders = wooCommerceDao.getAllOrdersLive()

    val confirmedEvents = MutableLiveData<List<Event>>()
    val availableEvents = MutableLiveData<List<Event>>()

    val processingPayment = MutableLiveData(false)

    fun getAssociatedAccounts(associatedWithId: Long) = async {
        val socios = remoteDatabaseDao.getAllAssociatedWith(associatedWithId)
        val personalDataList = socios.associateWith { socio ->
            transactionsDao.getByIdSocio(socio.idSocio)
        }
        Timber.i("Got ${socios.size} associated accounts for #$associatedWithId")
        associatedAccountsTransactions.postValue(personalDataList)
    }

    private suspend fun isConfirmed(event: Event, customer: Customer?): Boolean {
        if (customer == null) return false
        return event.isConfirmed(getApplication(), customer)
    }

    suspend fun updateConfirmedEvents(customer: Customer?) {
        if (customer == null) return
        val events = events.value ?: return
        val (confirmed, available) = io {
            val confirmed = mutableMapOf<Event, Boolean>()
            events.forEach { event ->
                confirmed[event] = isConfirmed(event, customer)
            }
            val confirmedEvents = confirmed.filter { it.value }.keys.toList()
            val availableEvents = confirmed.filter { !it.value }.keys.toList()
            confirmedEvents to availableEvents
        }
        confirmedEvents.postValue(confirmed)
        availableEvents.postValue(available)
    }

    @Deprecated("Use Redsys payment")
    fun makePayment(
        amount: Double,
        concept: String,
        @UiThread onComplete: (paymentUrl: String) -> Unit
    ) = async {
        val paymentUrl = try {
            processingPayment.postValue(true)
            Timber.d("Requesting a payment of $amount €. Getting customer...")
            val customer = customer.first()
                ?: throw IllegalStateException("Could not get current customer.")
            Timber.d("Customer ID for payment: ${customer.id}")
            val payments = wooCommerceDao.getAllAvailablePayments()
            Timber.d("Making request...")
            val url = RemoteCommerce.transferAmount(amount, concept, payments, customer)
            Timber.i("Payment url: $url")
            url
        } catch (e: Exception) {
            Timber.e("Could not make payment: ${e.message}")
            null
        } finally {
            processingPayment.postValue(false)
        } ?: return@async
        ui { onComplete(paymentUrl) }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun signUpForEvent(
        customer: Customer,
        event: Event,
        metadata: List<OrderMetadata>,
        @UiThread onComplete: (paymentUrl: String) -> Unit
    ) = async {
        Timber.i("Signing up for event (price=${event.price}). Metadata: $metadata")
        val (paymentUrl, order) = RemoteCommerce.eventSignup(
            customer,
            "", // FIXME: Set notes
            event = event,
            metadata = metadata,
        )
        Timber.i("Adding order to database...")
        wooCommerceDao.insert(order)
        Timber.i("Event sign up is complete.")
        ui { onComplete(paymentUrl) }
    }

    fun deleteEvent(id: Long) = async {
        wooCommerceDao.deleteEvent(id)
    }

    /**
     * Sets the currently selected account index to `0`.
     */
    fun resetCurrentAccount() = async {
        getApplication<Application>().dataStore.edit {
            it[SELECTED_ACCOUNT] = 0
        }
    }
}
