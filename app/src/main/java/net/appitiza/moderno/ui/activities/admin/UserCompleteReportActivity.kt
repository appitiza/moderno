package net.appitiza.moderno.ui.activities.admin

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user_complete_report.*
import net.appitiza.moderno.R
import net.appitiza.moderno.adapter.AdminHistoryAdapter
import net.appitiza.moderno.adapter.AdminPayAdapter
import net.appitiza.moderno.adapter.AdminSpnrUserAdapter
import net.appitiza.moderno.constants.Constants
import net.appitiza.moderno.model.AdminPayData
import net.appitiza.moderno.model.CurrentCheckIndata
import net.appitiza.moderno.model.UserListdata
import net.appitiza.moderno.ui.activities.BaseActivity
import net.appitiza.moderno.ui.activities.interfaces.AdminPayClick
import net.appitiza.moderno.ui.activities.interfaces.AdminWorkHistoryClick
import net.appitiza.moderno.ui.activities.interfaces.UserClick
import net.appitiza.moderno.utils.PreferenceHelper
import net.appitiza.moderno.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

class UserCompleteReportActivity : BaseActivity(), UserClick, AdminWorkHistoryClick, AdminPayClick {


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


    private lateinit var mHistoryDisplay: ArrayList<CurrentCheckIndata>
    private lateinit var adapterMonthly: AdminHistoryAdapter
    private var userSalary: Int = 0

    private lateinit var mPayList: ArrayList<AdminPayData>
    private lateinit var payAdapter: AdminPayAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_complete_report)
        initializeFireBase()
        getUser()
        setClick()
    }

    private fun initializeFireBase() {
        mUserList = arrayListOf()
        rv_admin_transaction_history_checkin.layoutManager = LinearLayoutManager(this)
        mHistoryDisplay = arrayListOf()
        adapterMonthly = AdminHistoryAdapter(applicationContext, mHistoryDisplay, this)
        rv_admin_transaction_history_checkin.adapter = adapterMonthly

        mPayList = arrayListOf()
        payAdapter = AdminPayAdapter(mPayList, this)
        rv_admin_transaction_history_pay.layoutManager = LinearLayoutManager(this)
        rv_admin_transaction_history_pay.adapter = payAdapter


        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun setClick() {
        spnr_admin_transaction_history_user.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                user = mUserList[position]
                userSalary = mUserList[position].salary
                loadCheckInInfo()
            }

        }

    }

    private fun getUser() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_user_list))
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
                            data.salary = document.data[Constants.USER_SALARY].toString().toInt()
                            mUserList.add(data)

                        }

                        userAdapter = AdminSpnrUserAdapter(this, mUserList, this)
                        spnr_admin_transaction_history_user.adapter = userAdapter
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this, fetchall_task.exception.toString())

                    }
                }


    }

    private fun loadCheckInInfo() {

        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.fetching_data))
        mProgress?.setCancelable(false)
        mProgress?.show()

        mHistoryDisplay.clear()



        db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                .whereEqualTo(Constants.CHECKIN_USEREMAIL, user!!.emailId)
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()

                    if (fetchall_task.isSuccessful) {
                        var total_payment = 0
                        var total_hours: Long = 0
                        for (document in fetchall_task.result) {
                            if (document.data[Constants.CHECKIN_SITE].toString() != "0") {
                                val mCheckInData = CurrentCheckIndata()
                                mCheckInData.documentid = document.id
                                mCheckInData.siteid = document.data[Constants.CHECKIN_SITE].toString()
                                mCheckInData.sitename = document.data[Constants.CHECKIN_SITENAME].toString()
                                if (!TextUtils.isEmpty(document.data[Constants.CHECKIN_CHECKIN].toString()) && document.data[Constants.CHECKIN_CHECKIN].toString() != "null") {
                                    mCheckInData.checkintime = getDate(document.data[Constants.CHECKIN_CHECKIN].toString()).time
                                }
                                if (!TextUtils.isEmpty(document.data[Constants.CHECKIN_CHECKOUT].toString()) && document.data[Constants.CHECKIN_CHECKOUT].toString() != "null") {
                                    mCheckInData.checkouttime = getDate(document.data[Constants.CHECKIN_CHECKOUT].toString()).time
                                }
                                mCheckInData.useremail = document.data[Constants.CHECKIN_USEREMAIL].toString()
                                mCheckInData.username = document.data[Constants.CHECKIN_USERNAME].toString()
                                mCheckInData.payment = document.data[Constants.CHECKIN_PAYMENT].toString()
                                mCheckInData.salary = userSalary

                                if (document.data[Constants.CHECKIN_PAYMENT].toString() != "null" && document.data[Constants.CHECKIN_PAYMENT].toString() != "") {
                                    val mPayment = Integer.parseInt(document.data[Constants.CHECKIN_PAYMENT].toString())
                                    total_payment += mPayment
                                }
                                if (mCheckInData.checkintime != 0L) {
                                    if (mCheckInData.checkouttime != 0L) {
                                        val mHours = getDate(document.data[Constants.CHECKIN_CHECKOUT].toString()).time - getDate(document.data[Constants.CHECKIN_CHECKIN].toString()).time
                                        total_hours += (mHours)
                                    }
                                    mHistoryDisplay.add(mCheckInData)
                                }
                            }
                        }
                        if(total_payment > 0) {
                            tv_admin_transaction_history_payment.text = getString(R.string.rupees, total_payment)
                        }
                        else
                        {
                            tv_admin_transaction_history_payment.text = getString(R.string.not_checked_out)
                        }

                        if (total_hours > 0) {

                            tv_admin_transaction_history_total_hours.text = Utils.convertHours(total_hours)
                            tv_admin_transaction_history_estimated.text = ((total_hours / (60L * 60L * 1000L)) * userSalary).toString() + " ₹"

                            val pending = (total_hours / (60L * 60L * 1000L)) * userSalary - total_payment
                            if (pending <= 0) {
                                tv_admin_transaction_history_pending.text = getString(R.string.rupees, pending * -1)
                            } else {
                                tv_admin_transaction_history_pending.text = getString(R.string.no_pending_rupees, pending)
                            }

                        } else {
                            tv_admin_transaction_history_total_hours.text = getString(R.string.not_checked_out)
                            tv_admin_transaction_history_estimated.text = getString(R.string.not_checked_out)
                            tv_admin_transaction_history_pending.text = getString(R.string.not_checked_out)
                        }


                        adapterMonthly.notifyDataSetChanged()
                    } else {
                        Utils.showDialog(this, fetchall_task.exception.toString())
                        Log.e("With time", fetchall_task.exception.toString())
                    }

                    loadPay()
                }
    }

    private fun loadPay() {
        mPayList.clear()
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
                            mData.time = getDate(document.data[Constants.CHECKIN_CHECKOUT].toString()).time


                            if (!mData.payment.equals("null") && mData.payment.toString() != "") {
                                val mPayment = Integer.parseInt(document.data[Constants.CHECKIN_PAYMENT].toString())
                                total_payment += mPayment
                            }

                            mPayList.add(mData)

                        }
                        payAdapter.notifyDataSetChanged()
                        tv_admin_transaction_history_admin_paid_amount.text = getString(R.string.rupees, total_payment)

                    } else {
                        Utils.showDialog(this, fetchall_task.exception.toString())
                        Log.e("With time", fetchall_task.exception.toString())
                    }
                }



        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.fetching_data))
        mProgress?.setCancelable(false)
        mProgress?.show()
        mPayList.clear()


    }

    private fun getDate(date: String): Date {
        val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val value: Date = format.parse(date)
        return value
    }

    override fun onClick(data: CurrentCheckIndata) {
    }

    override fun onUserClick(data: UserListdata) {

    }

    override fun onClick(data: AdminPayData) {

    }
}
