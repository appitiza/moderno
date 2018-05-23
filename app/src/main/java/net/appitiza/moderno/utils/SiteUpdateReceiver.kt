package net.appitiza.moderno.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SiteUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
            SiteUpdateObservable.getInstance().notifyChanges();
    }
}