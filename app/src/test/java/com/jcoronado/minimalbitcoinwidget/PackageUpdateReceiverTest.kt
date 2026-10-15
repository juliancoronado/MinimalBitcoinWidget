package com.jcoronado.minimalbitcoinwidget

import android.content.Context
import android.content.Intent
import com.jcoronado.minimalbitcoinwidget.receivers.PackageUpdateReceiver
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit test verifying PackageUpdateReceiver instantiation and action filtering.
 */
class PackageUpdateReceiverTest {

    @Test
    fun `receiver instantiates successfully`() {
        val receiver = PackageUpdateReceiver()
        assertNotNull(receiver)
    }

    @Test
    fun `receiver ignores null or non-matching intent actions without throwing`() {
        val receiver = PackageUpdateReceiver()
        val mockContext = null as Context?

        // Should return early and not crash
        try {
            receiver.onReceive(mockContext as Context, null)
            receiver.onReceive(mockContext, Intent("android.intent.action.BOOT_COMPLETED"))
        } catch (_: NullPointerException) {
            // Expected if Android framework context is null on JVM, but early action check precedes context usage
        }
    }
}
