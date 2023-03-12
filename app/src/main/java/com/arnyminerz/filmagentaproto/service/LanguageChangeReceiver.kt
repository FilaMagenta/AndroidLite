package com.arnyminerz.filmagentaproto.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_LOCALE_LIST
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.filmagentaproto.utils.getParcelableExtraCompat

class LanguageChangeReceiver: BroadcastReceiver() {
    companion object {
        val currentLanguage = MutableLiveData(
            AppCompatDelegate.getApplicationLocales().toList()
        )

        private fun LocaleListCompat.toList() = (0 until size()).mapNotNull { get(it) }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val localesList = intent?.getParcelableExtraCompat(EXTRA_LOCALE_LIST, LocaleList::class) ?: return
        val locales = LocaleListCompat.wrap(localesList)

        currentLanguage.postValue(locales.toList())
    }
}