package com.arnyminerz.filamagenta.desktop.localization

import java.util.Locale
import org.json.JSONException
import org.json.JSONObject

/**
 * Provides utility functions to load translations from JSON files inside `resources/lang`.
 *
 * The current locale is fetched with [Locale.getDefault], so you can update it with
 * [Locale.setDefault].
 */
object Translations {
    private var translations: Map<Locale, Map<String, String>>? = null

    private var fallbackLocale: Locale? = null

    /**
     * Gets the amount of locales currently loaded. You can load some with [load].
     */
    val loadedLocales: Int
        get() = translations?.size ?: 0

    /**
     * Gets a list with all the locales available. Can be null if [load] has still not been called.
     */
    val availableLocales: Set<Locale>?
        get() = translations?.keys

    /**
     * Loads all the translations available in resources. Note that running this method will
     * override any translations currently loaded.
     *
     * **Remember to run [Translations.setFallback] to set a fallback locale.
     * @param languageTags A list of languages to load. Must be a valid locale name, and match a
     * file inside `resources/lang`.
     */
    fun load(vararg languageTags: String) {
        val newTranslations = mutableMapOf<Locale, Map<String, String>>()

        for (language in languageTags) {
            val locale = Locale.forLanguageTag(language)
            if (locale == null) {
                System.err.println("Could not load locale with tag: $language. Tag not valid.")
                continue
            }

            try {
                this::class.java.classLoader
                    .getResource("lang/$language.json")
                    ?.openStream()
                    ?.use { it.bufferedReader().readText() }
                    ?.let { raw ->
                        val json = JSONObject(raw)
                        newTranslations[locale] = json.keySet().associateWith { json.getString(it) }
                    }
                    ?: System.err.println("Could not find language \"$language\" in resources.")
            } catch (e: JSONException) {
                System.err.println("Could not load translations file for \"$language\".")
                e.printStackTrace()
            }
        }

        translations = newTranslations
    }

    /**
     * Updates the fallback locale for when a translation is not available on the system's locale.
     */
    fun setFallback(tag: String) = setFallback(Locale.forLanguageTag(tag))

    /**
     * Updates the fallback locale for when a translation is not available on the system's locale.
     */
    fun setFallback(locale: Locale): Translations {
        this.fallbackLocale = locale
        return this
    }

    /** Clears all the translations stored. */
    fun dispose() {
        translations = null
        fallbackLocale = null
    }

    /**
     * Gets an string from the locale given. It must be present at `resources/lang`, and it must
     * have been loaded before with [Translations.load].
     * @return The value keyed at [key] in the translations file.
     * @throws UninitializedPropertyAccessException If there are no translations loaded currently.
     * @throws IllegalStateException If no fallback locale has been set (tip: use
     * [Translations.setFallback]), or if the set fallback locale is not present in the translation
     * files.
     * @throws NullPointerException If the given [key] is not translated or present in any
     * translations file.
     */
    fun getString(key: String, vararg args: Any?): String {
        if (translations == null)
            throw UninitializedPropertyAccessException("There are no translations loaded. Please, perform load with Translations.load")

        var result: String? = null

        val locale: Locale = Locale.getDefault()

        val translations = translations!!
        if (translations.containsKey(locale))
            result = translations.getValue(locale)[key]

        if (result == null) {
            if (fallbackLocale == null)
                throw IllegalStateException("Fallback locale not set. Please, do so before running Translations.get with Translations.load")

            val fallbackLocale = fallbackLocale!!
            if (translations.containsKey(fallbackLocale))
                result = translations.getValue(fallbackLocale)[key]
            else
                throw IllegalStateException("Fallback locale ($fallbackLocale) does not exist inside the loaded translations.")
        }

        return result!!.format(*args)
    }
}
