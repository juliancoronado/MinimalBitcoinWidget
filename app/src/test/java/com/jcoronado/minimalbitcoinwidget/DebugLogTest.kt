package com.jcoronado.minimalbitcoinwidget

import org.junit.Assert.assertEquals
import org.junit.Test

class DebugLogTest {

    @Test
    fun debugLog_defaultType_isWidget() {
        val log = DebugLog(message = "Fetching data")
        assertEquals(DebugLog.TYPE_WIDGET, log.type)
        assertEquals("Fetching data", log.message)
    }

    @Test
    fun debugLog_customType_isApp() {
        val log = DebugLog(message = "Manual refresh requested", type = DebugLog.TYPE_APP)
        assertEquals(DebugLog.TYPE_APP, log.type)
        assertEquals("Manual refresh requested", log.message)
    }
}
