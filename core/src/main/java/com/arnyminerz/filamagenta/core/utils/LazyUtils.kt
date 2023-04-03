package com.arnyminerz.filamagenta.core.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("ClassName")
class lazyNullCacheable<T>(val initializer: () -> T) : ReadWriteProperty<Any?, T> {
    private object UNINITIALIZED_VALUE
    private var prop: Any? = UNINITIALIZED_VALUE

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return if (prop == UNINITIALIZED_VALUE || prop == null) {
            synchronized(this) {
                initializer().also { prop = it }
            }
        } else prop as T
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        synchronized(this) {
            prop = value
        }
    }
}
