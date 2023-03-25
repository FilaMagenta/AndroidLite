package com.arnyminerz.filmagentaproto.ui.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Verified
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.redsys.tpvvinapplibrary.ErrorResponse
import com.redsys.tpvvinapplibrary.ResultResponse

@Composable
@ExperimentalMaterial3Api
fun PaymentMadeBottomSheet(result: Pair<ResultResponse?, ErrorResponse?>, onDismissRequest: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        val (successResult, errorResult) = result

        when {
            successResult != null -> {
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
                    text = stringResource(R.string.payment_made_correct_title),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 32.dp),
                    fontSize = 26.sp,
                )
                Text(
                    text = stringResource(R.string.payment_made_correct_message),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 72.dp),
                    textAlign = TextAlign.Center,
                )
            }
            errorResult != null -> {
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
                    text = stringResource(R.string.payment_made_fail_title),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 32.dp),
                    fontSize = 26.sp,
                )
                Text(
                    text = stringResource(R.string.payment_made_fail_message, errorResult.code),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 72.dp)
                        .padding(horizontal = 12.dp),
                    textAlign = TextAlign.Center,
                )
            }
            else -> onDismissRequest()
        }
    }
}
