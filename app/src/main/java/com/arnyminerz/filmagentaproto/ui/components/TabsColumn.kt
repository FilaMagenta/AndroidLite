package com.arnyminerz.filmagentaproto.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

/**
 * Provides a component with some tabs on the top, and a content that changes when each tab is
 * selected.
 * @param tabs The content of the tabs to display.
 * @param modifier Some modifiers to apply to the general column
 * @param content The content in each of the pages. Provides `page` for varying depending on page.
 */
@Composable
@ExperimentalFoundationApi
fun TabsColumn(
    tabs: Set<String>,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(page: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState { tabs.size }

    var selectedTabIndex by remember { mutableStateOf(0) }

    // Synchronize pager and tabs
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { selectedTabIndex = it }
    }
    LaunchedEffect(selectedTabIndex) {
        snapshotFlow { selectedTabIndex }
            .collect { scope.launch { pagerState.animateScrollToPage(it) } }
    }

    Column(modifier) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            for ((index, text) in tabs.withIndex())
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text) },
                )
        }
        HorizontalPager(
            state = pagerState,
        ) { page ->
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                content(this, page)
            }
        }
    }
}
