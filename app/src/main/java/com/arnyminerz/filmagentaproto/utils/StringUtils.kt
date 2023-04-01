package com.arnyminerz.filmagentaproto.utils

import android.net.Uri
import java.net.URL

fun Uri.toURL(): URL = URL(toString())
