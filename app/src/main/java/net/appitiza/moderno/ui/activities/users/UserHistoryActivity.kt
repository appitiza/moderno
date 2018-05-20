package net.appitiza.moderno.ui.activities.users

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user_history.*
import kotlinx.android.synthetic.main.users_daily_layout.*
import kotlinx.android.synthetic.main.users_monthly_layout.*
import net.appitiza.moderno.R
import net.appitiza.moderno.adapter.UserHistoryAdapter
import net.appitiza.moderno.constants.Constants
import net.appitiza.moderno.model.CurrentCheckIndata
import net.appitiza.moderno.ui.activities.BaseActivity
import net.appitiza.moderno.utils.PreferenceHelper
import net.appitiza.moderno.utils.Utils
import java.util.*


class UserHistoryActivity : BaseActivity() {
    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var salary by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_SALARY, 0)
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mHistoryDisplay: ArrayList<CurrentCheckIndata>
    private lateinit var mHistoryDaily: ArrayList<CurrentCheckIndata>
    private lateinit var mHistoryMonthly: ArrayList<CurrentCheckIndata>
    private lateinit var adapterMonthly: UserHistoryAdapter
    private val mSelectedCalender = Calendar.getInstance()

    private val mSelectedStartCalender = Calendar.getInstance()
    private val mSelectedEndCalender = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_history)
        initialize()
        setClick()
        loadDaily()
    }

    private fun initialize() {
        rv_history_list.layoutManager = LinearLayoutManager(this)
        mHistoryDisplay = arrayListOf()
        mHistoryDaily = arrayListOf()
        mHistoryMonthly = arrayListOf()
        adapterMonthly = UserHistoryAdapter(applicationContext, mHistoryDisplay, salary)
        rv_history_list.adapter = adapterMonthly
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        ll_users_daily_root.visibility = View.GONE
        ll_users_monthly_root.visibility = View.GONE
        mSelectedCalender.set(mSelectedCalender.get(Calendar.YEAR), mSelectedCalender.get(Calendar.MONTH), mSelectedCalender.get(Calendar.DAY_OF_MONTH), 0, 0, 1)

        mSelectedStartCalender.set(mSelectedStartCalender.get(Calendar.YEAR), mSelectedStartCalender.get(Calendar.MONTH), 1, 0, 0, 1)
        tv_user_history_monthly_start.text = Utils.convertDate(mSelectedStartCalender.timeInMillis, "dd MMM yyyy")
        mSelectedEndCalender.set(mSelectedEndCalender.get(Calendar.YEAR), mSelectedCalender.get(Calendar.MONTH), mSelectedCalender.get(Calendar.DAY_OF_MONTH), 0, 0, 1)
        tv_user_history_monthly_end.text = Utils.convertDate(mSelectedEndCalender.timeInMillis, "dd MMM yyyy")



        tv_useres_history_daily_date.text = Utils.convertDate(mSelectedCalender.timeInMillis, "dd MMM yyyy")


    }

    private fun setClick() {
        tv_user_history_daily.setOnClickListener { loadDaily() }
        tv_user_history_monthly.setOnClickListener { loadMonthly() }
        tv_useres_history_daily_date.setOnClickListener { loadCalendar(0) }
        tv_user_history_monthly_start.setOnClickListener { loadCalendar(1) }
        tv_user_history_monthly_end.setOnClickListener { loadCalendar(1) }
    }

    private fun loadCalendar(from: Int) {
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = android.app.DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->


                    if (from == 0) {
                        mSelectedCalender.set(year, monthOfYear, dayOfMonth, 0, 0, 1)
                        tv_useres_history_daily_date.text = Utils.convertDate(mSelectedCalender.timeInMillis, "dd MMM yyyy")
                        loadDaily()
                    } else if (from == 1) {
                        mSelectedStartCalender.set(year, monthOfYear, dayOfMonth, 0, 0, 1)
                        tv_user_history_monthly_start.text = Utils.convertDate(mSelectedStartCalender.timeInMillis, "dd MMM yyyy")
                        loadCalendar(2)
                    } else {
                        mSelectedEndCalender.set(year, monthOfYear, dayOfMonth, 0, 0, 1)
                        tv_user_history_monthly_end.text = Utils.convertDate(mSelectedEndCalender.timeInMillis, "dd MMM yyyy")
                        loadMonthly()
                    }

                }, mYear, mMonth, mDay)

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - 1000
        datePickerDialog.setTitle(null)
        datePickerDialog.setCancelable(false)
        datePickerDialog.show()
    }

    private fun loadDaily() {
        ll_users_daily_root.visibility = View.VISIBLE
        tv_user_history_daily.setTextColor(ContextCompat.getColor(this, R.color.text_clicked))
        ll_users_monthly_root.visibility = View.GONE
        tv_user_history_monthly.setTextColor(ContextCompat.getColor(this, R.color.white))


        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.fetching_data))
        mProgress?.setCancelable(false)
        mProgress?.show()

        val mCalender1 = Calendar.getInstance()
        mCalender1.set(mSelectedCalender.get(Calendar.YEAR), mSelectedCalender.get(Calendar.MONTH), 1, 0, 0, 1)
        val mCalender2 = Calendar.getInstance()
        mCalender2.set(mSelectedCalender.get(Calendar.YEAR), mSelectedCalender.get(Calendar.MONTH) + 1, 3, 23, 59, 59)


        db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                .whereEqualTo(Constants.CHECKIN_USEREMAIL, useremail)
                .whereLessThanOrEqualTo(Constants.CHECKIN_CHECKIN, mCalender2.time)
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()

                    if (fetchall_task.isSuccessful) {
                        var total_payment = 0
                        var total_hours: Long = 0
                        var total_sites: String = ""
                        for (document in fetchall_task.result) {
                            Log.d(" data", document.id + " => " + document.data)
                            val mCheckInData = CurrentCheckIndata()
                            mCheckInData.documentid = document.id
                            mCheckInData.siteid = document.data[Constants.CHECKIN_SITE].toString()
                            mCheckInData.sitename = document.data[Constants.CHECKIN_SITENAME].toString()
                            if (!TextUtils.isEmpty(document.data[Constants.CHECKIN_CHECKIN].toString()) && document.data[Constants.CHECKIN_CHECKIN].toString() != "null") {
                                mCheckInData.checkintime = Utils.getDateTimestamp(document.data[Constants.CHECKIN_CHECKIN].toString()).time
                            }
                            if (!TextUtils.isEmpty(document.data[Constants.CHECKIN_CHECKOUT].toString()) && document.data[Constants.CHECKIN_CHECKOUT].toString() != "null") {
                                mCheckInData.checkouttime = Utils.getDateTimestamp(document.data[Constants.CHECKIN_CHECKOUT].toString()).time
                            }
                            mCheckInData.useremail = document.data[Constants.CHECKIN_USEREMAIL].toString()
                            mCheckInData.username = document.data[Constants.CHECKIN_USERNAME].toString()
                            mCheckInData.payment = document.data[Constants.CHECKIN_PAYMENT].toString()

                            if (mCheckInData.checkintime!! >= mSelectedCalender.timeInMillis && mCheckInData.checkintime!! <= (mSelectedCalender.timeInMillis + (24L * 60L * 60L * 1000L))) {
                                if (!mCheckInData.payment.equals("null") && mCheckInData.payment.toString() != "") {
                                    val mPayment = Integer.parseInt(document.data[Constants.CHECKIN_PAYMENT].toString())
                                    total_payment += mPayment

                                    if (mCheckInData.sitename != "") {
                                        val site = mCheckInData.sitename
                                        total_sites += if (total_sites == "") {
                                            site
                                        } else {
                                            "," + site
                                        }
                                    }
                                }
                                if (mCheckInData.checkintime != 0L) {
                                    if (mCheckInData.checkouttime != 0L) {
                                        val mHours = Utils.getDateTimestamp(document.data[Constants.CHECKIN_CHECKOUT].toString()).time - Utils.getDateTimestamp(document.data[Constants.CHECKIN_CHECKIN].toString()).time
                                        total_hours += (mHours)
                                    }
                                }
                                mHistoryDaily.add(mCheckInData)
                            }

                        }
                        tv_useres_history_daily_payment.text = getString(R.string.rupees, total_payment)

                        if (total_hours > 0) {

                            tv_useres_history_daily_total_hours.text = Utils.convertHours(total_hours)

                        } else {
                            tv_useres_history_daily_total_hours.text = getString(R.string.not_checked_out)
                        }
                        tv_useres_history_daily_sites.text = total_sites
                    } else {
                        Utils.showDialog(this, fetchall_task.exception?.message.toString())
                        Log.e("With time", fetchall_task.exception.toString())
                    }
                }
    }

    private fun loadMonthly() {


        ll_users_daily_root.visibility = View.GONE
        tv_user_history_daily.setTextColor(ContextCompat.getColor(this, R.color.white))
        ll_users_monthly_root.visibility = View.VISIBLE
        tv_user_history_monthly.setTextColor(ContextCompat.getColor(this, R.color.text_clicked))

        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.fetching_data))
        mProgress?.setCancelable(false)
        mProgress?.show()

        val mCalender1 = Calendar.getInstance()
        mCalender1.set(mSelectedStartCalender.get(Calendar.YEAR), mSelectedStartCalender.get(Calendar.MONTH), mSelectedStartCalender.get(Calendar.DAY_OF_MONTH), 0, 0, 1)
        val mCalender2 = Calendar.getInstance()
        mCalender2.set(mSelectedEndCalender.get(Calendar.YEAR), mSelectedEndCalender.get(Calendar.MONTH) , mSelectedEndCalender.get(Calendar.DAY_OF_MONTH), 23, 59, 59)


        mHistoryMonthly.clear()
        mHistoryDisplay.clear()
        db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                .whereEqualTo(Constants.CHECKIN_USEREMAIL, useremail)
                .whereGreaterThanOrEqualTo(Constants.CHECKIN_CHECKIN, mCalender1.time)
                .whereLessThanOrEqualTo(Constants.CHECKIN_CHECKIN, mCalender2.time)
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()

                    if (fetchall_task.isSuccessful) {
                        var total_payment = 0
                        var total_hours: Long = 0
                        for (document in fetchall_task.result) {
                            Log.d(" data", document.id + " => " + document.data)

                            val mCheckInData = CurrentCheckIndata()
                            mCheckInData.documentid = document.id
                            mCheckInData.siteid = document.data[Constants.CHECKIN_SITE].toString()
                            mCheckInData.sitename = document.data[Constants.CHECKIN_SITENAME].toString()
                            if (!TextUtils.isEmpty(document.data[Constants.CHECKIN_CHECKIN].toString()) && document.data[Constants.CHECKIN_CHECKIN].toString() != "null") {
                                mCheckInData.checkintime = Utils.getDateTimestamp(document.data[Constants.CHECKIN_CHECKIN].toString()).time
                            }
                            if (!TextUtils.isEmpty(document.data[Constants.CHECKIN_CHECKOUT].toString()) && document.data[Constants.CHECKIN_CHECKOUT].toString() != "null") {
                                mCheckInData.checkouttime = Utils.getDateTimestamp(document.data[Constants.CHECKIN_CHECKOUT].toString()).time
                            }
                            mCheckInData.useremail = document.data[Constants.CHECKIN_USEREMAIL].toString()
                            mCheckInData.username = document.data[Constants.CHECKIN_USERNAME].toString()
                            mCheckInData.payment = document.data[Constants.CHECKIN_PAYMENT].toString()


                            //   if (mCheckInData.checkintime!! >= (mSelectedCalender.timeInMillis - (mSelectedCalender.get(Calendar.DAY_OF_MONTH) * 24L * 60L * 60L * 1000L)) && mCheckInData.checkintime!! <= (mSelectedCalender.timeInMillis + ((mSelectedCalender.getActualMaximum(Calendar.DATE) - mSelectedCalender.get(Calendar.DAY_OF_MONTH)) * 24L * 60L * 60L * 1000L))) {


                            if (document.data[Constants.CHECKIN_PAYMENT].toString() != "null" && document.data[Constants.CHECKIN_PAYMENT].toString() != "") {
                                val mPayment = Integer.parseInt(document.data[Constants.CHECKIN_PAYMENT].toString())
                                total_payment += mPayment
                            }
                            if (mCheckInData.checkintime != 0L) {
                                if (mCheckInData.checkouttime != 0L) {
                                    val mHours = Utils.getDateTimestamp(document.data[Constants.CHECKIN_CHECKOUT].toString()).time - Utils.getDateTimestamp(document.data[Constants.CHECKIN_CHECKIN].toString()).time
                                    total_hours += (mHours)
                                }
                            }
                            mHistoryMonthly.add(mCheckInData)
                            // }

                        }
                        tv_useres_history_monthly_payment.text = getString(R.string.rupees, total_payment)

                        if (total_hours > 0) {

                            tv_useres_history_monthly_total_hours.text = Utils.convertHours(total_hours)

                        } else {
                            tv_useres_history_monthly_total_hours.text = getString(R.string.not_checked_out)
                        }


                        mHistoryDisplay.addAll(mHistoryMonthly)
                        adapterMonthly.notifyDataSetChanged()
                    } else {
                        Utils.showDialog(this, fetchall_task.exception?.message.toString())
                        Log.e("With time", fetchall_task.exception.toString())
                    }
                }
    }

}
