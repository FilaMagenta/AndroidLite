package com.arnyminerz.filmagentaproto.database.data.woo

import androidx.annotation.StringDef
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.filamagenta.core.database.data.woo.CustomerProto
import com.arnyminerz.filamagenta.core.database.data.woo.ROLE_ADMINISTRATOR
import com.arnyminerz.filamagenta.core.database.data.woo.ROLE_AUTHOR
import com.arnyminerz.filamagenta.core.database.data.woo.ROLE_CONTRIBUTOR
import com.arnyminerz.filamagenta.core.database.data.woo.ROLE_CUSTOMER
import com.arnyminerz.filamagenta.core.database.data.woo.ROLE_EDITOR
import com.arnyminerz.filamagenta.core.database.data.woo.ROLE_SHOP_MANAGER
import com.arnyminerz.filamagenta.core.database.data.woo.ROLE_SUBSCRIBER
import java.util.Date

@StringDef(
    ROLE_ADMINISTRATOR, ROLE_EDITOR, ROLE_AUTHOR, ROLE_CONTRIBUTOR, ROLE_SUBSCRIBER,
    ROLE_CUSTOMER, ROLE_SHOP_MANAGER
)
annotation class CustomerRole

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey override val id: Long,
    override val dateCreated: Date,
    override val dateModified: Date,
    override val email: String,
    override val firstName: String,
    override val lastName: String,
    @CustomerRole override val role: String,
    override val username: String,
    override val billing: DeliveryInformation,
    override val shipping: DeliveryInformation,
    override val isPayingCustomer: Boolean,
    override val avatarUrl: String,
): CustomerProto(id, dateCreated, dateModified, email, firstName, lastName, role, username, billing, shipping, isPayingCustomer, avatarUrl)