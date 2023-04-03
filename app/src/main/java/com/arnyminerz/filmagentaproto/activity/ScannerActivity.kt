package com.arnyminerz.filmagentaproto.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.ui.screens.CameraPreview
import com.arnyminerz.filmagentaproto.ui.theme.setContentThemed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import timber.log.Timber

@OptIn(ExperimentalPermissionsApi::class)
@androidx.annotation.OptIn(ExperimentalGetImage::class)
class ScannerActivity : AppCompatActivity() {
    companion object {
        const val RESULT_CONTENT = "result"
    }

    /**
     * Creates a contract for scanning a QR code. The result might be null if there was an error
     * while scanning, or if the user cancelled the request. Otherwise contains a list with the
     * data of all the scanner QR codes.
     */
    object Contract : ActivityResultContract<Void?, List<String>?>() {
        override fun createIntent(context: Context, input: Void?): Intent =
            Intent(context, ScannerActivity::class.java)

        override fun parseResult(resultCode: Int, intent: Intent?): List<String>? {
            if (resultCode == Activity.RESULT_OK) {
                val result = intent?.getStringArrayExtra(RESULT_CONTENT)
                return result?.toList()
            }
            return null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentThemed {
            BackHandler {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }

            val cameraPermissionState = rememberPermissionState(
                android.Manifest.permission.CAMERA
            )

            if (cameraPermissionState.status.isGranted)
                CameraPreview(modifier = Modifier.fillMaxSize()) { result ->
                    Timber.d("Got result! List: $result")
                    setResult(
                        Activity.RESULT_OK,
                        Intent().apply { putExtra(RESULT_CONTENT, result.toTypedArray()) },
                    )
                    finish()
                }
            else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.permission_required_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .padding(horizontal = 8.dp),
                        )
                        Text(
                            text = stringResource(R.string.permission_required_camera),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .padding(horizontal = 8.dp),
                        )
                        OutlinedButton(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text(stringResource(R.string.permission_required_grant))
                        }
                    }
                }
            }
        }
    }
}