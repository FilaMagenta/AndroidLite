package com.arnyminerz.filmagentaproto.ui.dialogs.admin

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.activity.AdminActivity

@Composable
@ExperimentalMaterial3Api
fun ScanResultBottomSheet(scanResult: Int, scanCustomer: String?, onDismissRequest: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        when (scanResult) {
            AdminActivity.SCAN_RESULT_LOADING -> {
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
                        .padding(top = 32.dp, bottom = 64.dp)
                )
            }
            AdminActivity.SCAN_RESULT_OK -> {
                Icon(
                    Icons.Outlined.Verified,
                    null,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(96.dp)
                        .padding(top = 32.dp),
                    tint = Color(0xff66ff66),
                )
                Text(
                    text = stringResource(R.string.admin_scan_correct),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 32.dp),
                    fontSize = 26.sp,
                )
                Text(
                    text = scanCustomer ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 64.dp),
                    textAlign = TextAlign.Center,
                )
            }
            AdminActivity.SCAN_RESULT_INVALID -> {
                Icon(
                    Icons.Outlined.Cancel,
                    null,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(96.dp)
                        .padding(top = 32.dp),
                    tint = Color(0xffff3333),
                )
                Text(
                    text = stringResource(R.string.admin_scan_invalid),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 64.dp),
                    fontSize = 26.sp,
                )
            }
        }
    }
}
