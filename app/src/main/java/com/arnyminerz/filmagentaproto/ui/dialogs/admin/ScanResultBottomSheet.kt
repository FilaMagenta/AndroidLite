package com.arnyminerz.filmagentaproto.ui.dialogs.admin

import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.activity.AdminEventActivity.Companion.SCAN_RESULT_FAIL
import com.arnyminerz.filmagentaproto.activity.AdminEventActivity.Companion.SCAN_RESULT_INVALID
import com.arnyminerz.filmagentaproto.activity.AdminEventActivity.Companion.SCAN_RESULT_LOADING
import com.arnyminerz.filmagentaproto.activity.AdminEventActivity.Companion.SCAN_RESULT_OK
import com.arnyminerz.filmagentaproto.activity.AdminEventActivity.Companion.SCAN_RESULT_OLD
import com.arnyminerz.filmagentaproto.activity.AdminEventActivity.Companion.SCAN_RESULT_REPEATED
import com.arnyminerz.filmagentaproto.ui.theme.SuccessColor
import com.arnyminerz.filmagentaproto.ui.theme.WarningColor

private val BOTTOM_MARGIN = 72.dp
private val HORIZONTAL_MARGIN = 12.dp

private const val LEVEL_SUCCESS = 0
private const val LEVEL_WARNING = 1
private const val LEVEL_ERROR = 2

@IntDef(LEVEL_SUCCESS, LEVEL_WARNING, LEVEL_ERROR)
annotation class ResponseLevel

@Composable
fun ColumnScope.ResponseView(
    @ResponseLevel level: Int,
    @StringRes titleRes: Int,
    @StringRes messageRes: Int? = null,
    message: String? = null,
) {
    Icon(
        when (level) {
            LEVEL_SUCCESS -> Icons.Outlined.Verified
            LEVEL_WARNING -> Icons.Outlined.NewReleases
            LEVEL_ERROR -> Icons.Outlined.Cancel
            else -> Icons.Outlined.Close
        },
        null,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .size(96.dp)
            .padding(top = 32.dp),
        tint = when (level) {
            LEVEL_SUCCESS -> SuccessColor
            LEVEL_WARNING -> WarningColor
            LEVEL_ERROR -> MaterialTheme.colorScheme.error
            else -> Color.Unspecified
        },
    )
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(
                bottom = BOTTOM_MARGIN.takeIf { message == null && messageRes == null } ?: 4.dp
            )
            .padding(horizontal = HORIZONTAL_MARGIN),
        fontSize = 26.sp,
    )
    message?.let { msg ->
        Text(
            text = msg,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = BOTTOM_MARGIN.takeIf { messageRes == null } ?: 4.dp)
                .padding(horizontal = HORIZONTAL_MARGIN),
            textAlign = TextAlign.Center,
        )
    }
    messageRes?.let { msgRes ->
        Text(
            text = stringResource(msgRes),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = BOTTOM_MARGIN)
                .padding(horizontal = HORIZONTAL_MARGIN),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
@ExperimentalMaterial3Api
fun ScanResultBottomSheet(scanResult: Int, scanCustomer: String?, onDismissRequest: () -> Unit) {
    LaunchedEffect(scanResult) {
        // Dismiss bottom sheet if result is SCAN_RESULT_FAIL
        if (scanResult == SCAN_RESULT_FAIL) onDismissRequest()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        when (scanResult) {
            SCAN_RESULT_LOADING -> {
                Text(
                    text = stringResource(R.string.admin_scan_loading),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                )
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 32.dp, bottom = BOTTOM_MARGIN)
                )
            }
            SCAN_RESULT_OK -> {
                ResponseView(
                    level = LEVEL_SUCCESS,
                    titleRes = R.string.admin_scan_correct,
                    message = scanCustomer,
                )
            }
            SCAN_RESULT_REPEATED -> {
                ResponseView(
                    level = LEVEL_WARNING,
                    titleRes = R.string.admin_scan_repeated,
                    message = scanCustomer,
                    messageRes = R.string.admin_scan_repeated_msg,
                )
            }
            SCAN_RESULT_INVALID -> {
                ResponseView(
                    level = LEVEL_ERROR,
                    titleRes = R.string.admin_scan_repeated,
                    messageRes = R.string.admin_scan_invalid,
                )
            }
            SCAN_RESULT_OLD -> {
                ResponseView(
                    level = LEVEL_ERROR,
                    titleRes = R.string.admin_scan_old,
                    messageRes = R.string.admin_scan_old_msg,
                )
            }
        }
    }
}
