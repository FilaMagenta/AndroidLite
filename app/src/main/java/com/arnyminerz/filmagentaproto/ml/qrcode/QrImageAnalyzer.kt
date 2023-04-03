package com.arnyminerz.filmagentaproto.ml.qrcode

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@ExperimentalGetImage
class QrImageAnalyzer(
    private val onScannedListener: (result: List<Barcode>) -> Unit,
    private val onFailureListener: (error: Exception) -> Unit,
): ImageAnalysis.Analyzer {
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            BarcodeScanning.getClient(options)
                .process(image)
                .addOnSuccessListener(onScannedListener)
                .addOnFailureListener(onFailureListener)
                .addOnCompleteListener {
                    // Close the proxy being used
                    imageProxy.close()
                }
        }
    }
}
