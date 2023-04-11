package com.arnyminerz.filamagenta.desktop.loaders

import com.arnyminerz.filamagenta.core.database.data.woo.Customer
import com.arnyminerz.filamagenta.desktop.remote.RemoteCommerce

class MainWindowLoader: Loader() {
    val customersCache = CachedList("customers", Customer.Companion)

    fun loadCustomers() = loadList(customersCache, null) { _, _ -> RemoteCommerce.customersList() }
}