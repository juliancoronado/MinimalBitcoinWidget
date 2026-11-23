package com.jcoronado.minimalbitcoinwidget.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jcoronado.minimalbitcoinwidget.viewmodels.PriceViewModel
import com.jcoronado.minimalbitcoinwidget.workers.PriceUpdateWorker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "PackageUpdateReceiver"

/**
 * Listens for application update events (ACTION_MY_PACKAGE_REPLACED).
 *
 * When an APK update is installed, resource IDs in resources.arsc can shift, causing
 * the launcher's cached RemoteViews to temporarily reference incorrect drawables or layouts.
 *
 * This receiver intercepts the package replacement event and immediately re-renders all
 * active widget instances from the local offline cache without making any network calls,
 * replacing the stale RemoteViews before the user views their home screen.
 */
class PackageUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        Log.i(TAG, "Application package replaced. Refreshing widgets from cache.")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Immediately repaint all widgets using the new build's compiled resource IDs from disk cache (0 API calls)
                PriceViewModel.refreshWidgetsFromCacheSuspend(context)
            } finally {
                pendingResult.finish()
            }
        }

        // Ensure background WorkManager sync remains scheduled
        PriceUpdateWorker.enqueue(context)
    }
}
