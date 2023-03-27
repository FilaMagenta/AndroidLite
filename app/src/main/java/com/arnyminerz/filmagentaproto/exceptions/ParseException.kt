package com.arnyminerz.filmagentaproto.exceptions

class ParseException(html: String, exception: Exception) :
    RuntimeException("Could not parse the HTML source. Message (${exception.javaClass.simpleName}): ${exception.message}.\nHTML: $html")
