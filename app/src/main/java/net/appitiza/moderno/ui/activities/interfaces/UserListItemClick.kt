package net.appitiza.moderno.ui.activities.interfaces

import net.appitiza.moderno.model.UserListdata

interface UserListItemClick {
    fun onDeleteClick(item : UserListdata)
}