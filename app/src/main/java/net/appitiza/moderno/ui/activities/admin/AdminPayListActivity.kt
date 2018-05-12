package net.appitiza.moderno.ui.activities.admin

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_admin_pay_list.*
import kotlinx.android.synthetic.main.activity_user_notifications.*
import net.appitiza.moderno.R
import net.appitiza.moderno.adapter.AdminPayAdapter
import net.appitiza.moderno.adapter.AdminSpnrUserAdapter
import net.appitiza.moderno.constants.Constants
import net.appitiza.moderno.model.AdminPayData
import net.appitiza.moderno.model.UserListdata
import net.appitiza.moderno.ui.activities.BaseActivity
import net.appitiza.moderno.ui.activities.interfaces.AdminPayClick
import net.appitiza.moderno.ui.activities.interfaces.UserClick
import net.appitiza.moderno.utils.PreferenceHelper
import net.appitiza.moderno.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

class AdminPayListActivity : BaseActivity(), AdminPayClick, UserClick {


    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mUserList: ArrayList<UserListdata>
    private lateinit var mPayList: ArrayList<AdminPayData>
    private lateinit var userAdapter: AdminSpnrUserAdapter
    private lateinit var payAdapter: AdminPayAdapter
    private var user: UserListdata? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_pay_list)
        initializeFireBase()
        setClick()
        getUser()


    }

    private fun initializeFireBase() {
        mUserList = arrayListOf()
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        mPayList = arrayListOf()
        payAdapter = AdminPayAdapter(mPayList, this)
        rv_pay_user_list.layoutManager = LinearLayoutManager(this)
        rv_pay_user_list.adapter = payAdapter
    }

    private fun setClick() {

        spnr_admin_pay_list_user.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                user = mUserList[position]
                loadPay()
            }

        }


    }

    private fun loadPay() {
        mPayList.clear()
        if (user!!.emailId != "all") {
            db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                    .whereEqualTo(Constants.CHECKIN_USEREMAIL, user!!.emailId)
                    .whereEqualTo(Constants.CHECKIN_SITE, "0")
                    .get()
                    .addOnCompleteListener { fetchall_task ->
                        mProgress?.dismiss()

                        if (fetchall_task.isSuccessful) {
                            var total_payment = 0
                            for (document in fetchall_task.result) {
                                val mData = AdminPayData()
                                mData.adminPayId = document.id
                                mData.payment = document.data[Constants.CHECKIN_PAYMENT].toString()
                                mData.user = document.data[Constants.CHECKIN_USEREMAIL].toString()
                                mData.username = document.data[Constants.CHECKIN_USERNAME].toString()
                                mData.time = getDate(document.data[Constants.CHECKIN_CHECKOUT].toString()).time.toLong()


                                if (!mData.payment.equals("null") && mData.payment.toString() != "") {
                                    val mPayment = Integer.parseInt(document.data[Constants.CHECKIN_PAYMENT].toString())
                                    total_payment += mPayment
                                }

                                mPayList.add(mData)

                            }
                            payAdapter.notifyDataSetChanged()
                            tv_pay_total.text = getString(R.string.rupees, total_payment)

                        } else {
                            Utils.showDialog(this, fetchall_task.exception.toString())
                            Log.e("With time", fetchall_task.exception.toString())
                        }
                    }

        }
        else
        {
            db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                    .whereEqualTo(Constants.CHECKIN_SITE, "0")
                    .get()
                    .addOnCompleteListener { fetchall_task ->
                        mProgress?.dismiss()

                        if (fetchall_task.isSuccessful) {
                            var total_payment = 0
                            for (document in fetchall_task.result) {
                                val mData = AdminPayData()
                                mData.adminPayId = document.id
                                mData.adminPayId = document.id
                                mData.payment = document.data[Constants.CHECKIN_PAYMENT].toString()
                                mData.user = document.data[Constants.CHECKIN_USEREMAIL].toString()
                                mData.username = document.data[Constants.CHECKIN_USERNAME].toString()
                                mData.time = getDate(document.data[Constants.CHECKIN_CHECKOUT].toString()).time.toLong()


                                if (!mData.payment.equals("null") && mData.payment.toString() != "") {
                                    val mPayment = Integer.parseInt(document.data[Constants.CHECKIN_PAYMENT].toString())
                                    total_payment += mPayment
                                }

                                mPayList.add(mData)

                            }
                            payAdapter.notifyDataSetChanged()
                            tv_pay_total.text = getString(R.string.rupees, total_payment)

                        } else {
                            Utils.showDialog(this, fetchall_task.exception.toString())
                            Log.e("With time", fetchall_task.exception.toString())
                        }
                    }
        }
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.fetching_data))
        mProgress?.setCancelable(false)
        mProgress?.show()
        mPayList.clear()


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
                        val data = UserListdata()
                        data.emailId = "all"
                        data.username = "All"
                        mUserList.add(data)

                        for (document in fetchall_task.result) {
                            // Log.d(FragmentActivity.TAG, document.id + " => " + document.getData())
                            val data = UserListdata()
                            data.emailId = document.data[Constants.USER_EMAIL].toString()
                            data.username = document.data[Constants.USER_DISPLAY_NAME].toString()
                            mUserList.add(data)

                        }

                        userAdapter = AdminSpnrUserAdapter(this, mUserList, this)
                        spnr_admin_pay_list_user.adapter = userAdapter
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this, fetchall_task.exception?.message.toString())
                    }
                }


    }
    private fun getDate(date: String): Date {
        val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val value: Date = format.parse(date)
        return value
    }
    override fun onClick(data: AdminPayData) {

    }

    override fun onUserClick(data: UserListdata) {

    }

}
