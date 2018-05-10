package net.appitiza.moderno.ui.activities.admin

import android.app.ProgressDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_admin_pay.*
import net.appitiza.moderno.R
import net.appitiza.moderno.adapter.AdminSpnrUserAdapter
import net.appitiza.moderno.adapter.AdminSprSiteAdapter
import net.appitiza.moderno.constants.Constants
import net.appitiza.moderno.model.CurrentCheckIndata
import net.appitiza.moderno.model.SiteListdata
import net.appitiza.moderno.model.UserListdata
import net.appitiza.moderno.ui.activities.BaseActivity
import net.appitiza.moderno.ui.activities.interfaces.UserClick
import net.appitiza.moderno.ui.activities.interfaces.UserSiteClick
import net.appitiza.moderno.utils.PreferenceHelper
import net.appitiza.moderno.utils.Utils
import java.util.*

class AdminPayActivity : BaseActivity(), UserClick, UserSiteClick {
    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mUserList: ArrayList<UserListdata>
    private lateinit var userAdapter: AdminSpnrUserAdapter
    private var user: UserListdata? = null

    private lateinit var mSiteList: ArrayList<SiteListdata>
    private lateinit var ciadapter: AdminSprSiteAdapter
    private lateinit var coadapter: AdminSprSiteAdapter
    private val mCheckInData: CurrentCheckIndata = CurrentCheckIndata()
    private var checkinSite: SiteListdata = SiteListdata()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_pay)
        initializeFireBase()
        getUser()
        getSites()
        setClick()
    }

    private fun initializeFireBase() {
        mUserList = arrayListOf()
        mSiteList = arrayListOf()
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun setClick() {
        tv_admin_submit_pay_user.setOnClickListener {
            if (TextUtils.isEmpty(mCheckInData.siteid)) {
                if (validate()) {
                    insertHistory()
                }
            } else {
                mProgress!!.hide()
                Utils.showDialog(this, "already checked in " + mCheckInData.checkintime + "  \nPlease check out")
            }

        }
        spnr_admin_pay_user_user.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                user = mUserList[position]
            }

        }

        spnr_admin_pay_user_site.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                checkinSite = mSiteList[position]
            }

        }
    }

    private fun getUser() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_site))
        mProgress?.setCancelable(false)
        mProgress?.show()

        db.collection(Constants.COLLECTION_USER)
                .whereEqualTo(Constants.USER_TYPE, "user")
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()
                    if (fetchall_task.isSuccessful) {

                        for (document in fetchall_task.result) {
                            // Log.d(FragmentActivity.TAG, document.id + " => " + document.getData())
                            val data = UserListdata()
                            data.emailId = document.data[Constants.USER_EMAIL].toString()
                            data.username = document.data[Constants.USER_DISPLAY_NAME].toString()
                            mUserList.add(data)

                        }

                        userAdapter = AdminSpnrUserAdapter(this, mUserList, this)
                        spnr_admin_pay_user_user.adapter = userAdapter
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this, fetchall_task.exception?.message.toString())
                    }
                }


    }

    private fun getSites() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_site))
        mProgress?.setCancelable(false)
        mProgress?.show()

        db.collection(Constants.COLLECTION_SITE)
                .whereEqualTo(Constants.SITE_STATUS, "undergoing")
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()
                    if (fetchall_task.isSuccessful) {
                        for (document in fetchall_task.result) {
                            // Log.d(FragmentActivity.TAG, document.id + " => " + document.getData())
                            val data: SiteListdata = SiteListdata()
                            data.siteid = document.id
                            data.sitename = document.data[Constants.SITE_NAME].toString()
                            data.type = document.data[Constants.SITE_TYPE].toString()
                            data.date = document.data[Constants.SITE_DATE].toString()
                            data.cost = document.data[Constants.SITE_COST].toString()
                            data.contact = document.data[Constants.SITE_CONTACT].toString()
                            data.person = document.data[Constants.SITE_PERSON].toString()
                            data.status = document.data[Constants.SITE_STATUS].toString()
                            data.lat = document.data[Constants.SITE_LAT].toString().toDouble()
                            data.lon = document.data[Constants.SITE_LON].toString().toDouble()
                            //  data.location = document.data[Constants.SITE_LOCATION]
                            mSiteList.add(data)

                        }
                        ciadapter = AdminSprSiteAdapter(this, mSiteList, this)
                        coadapter = AdminSprSiteAdapter(this, mSiteList, this)
                        spnr_admin_pay_user_site.adapter = ciadapter
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this, fetchall_task.exception?.message.toString())
                    }
                }


    }

    override fun onUserClick(data: UserListdata) {

    }

    override fun onSiteClick(data: SiteListdata) {

    }

    private fun insertHistory() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.syn))
        mProgress?.setCancelable(false)
        mProgress?.show()
        val map = HashMap<String, Any>()
        map[Constants.CHECKIN_SITE] = checkinSite.siteid.toString()
        map[Constants.CHECKIN_SITENAME] = checkinSite.sitename.toString()
        map[Constants.CHECKIN_CHECKIN] = FieldValue.serverTimestamp()
        map[Constants.CHECKIN_USEREMAIL] = user?.emailId.toString()
        map[Constants.CHECKIN_CHECKOUT] =  FieldValue.serverTimestamp()
        map[Constants.CHECKIN_PAYMENT] = et_admin_pay_user_payment.text.toString()
        map[Constants.CHECKIN_USERNAME] = user?.username.toString()
        map[Constants.CHECKIN_PAYMENT_TYPE] = Constants.ADMIN_PAYMENT
        db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                .add(map)
                .addOnSuccessListener { documentReference ->

                    et_admin_pay_user_payment.setText("")
                    mProgress!!.dismiss()
                    Utils.showDialog(this, getString(R.string.payment_added))

                }
                .addOnFailureListener { e ->
                    mProgress!!.dismiss()

                }
    }




    private fun validate(): Boolean {
         if (TextUtils.isEmpty(et_admin_pay_user_payment.text.toString())) {
            Utils.showDialog(this, getString(R.string.please_provide_payment_amount))
            return false
        } else {
            return true
        }

    }
}
