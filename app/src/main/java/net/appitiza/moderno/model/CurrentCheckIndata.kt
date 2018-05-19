package net.appitiza.moderno.model

import java.io.Serializable

class CurrentCheckIndata : Serializable {
    var documentid: String = ""
    var siteid: String? = ""
    var sitename: String? = null
    var checkintime: Long = 0
    var checkouttime: Long = 0
    var useremail: String? = null
    var username: String? = null
    var payment: String? = null
    var salary: Int = 0



}