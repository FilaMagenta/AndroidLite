package com.arnyminerz.filmagentaproto.exceptions

import com.redsys.tpvvinapplibrary.ErrorResponse

class PaymentException(description: String, code: String): RuntimeException("Could not make payment. Error ($code): $description") {
    constructor(response: ErrorResponse): this(response.desc, response.code)
}
