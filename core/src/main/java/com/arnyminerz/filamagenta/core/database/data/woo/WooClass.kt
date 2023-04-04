package com.arnyminerz.filamagenta.core.database.data.woo

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable

abstract class WooClass(
    open val id: Long
): JsonSerializable
