package com.arnyminerz.filmagentaproto.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
annotation class Base64Bitmap

/**
 * Encodes the given Bitmap into Base64.
 */
@Base64Bitmap
fun Bitmap.encodeBase64(flags: Int = Base64.NO_WRAP): String? {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, flags)
}

/**
 * Decodes a Base64 Bitmap String into a Bitmap.
 */
fun @Base64Bitmap String.decodeBitmapBase64(flags: Int = Base64.NO_WRAP): Bitmap {
    val bytes = Base64.decode(this, flags)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
