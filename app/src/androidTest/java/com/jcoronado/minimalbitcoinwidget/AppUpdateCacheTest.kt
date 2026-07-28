package com.jcoronado.minimalbitcoinwidget

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented unit tests for [Prefs.checkAppUpdateAndInvalidateCache].
 * Tests all upgrade scenarios including legacy pre-v3.0.0 builds, modern v3.x updates,
 * fresh installs, and same-version reopens.
 */
@RunWith(AndroidJUnit4::class)
class AppUpdateCacheTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        // Clear all preferences before each test
        prefs.edit(commit = true) { clear() }
    }

    @Test
    fun upgradeFromLegacyVersionCode11_invalidatesPriceCacheAndUpdatesVersionCode() {
        // Given: A legacy version code (11 = v2.6.1) with existing cached data
        prefs.edit(commit = true) {
            putInt(Prefs.LAST_VERSION_CODE, 11)
            putLong(Prefs.LAST_API_CALL_TIMESTAMP, 123456789L)
            putString(Prefs.CACHED_PRICE_DATA, """{"currentPrice":95000.0}""")
        }

        // When: App update check is executed
        Prefs.checkAppUpdateAndInvalidateCache(prefs)

        // Then: Price cache is invalidated and version code is updated to current
        assertEquals(0L, prefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, -1L))
        assertNull(prefs.getString(Prefs.CACHED_PRICE_DATA, null))
        assertEquals(BuildConfig.VERSION_CODE, prefs.getInt(Prefs.LAST_VERSION_CODE, 0))
    }

    @Test
    fun upgradeFromLegacyVersionBoundaryCode1_invalidatesPriceCacheAndUpdatesVersionCode() {
        // Given: The lowest legacy version code boundary (1)
        prefs.edit(commit = true) {
            putInt(Prefs.LAST_VERSION_CODE, 1)
            putLong(Prefs.LAST_API_CALL_TIMESTAMP, 123456789L)
            putString(Prefs.CACHED_PRICE_DATA, """{"currentPrice":95000.0}""")
        }

        // When
        Prefs.checkAppUpdateAndInvalidateCache(prefs)

        // Then
        assertEquals(0L, prefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, -1L))
        assertNull(prefs.getString(Prefs.CACHED_PRICE_DATA, null))
        assertEquals(BuildConfig.VERSION_CODE, prefs.getInt(Prefs.LAST_VERSION_CODE, 0))
    }

    @Test
    fun upgradeFromLegacyVersionPre300_whereLastVersionCodeNotSaved_invalidatesPriceCacheAndUpdatesVersionCode() {
        // Given: Pre-v3.0.0 install where LAST_VERSION_CODE was never saved (defaults to 0),
        // but legacy preferences (LAST_API_CALL_TIMESTAMP) exist from previous app runs.
        prefs.edit(commit = true) {
            putLong(Prefs.LAST_API_CALL_TIMESTAMP, 987654321L)
            putString(Prefs.CACHED_PRICE_DATA, """{"currentPrice":50000.0}""")
        }

        // When
        Prefs.checkAppUpdateAndInvalidateCache(prefs)

        // Then: Legacy upgrade is detected via existing prefs -> cache wiped & version saved
        assertEquals(0L, prefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, -1L))
        assertNull(prefs.getString(Prefs.CACHED_PRICE_DATA, null))
        assertEquals(BuildConfig.VERSION_CODE, prefs.getInt(Prefs.LAST_VERSION_CODE, 0))
    }

    @Test
    fun upgradeFromModernVersion300_preservesPriceCacheAndUpdatesVersionCode() {
        // Given: Upgrading from v3.0.0 (versionCode = 12)
        val validTimestamp = 555555555L
        val validCacheJson = """{"currentPrice":98000.0}"""
        prefs.edit(commit = true) {
            putInt(Prefs.LAST_VERSION_CODE, 12)
            putLong(Prefs.LAST_API_CALL_TIMESTAMP, validTimestamp)
            putString(Prefs.CACHED_PRICE_DATA, validCacheJson)
        }

        // When
        Prefs.checkAppUpdateAndInvalidateCache(prefs)

        // Then: Cache is preserved (not wiped) and version code is updated
        assertEquals(validTimestamp, prefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, 0L))
        assertEquals(validCacheJson, prefs.getString(Prefs.CACHED_PRICE_DATA, null))
        assertEquals(BuildConfig.VERSION_CODE, prefs.getInt(Prefs.LAST_VERSION_CODE, 0))
    }

    @Test
    fun upgradeFromModernVersion311_preservesPriceCacheAndUpdatesVersionCode() {
        // Given: Upgrading from v3.1.1 (versionCode = 14)
        val validTimestamp = 777777777L
        val validCacheJson = """{"currentPrice":100000.0}"""
        prefs.edit(commit = true) {
            putInt(Prefs.LAST_VERSION_CODE, 14)
            putLong(Prefs.LAST_API_CALL_TIMESTAMP, validTimestamp)
            putString(Prefs.CACHED_PRICE_DATA, validCacheJson)
        }

        // When
        Prefs.checkAppUpdateAndInvalidateCache(prefs)

        // Then: Cache is preserved and version code is updated to current
        assertEquals(validTimestamp, prefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, 0L))
        assertEquals(validCacheJson, prefs.getString(Prefs.CACHED_PRICE_DATA, null))
        assertEquals(BuildConfig.VERSION_CODE, prefs.getInt(Prefs.LAST_VERSION_CODE, 0))
    }

    @Test
    fun freshInstall_doesNotInvalidateCache_savesCurrentVersionCode() {
        // Given: Completely fresh install (empty preferences)
        assertFalse(prefs.contains(Prefs.LAST_VERSION_CODE))

        // When
        Prefs.checkAppUpdateAndInvalidateCache(prefs)

        // Then: Current version code is recorded, cache is not modified
        assertEquals(BuildConfig.VERSION_CODE, prefs.getInt(Prefs.LAST_VERSION_CODE, 0))
        assertNull(prefs.getString(Prefs.CACHED_PRICE_DATA, null))
    }

    @Test
    fun reopenAppOnSameVersion_doesNotModifyPreferences() {
        // Given: Already running the current version code
        val existingTimestamp = 888888888L
        val existingCache = """{"currentPrice":99000.0}"""
        prefs.edit(commit = true) {
            putInt(Prefs.LAST_VERSION_CODE, BuildConfig.VERSION_CODE)
            putLong(Prefs.LAST_API_CALL_TIMESTAMP, existingTimestamp)
            putString(Prefs.CACHED_PRICE_DATA, existingCache)
        }

        // When
        Prefs.checkAppUpdateAndInvalidateCache(prefs)

        // Then: Preferences remain unchanged
        assertEquals(BuildConfig.VERSION_CODE, prefs.getInt(Prefs.LAST_VERSION_CODE, 0))
        assertEquals(existingTimestamp, prefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, 0L))
        assertEquals(existingCache, prefs.getString(Prefs.CACHED_PRICE_DATA, null))
    }
}
