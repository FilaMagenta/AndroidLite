package com.arnyminerz.filamagenta.desktop.test

import com.arnyminerz.filamagenta.desktop.localization.Translations
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TestLocalization {
    @Before
    fun prepare_locale() {
        // Set the default locale to English
        Locale.setDefault(Locale.ENGLISH)
    }


    @Test
    fun test_load_notExists() {
        Translations.load("de")
        assertEquals(0, Translations.loadedLocales)
    }

    @Test
    fun test_load_invalid() {
        Translations.load("wrong")
        assertEquals(0, Translations.loadedLocales)
    }

    @Test
    fun test_load_correct() {
        Translations.load("en")
        assertEquals(1, Translations.loadedLocales)

        val test = Translations.get("test")
        assertEquals("Hello world!", test)
    }

    @Test
    fun test_load_invalidKey_noFallback() {
        Translations.load("en")
        assertEquals(1, Translations.loadedLocales)

        val correct = try {
            Translations.get("missing")
            false
        } catch (e: IllegalStateException) {
            true
        }
        assertTrue(correct)
    }

    @Test
    fun test_load_invalidKey_withFallback() {
        Translations.load("en")
        assertEquals(1, Translations.loadedLocales)

        Translations.setFallback("en")

        val correct = try {
            Translations.get("missing")
            false
        } catch (e: NullPointerException) {
            true
        }
        assertTrue(correct)
    }

    @Test
    fun test_no_load() {
        val correct = try {
            Translations.get("any")
            false
        } catch (e: UninitializedPropertyAccessException) {
            true
        }
        assertTrue(correct)
    }


    @After
    fun dispose_translations() {
        Translations.dispose()
    }
}
