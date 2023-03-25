package com.arnyminerz.filmagentaproto.ui.dialogs.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.utils.capitalizedWords

private const val SORT_BY_NAME = 0
private const val SORT_BY_ORDER = 1

@Deprecated("Use AdminEventActivity")
@Composable
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun PeopleListBottomSheet(
    peopleList: List<Order>,
    customers: List<Customer>,
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
    ) {
        var sortBy by remember { mutableStateOf(SORT_BY_NAME) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BottomSheetDefaults.ContainerColor)
                        .padding(horizontal = 8.dp),
                ) {
                    FilterChip(
                        selected = sortBy == SORT_BY_NAME,
                        onClick = { sortBy = SORT_BY_NAME },
                        label = { Text(stringResource(R.string.admin_events_sort_name)) },
                        leadingIcon = { Icon(Icons.Rounded.FontDownload, null) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                    FilterChip(
                        selected = sortBy == SORT_BY_ORDER,
                        onClick = { sortBy = SORT_BY_ORDER },
                        label = { Text(stringResource(R.string.admin_events_sort_order)) },
                        leadingIcon = { Icon(Icons.Rounded.Numbers, null) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
            items(
                peopleList
                    .map { order -> order to customers.find { it.id == order.customerId } }
                    .sortedBy { (order, customer) ->
                        when (sortBy) {
                            SORT_BY_NAME -> customer?.firstName
                            SORT_BY_ORDER -> "${order.id}"
                            else -> "#${order.id}"
                        }
                    }
            ) { (order, customer) ->
                val product = order.items.firstOrNull()

                ListItem(
                    headlineContent = {
                        Text(
                            text = customer?.let {
                                "${customer.firstName} ${customer.lastName}".lowercase()
                                    .capitalizedWords()
                            } ?: order.customerId.toString()
                        )
                    },
                    overlineContent = {
                        Text(stringResource(R.string.admin_events_order_number, order.id))
                    },
                    supportingContent = {
                        if (product != null)
                            Text("x ${product.quantity}, ${product.price} € (${product.variationId})")
                    }
                )
            }
        }
    }
}
