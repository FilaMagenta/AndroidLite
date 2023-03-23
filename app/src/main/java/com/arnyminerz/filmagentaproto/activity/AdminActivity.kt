package com.arnyminerz.filmagentaproto.activity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.arnyminerz.filmagentaproto.App
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.data.woo.ROLE_ADMINISTRATOR
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.logic.getOrders
import com.arnyminerz.filmagentaproto.ui.components.NavigationBarItem
import com.arnyminerz.filmagentaproto.ui.components.NavigationBarItems
import com.arnyminerz.filmagentaproto.ui.components.Tooltip
import com.arnyminerz.filmagentaproto.ui.screens.admin.EventsAdminScreen
import com.arnyminerz.filmagentaproto.ui.theme.setContentThemed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
class AdminActivity : AppCompatActivity() {
    companion object {
        /**
         * Stores if the intent that launched the Activity comes from another activity, or from
         * a shortcut.
         */
        private const val EXTRA_PARENT_ACTIVITY = "parent"
    }

    object Contract : ActivityResultContract<Void?, Void?>() {
        override fun createIntent(context: Context, input: Void?): Intent =
            Intent(context, AdminActivity::class.java).apply {
                putExtra(EXTRA_PARENT_ACTIVITY, true)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Void? = null
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val comesFromActivity = intent.getBooleanExtra(EXTRA_PARENT_ACTIVITY, false)

        setContentThemed {
            val scope = rememberCoroutineScope()

            // Handle back presses
            BackHandler { setResult(Activity.RESULT_CANCELED); finish() }

            // Store the current page selected
            val pagerState = rememberPagerState()

            val customer by viewModel.customer.observeAsState()
            LaunchedEffect(customer) {
                snapshotFlow { customer }
                    .filterNotNull()
                    .collect {
                        if (it.role != ROLE_ADMINISTRATOR) {
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }
                    }
            }

            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.admin_title)) },
                        navigationIcon = {
                            if (comesFromActivity)
                                IconButton(
                                    onClick = { setResult(Activity.RESULT_CANCELED); finish() },
                                ) {
                                    Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.back))
                                }
                        },
                        actions = {
                            Box {
                                val showTooltip = remember { mutableStateOf(false) }

                                // Buttons and Surfaces don't support onLongClick out of the box,
                                // so use a simple Box with combinedClickable
                                Box(
                                    modifier = Modifier
                                        .combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = rememberRipple(),
                                            onClickLabel = "Button action description",
                                            role = Role.Button,
                                            onClick = { /* TODO: onClick */ },
                                            onLongClick = { showTooltip.value = true },
                                        ),
                                ) {
                                    Icon(Icons.Rounded.QrCodeScanner, "")
                                }

                                Tooltip(showTooltip) {
                                    // Tooltip content goes here.
                                    Text("Scan Code")
                                }
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItems(
                            selectedIndex = pagerState.currentPage,
                            onSelected = { scope.launch { pagerState.animateScrollToPage(it) } },
                            items = listOf(
                                NavigationBarItem(
                                    selectedIcon = Icons.Outlined.CalendarMonth,
                                    unselectedIcon = Icons.Filled.CalendarMonth,
                                    label = R.string.navigation_events,
                                )
                            ),
                        )
                    }
                },
            ) { paddingValues ->
                val events by viewModel.events.observeAsState()
                val customers by viewModel.customers.observeAsState(emptyList())

                HorizontalPager(pageCount = 1) { page ->
                    Column(
                        Modifier
                            .padding(paddingValues)
                            .padding(8.dp)
                    ) {
                        when (page) {
                            0 -> EventsAdminScreen(events, customers)
                        }
                    }
                }
            }
        }
    }

    class ViewModel(application: Application) : AndroidViewModel(application) {
        private val database = AppDatabase.getInstance(application)
        private val wooCommerceDao = database.wooCommerceDao()

        val customer = (application as App).customer

        val events = wooCommerceDao.getAllEventsLive().switchMap { events ->
            eventsOrdersLive(events)
        }

        val customers = wooCommerceDao.getAllCustomersLive()

        private fun eventsOrdersLive(events: List<Event>): LiveData<List<Pair<Event, List<Order>>>> =
            MutableLiveData<List<Pair<Event, List<Order>>>>().apply {
                viewModelScope.launch(Dispatchers.IO) {
                    val result = mutableListOf<Pair<Event, List<Order>>>()
                    for (event in events) {
                        val orders = event.getOrders(getApplication())
                            .map { order ->
                                order.copy(items = order.items.filter { it.productId == event.id })
                            }
                        result.add(event to orders)
                    }
                    postValue(result)
                }
            }
    }
}