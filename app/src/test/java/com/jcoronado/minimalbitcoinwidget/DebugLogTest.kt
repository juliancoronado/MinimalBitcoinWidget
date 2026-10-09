package com.jcoronado.minimalbitcoinwidget

import org.junit.Assert.assertEquals
import org.junit.Test

class DebugLogTest {

    @Test
    fun debugLog_defaultType_isWidget() {
        val log = DebugLog(message = "PriceWorker: Fetching Data")
        assertEquals(DebugLog.TYPE_WIDGET, log.type)
        assertEquals("PriceWorker: Fetching Data", log.message)
    }

    @Test
    fun debugLog_customType_isApp() {
        val log = DebugLog(message = "App: Manual refresh requested", type = DebugLog.TYPE_APP)
        assertEquals(DebugLog.TYPE_APP, log.type)
        assertEquals("App: Manual refresh requested", log.message)
    }
}
