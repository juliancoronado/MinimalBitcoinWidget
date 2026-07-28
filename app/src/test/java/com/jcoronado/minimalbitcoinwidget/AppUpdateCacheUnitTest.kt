package com.jcoronado.minimalbitcoinwidget

import android.content.SharedPreferences
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * JVM Unit tests for [Prefs.checkAppUpdateAndInvalidateCache] running directly on local JVM.
 * Validates all 7 upgrade scenarios using an in-memory FakeSharedPreferences.
 */
class AppUpdateCacheUnitTest {

    private lateinit var fakePrefs: FakeSharedPreferences

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
    }

    @Test
    fun upgradeFromLegacyVersionCode11_invalidatesPriceCacheAndUpdatesVersionCode() {
        fakePrefs.putInt(Prefs.LAST_VERSION_CODE, 11)
        fakePrefs.putLong(Prefs.LAST_API_CALL_TIMESTAMP, 123456789L)
        fakePrefs.putString(Prefs.CACHED_PRICE_DATA, """{"currentPrice":95000.0}""")

        Prefs.checkAppUpdateAndInvalidateCache(fakePrefs)

        assertEquals(0L, fakePrefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, -1L))
        assertNull(fakePrefs.getString(Prefs.CACHED_PRICE_DATA, null))
        assertEquals(BuildConfig.VERSION_CODE, fakePrefs.getInt(Prefs.LAST_VERSION_CODE, 0))
    }

    @Test
    fun upgradeFromLegacyVersionBoundaryCode1_invalidatesPriceCacheAndUpdatesVersionCode() {
        fakePrefs.putInt(Prefs.LAST_VERSION_CODE, 1)
        fakePrefs.putLong(Prefs.LAST_API_CALL_TIMESTAMP, 123456789L)
        fakePrefs.putString(Prefs.CACHED_PRICE_DATA, """{"currentPrice":95000.0}""")

        Prefs.checkAppUpdateAndInvalidateCache(fakePrefs)

        assertEquals(0L, fakePrefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, -1L))
        assertNull(fakePrefs.getString(Prefs.CACHED_PRICE_DATA, null))
        assertEquals(BuildConfig.VERSION_CODE, fakePrefs.getInt(Prefs.LAST_VERSION_CODE, 0))
    }

    @Test
    fun upgradeFromLegacyVersionPre300_whereLastVersionCodeNotSaved_invalidatesPriceCacheAndUpdatesVersionCode() {
        fakePrefs.putLong(Prefs.LAST_API_CALL_TIMESTAMP, 987654321L)
        fakePrefs.putString(Prefs.CACHED_PRICE_DATA, """{"currentPrice":50000.0}""")

        Prefs.checkAppUpdateAndInvalidateCache(fakePrefs)

        assertEquals(0L, fakePrefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, -1L))
        assertNull(fakePrefs.getString(Prefs.CACHED_PRICE_DATA, null))
        assertEquals(BuildConfig.VERSION_CODE, fakePrefs.getInt(Prefs.LAST_VERSION_CODE, 0))
    }

    @Test
    fun upgradeFromModernVersion300_preservesPriceCacheAndUpdatesVersionCode() {
        val validTimestamp = 555555555L
        val validCacheJson = """{"currentPrice":98000.0}"""
        fakePrefs.putInt(Prefs.LAST_VERSION_CODE, 12)
        fakePrefs.putLong(Prefs.LAST_API_CALL_TIMESTAMP, validTimestamp)
        fakePrefs.putString(Prefs.CACHED_PRICE_DATA, validCacheJson)

        Prefs.checkAppUpdateAndInvalidateCache(fakePrefs)

        assertEquals(validTimestamp, fakePrefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, 0L))
        assertEquals(validCacheJson, fakePrefs.getString(Prefs.CACHED_PRICE_DATA, null))
        assertEquals(BuildConfig.VERSION_CODE, fakePrefs.getInt(Prefs.LAST_VERSION_CODE, 0))
    }

    @Test
    fun upgradeFromModernVersion311_preservesPriceCacheAndUpdatesVersionCode() {
        val validTimestamp = 777777777L
        val validCacheJson = """{"currentPrice":100000.0}"""
        fakePrefs.putInt(Prefs.LAST_VERSION_CODE, 14)
        fakePrefs.putLong(Prefs.LAST_API_CALL_TIMESTAMP, validTimestamp)
        fakePrefs.putString(Prefs.CACHED_PRICE_DATA, validCacheJson)

        Prefs.checkAppUpdateAndInvalidateCache(fakePrefs)

        assertEquals(validTimestamp, fakePrefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, 0L))
        assertEquals(validCacheJson, fakePrefs.getString(Prefs.CACHED_PRICE_DATA, null))
        assertEquals(BuildConfig.VERSION_CODE, fakePrefs.getInt(Prefs.LAST_VERSION_CODE, 0))
    }

    @Test
    fun freshInstall_doesNotInvalidateCache_savesCurrentVersionCode() {
        assertFalse(fakePrefs.contains(Prefs.LAST_VERSION_CODE))

        Prefs.checkAppUpdateAndInvalidateCache(fakePrefs)

        assertEquals(BuildConfig.VERSION_CODE, fakePrefs.getInt(Prefs.LAST_VERSION_CODE, 0))
        assertNull(fakePrefs.getString(Prefs.CACHED_PRICE_DATA, null))
    }

    @Test
    fun reopenAppOnSameVersion_doesNotModifyPreferences() {
        val existingTimestamp = 888888888L
        val existingCache = """{"currentPrice":99000.0}"""
        fakePrefs.putInt(Prefs.LAST_VERSION_CODE, BuildConfig.VERSION_CODE)
        fakePrefs.putLong(Prefs.LAST_API_CALL_TIMESTAMP, existingTimestamp)
        fakePrefs.putString(Prefs.CACHED_PRICE_DATA, existingCache)

        Prefs.checkAppUpdateAndInvalidateCache(fakePrefs)

        assertEquals(BuildConfig.VERSION_CODE, fakePrefs.getInt(Prefs.LAST_VERSION_CODE, 0))
        assertEquals(existingTimestamp, fakePrefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, 0L))
        assertEquals(existingCache, fakePrefs.getString(Prefs.CACHED_PRICE_DATA, null))
    }
}

/**
 * Lightweight, in-memory implementation of [SharedPreferences] for unit testing.
 */
class FakeSharedPreferences : SharedPreferences {
    private val map = mutableMapOf<String, Any?>()

    fun putInt(key: String, value: Int) { map[key] = value }
    fun putLong(key: String, value: Long) { map[key] = value }
    fun putString(key: String, value: String?) { map[key] = value }

    override fun getAll(): Map<String, *> = map
    override fun getString(key: String, defValue: String?): String? = map[key] as? String ?: defValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? = map[key] as? Set<String> ?: defValues
    override fun getInt(key: String, defValue: Int): Int = map[key] as? Int ?: defValue
    override fun getLong(key: String, defValue: Long): Long = map[key] as? Long ?: defValue
    override fun getFloat(key: String, defValue: Float): Float = map[key] as? Float ?: defValue
    override fun getBoolean(key: String, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
    override fun contains(key: String): Boolean = map.containsKey(key) && map[key] != null

    override fun edit(): SharedPreferences.Editor = EditorImpl()

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    private inner class EditorImpl : SharedPreferences.Editor {
        private val changes = mutableMapOf<String, Any?>()
        private val removes = mutableSetOf<String>()

        override fun putString(key: String, value: String?): SharedPreferences.Editor { changes[key] = value; return this }
        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor { changes[key] = values; return this }
        override fun putInt(key: String, value: Int): SharedPreferences.Editor { changes[key] = value; return this }
        override fun putLong(key: String, value: Long): SharedPreferences.Editor { changes[key] = value; return this }
        override fun putFloat(key: String, value: Float): SharedPreferences.Editor { changes[key] = value; return this }
        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor { changes[key] = value; return this }
        override fun remove(key: String): SharedPreferences.Editor { removes.add(key); return this }
        override fun clear(): SharedPreferences.Editor { map.clear(); return this }

        override fun apply() { commit() }
        override fun commit(): Boolean {
            removes.forEach { map.remove(it) }
            map.putAll(changes)
            return true
        }
    }
}
