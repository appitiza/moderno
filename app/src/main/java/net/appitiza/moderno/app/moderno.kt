package net.appitiza.moderno.app

import android.app.Application
import net.appitiza.moderno.constants.Constants
import net.appitiza.moderno.utils.PreferenceHelper

class Moderno : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferenceHelper.init(this, Constants.PREF_NAME)
    }
}