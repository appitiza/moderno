package net.appitiza.moderno.ui.activities.admin

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.AdapterView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_income_expense_status.*
import net.appitiza.moderno.R
import net.appitiza.moderno.adapter.AdminSprSiteAdapter
import net.appitiza.moderno.adapter.IncomeExpenseAdapter
import net.appitiza.moderno.constants.Constants
import net.appitiza.moderno.model.CurrentCheckIndata
import net.appitiza.moderno.model.IncomeExpenseData
import net.appitiza.moderno.model.SiteListdata
import net.appitiza.moderno.ui.activities.BaseActivity
import net.appitiza.moderno.ui.activities.interfaces.UserSiteClick
import net.appitiza.moderno.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

class IncomeExpenseStatusActivity : BaseActivity(), UserSiteClick {
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mSiteList: ArrayList<SiteListdata>
    private lateinit var siteAdapter: AdminSprSiteAdapter
    private var selectedSite: SiteListdata = SiteListdata()
    private lateinit var mHistory: ArrayList<CurrentCheckIndata>
    private lateinit var mIncomeExpenseList: ArrayList<IncomeExpenseData>
    private lateinit var adapter: IncomeExpenseAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income_expense_status)
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

        rv_income_expense.layoutManager = LinearLayoutManager(this)
        adapter = IncomeExpenseAdapter(mIncomeExpenseList)
        rv_income_expense.adapter = adapter

    }

    private fun setClick() {

        spnr_admin_income_expense_site.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                selectedSite = mSiteList[position]
                loadIncomeExpenseDetails()

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
                        spnr_admin_income_expense_site.adapter = siteAdapter
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this, fetchall_task.exception.toString())

                    }
                }


    }

    private fun loadIncomeExpenseDetails() {

        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.fetching_data))
        mProgress?.setCancelable(false)
        mProgress?.show()
        mIncomeExpenseList.clear()
        db.collection(Constants.COLLECTION_INCOME_EXPENSE)
                .whereEqualTo(Constants.INCOME_EXPENSE_SITE_ID, selectedSite.siteid.toString())
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()

                    if (fetchall_task.isSuccessful) {
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



                            mIncomeExpenseList.add(mIncomeExpenseData)

                        }
                        adapter.notifyDataSetChanged()


                    } else {
                        Utils.showDialog(this, fetchall_task.exception.toString())
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
