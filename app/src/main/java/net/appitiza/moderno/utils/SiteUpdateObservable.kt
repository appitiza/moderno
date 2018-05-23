package net.appitiza.moderno.utils

import java.util.*

class SiteUpdateObservable : Observable() {


    companion object {
        var mCollaborationUpdateObservable: SiteUpdateObservable = SiteUpdateObservable()
        fun getInstance(): SiteUpdateObservable {
            mCollaborationUpdateObservable = SiteUpdateObservable()

            return mCollaborationUpdateObservable
        }

    }

    fun notifyChanges() {
        if (countObservers() > 0) {
            setChanged()
            notifyObservers()
        }
    }
}
