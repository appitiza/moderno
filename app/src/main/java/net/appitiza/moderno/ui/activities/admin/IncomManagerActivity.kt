package net.appitiza.moderno.ui.activities.admin

import android.app.ProgressDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_incom_manager.*
import net.appitiza.moderno.R
import net.appitiza.moderno.adapter.AdminSprSiteAdapter
import net.appitiza.moderno.constants.Constants
import net.appitiza.moderno.model.CurrentCheckIndata
import net.appitiza.moderno.model.IncomeExpenseData
import net.appitiza.moderno.model.SiteListdata
import net.appitiza.moderno.ui.activities.BaseActivity
import net.appitiza.moderno.ui.activities.interfaces.UserSiteClick
import net.appitiza.moderno.utils.PreferenceHelper
import net.appitiza.moderno.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

class IncomManagerActivity : BaseActivity(), UserSiteClick {
    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mSiteList: ArrayList<SiteListdata>
    private lateinit var siteAdapter: AdminSprSiteAdapter
    private var selectedSite: SiteListdata = SiteListdata()
    private lateinit var mHistory: ArrayList<CurrentCheckIndata>
    private lateinit var mIncomeExpenseList: ArrayList<IncomeExpenseData>

    private var total_income = 0
    private var total_expense = 0
    private var total_payment = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incom_manager)
        initializeFireBase()
        setClick()
        getSites()
    }
    private fun initializeFireBase() {

        mSiteList = arrayListOf()
        mHistory = arrayListOf()
        mIncomeExpenseList = arrayListOf()
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

    }
    private fun setClick() {

        spnr_admin_income_manager_sites.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                selectedSite = mSiteList[position]
                tv_admin_income_manager_projectcost.text = getString(R.string.rupees, mSiteList[position].cost)

                loadSiteDetails()
            }

        }


    }

    private fun getSites() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_site))
        mProgress?.setCancelable(false)
        mProgress?.show()

        db.collection(Constants.COLLECTION_SITE)
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
                            data.cost = document.data[Constants.SITE_COST].toString().toInt()
                            data.contact = document.data[Constants.SITE_CONTACT].toString()
                            data.person = document.data[Constants.SITE_PERSON].toString()
                            data.lat = document.data[Constants.SITE_LAT].toString().toDouble()
                            data.lon = document.data[Constants.SITE_LON].toString().toDouble()
                            data.status = document.data[Constants.SITE_STATUS].toString()
                            mSiteList.add(data)

                        }
                        siteAdapter = AdminSprSiteAdapter(this, mSiteList, this)
                        spnr_admin_income_manager_sites.adapter = siteAdapter
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this,fetchall_task.exception.toString())

                    }
                }


    }

    private fun loadSiteDetails() {

        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.fetching_data))
        mProgress?.setCancelable(false)
        mProgress?.show()
        mHistory.clear()
        db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                .whereEqualTo(Constants.CHECKIN_SITE,selectedSite.siteid.toString())
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()

                    if (fetchall_task.isSuccessful) {
                        total_payment = 0
                        var total_hours : Long = 0
                        for (document in fetchall_task.result) {
                            Log.d(" data", document.id + " => " + document.data)
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
                            mCheckInData.payment = document.data[Constants.CHECKIN_PAYMENT].toString()
                            if (document.data[Constants.CHECKIN_PAYMENT].toString() != "null" && document.data[Constants.CHECKIN_PAYMENT].toString() != "") {
                                val mPayment = Integer.parseInt(document.data[Constants.CHECKIN_PAYMENT].toString())
                                total_payment += mPayment
                            }
                            if(mCheckInData.checkintime != 0L && mCheckInData.checkouttime != 0L) {
                                val mHours = getDate(document.data[Constants.CHECKIN_CHECKOUT].toString()).time - getDate(document.data[Constants.CHECKIN_CHECKIN].toString()).time
                                total_hours += (mHours)
                            }
                            mHistory.add(mCheckInData)

                        }
                        tv_admin_income_manager_payment_recieved.text = getString(R.string.rupees,  total_payment)

                        if (total_hours > 0) {

                            tv_admin_income_manager_total_hours.text = Utils.convertHours(total_hours)

                        } else {
                            tv_admin_income_manager_total_hours.text = getString(R.string.not_checked_out)
                        }



                    } else {
                        Utils.showDialog(this,fetchall_task.exception.toString())
                    }
                    loadIncomeExpenseDetails()
                }
    }
    private fun loadIncomeExpenseDetails() {

        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.fetching_data))
        mProgress?.setCancelable(false)
        mProgress?.show()
        mHistory.clear()
        db.collection(Constants.COLLECTION_INCOME_EXPENSE)
                .whereEqualTo(Constants.INCOME_EXPENSE_SITE_ID,selectedSite.siteid.toString())
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()

                    if (fetchall_task.isSuccessful) {
                       total_income = 0
                       total_expense = 0
                        for (document in fetchall_task.result) {
                            val mIncomeExpenseData = IncomeExpenseData()
                            mIncomeExpenseData.id = document.id
                            mIncomeExpenseData.siteId = document.data[Constants.INCOME_EXPENSE_SITE_ID].toString()
                            mIncomeExpenseData.siteName = document.data[Constants.INCOME_EXPENSE_SITE_NAME].toString()
                            mIncomeExpenseData.categoryName = document.data[Constants.INCOME_EXPENSE_CATEGORY_NAME].toString()
                            mIncomeExpenseData.categoryId = document.data[Constants.INCOME_EXPENSE_CATEGORY_ID].toString()
                            val mPayment = Integer.parseInt(document.data[Constants.INCOME_EXPENSE_PAYMENT].toString())
                            mIncomeExpenseData.payment = mPayment
                            mIncomeExpenseData.reason = document.data[Constants.INCOME_EXPENSE_REASON].toString()
                            mIncomeExpenseData.time = getDate(document.data[Constants.INCOME_EXPENSE_TIME].toString()).time
                            mIncomeExpenseData.type = document.data[Constants.INCOME_EXPENSE_TYPE].toString()


                            if (mIncomeExpenseData.type == "income") {
                                total_income += mPayment
                            }
                            else
                            {
                                total_expense += mPayment
                            }


                            mIncomeExpenseList.add(mIncomeExpenseData)

                        }
                        tv_admin_income_manager_income.text = getString(R.string.rupees,  total_income)
                        tv_admin_income_manager_expense.text = getString(R.string.rupees,  total_expense)
                        tv_admin_income_manager_pending.text = getString(R.string.rupees,  (selectedSite.cost  - (total_income + total_payment)))

                        val profit = total_income  - (total_expense + total_payment)

                        if(profit >= 0 )
                        {
                            tv_admin_income_manager_profit_title.text = getString(R.string.profit_margin)
                            }
                        else
                        {
                            tv_admin_income_manager_profit_title.text = getString(R.string.loss_margin)
                        }
                        tv_admin_income_manager_profit.text = getString(R.string.rupees,  profit)



                    } else {
                        Utils.showDialog(this,fetchall_task.exception.toString())
                    }
                }
    }
    private fun getDate(date: String): Date {
        val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val value: Date = format.parse(date)
        return value
    }

    override fun onSiteClick(data: SiteListdata) {


    }
}
