package com.arnyminerz.filmagentaproto.security

import androidx.annotation.StringRes
import com.arnyminerz.filamagenta.core.security.PasswordSafety
import com.arnyminerz.filmagentaproto.R

val PasswordSafety.labelRes: Int
    @StringRes
    get() = when (this) {
        PasswordSafety.Magenta -> R.string.register_password_unsafe_magenta
        PasswordSafety.Short -> R.string.register_password_unsafe_short
        PasswordSafety.AllCaps -> R.string.register_password_unsafe_all_caps
        PasswordSafety.AllLowercase -> R.string.register_password_unsafe_all_lowe
        PasswordSafety.AllNumbers -> R.string.register_password_unsafe_all_numb
        PasswordSafety.Safe -> R.string.register_password_safe
    }
