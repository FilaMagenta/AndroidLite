package com.arnyminerz.filmagentaproto.utils

import android.net.Uri
import java.net.URI

fun Uri.toURI(): URI = URI.create(toString())
