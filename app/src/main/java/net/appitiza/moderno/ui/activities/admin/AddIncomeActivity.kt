package net.appitiza.moderno.ui.activities.admin

import android.app.ProgressDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_add_income.*
import net.appitiza.moderno.R
import net.appitiza.moderno.adapter.AdminSprCategoryAdapter
import net.appitiza.moderno.adapter.AdminSprSiteAdapter
import net.appitiza.moderno.constants.Constants
import net.appitiza.moderno.model.Categorydata
import net.appitiza.moderno.model.SiteListdata
import net.appitiza.moderno.ui.activities.BaseActivity
import net.appitiza.moderno.ui.activities.interfaces.UserSiteClick
import net.appitiza.moderno.utils.PreferenceHelper
import net.appitiza.moderno.utils.Utils
import java.util.*

class AddIncomeActivity : BaseActivity(), UserSiteClick {


    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mSiteList: ArrayList<SiteListdata>
    private lateinit var mCategoryList: ArrayList<Categorydata>
    private lateinit var siteAdapter: AdminSprSiteAdapter
    private lateinit var categoryAdapter: AdminSprCategoryAdapter
    private var selectedSite: SiteListdata = SiteListdata()
    private var selectedCategory: Categorydata = Categorydata()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)
        initializeFireBase()
        setClick()
        getSites()
        getCategory()
    }

    private fun initializeFireBase() {

        mSiteList = arrayListOf()
        mCategoryList = arrayListOf()
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

    }

    private fun setClick() {

        spnr_admin_add_income_site.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                selectedSite = mSiteList[position]

            }

        }
        spnr_admin_add_income_category.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                selectedCategory = mCategoryList[position]

            }

        }
        tv_admin_submit_add_income.setOnClickListener { addIncome() }

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
                            val data = SiteListdata()
                            data.siteid = document.id
                            data.sitename = document.data[Constants.SITE_NAME].toString()
                            data.type = document.data[Constants.SITE_TYPE].toString()
                            data.date = document.data[Constants.SITE_DATE].toString()
                            data.cost = document.data[Constants.SITE_COST].toString()
                            data.contact = document.data[Constants.SITE_CONTACT].toString()
                            data.person = document.data[Constants.SITE_PERSON].toString()
                            data.lat = document.data[Constants.SITE_LAT].toString().toDouble()
                            data.lon = document.data[Constants.SITE_LON].toString().toDouble()
                            data.status = document.data[Constants.SITE_STATUS].toString()
                            mSiteList.add(data)

                        }
                        siteAdapter = AdminSprSiteAdapter(this, mSiteList, this)
                        spnr_admin_add_income_site.adapter = siteAdapter
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this, fetchall_task.exception.toString())

                    }
                }
    }

    private fun getCategory() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_category))
        mProgress?.setCancelable(false)
        mProgress?.show()

        db.collection(Constants.COLLECTION_INCOME_CATEGORY)
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()
                    if (fetchall_task.isSuccessful) {
                        for (document in fetchall_task.result) {
                            val data: Categorydata = Categorydata()
                            data.id = document.id
                            data.title = document.data[Constants.INCOME_TITLE].toString()
                            data.type = "income"
                            mCategoryList.add(data)

                        }
                        categoryAdapter = AdminSprCategoryAdapter(this, mCategoryList)
                        spnr_admin_add_income_category.adapter = categoryAdapter
                        mProgress?.dismiss()

                    }
                }
    }

    private fun addIncome() {
        if (validate()) {
            mProgress?.setTitle(getString(R.string.app_name))
            mProgress?.setMessage(getString(R.string.adding_income_details))
            mProgress?.setCancelable(false)
            mProgress?.show()

            // Sign in success, update UI with the signed-in user's information
            val map = HashMap<String, Any>()
            map[Constants.INCOME_EXPENSE_SITE_ID] = selectedSite.siteid.toString()
            map[Constants.INCOME_EXPENSE_SITE_NAME] = selectedSite.sitename.toString()
            map[Constants.INCOME_EXPENSE_CATEGORY_ID] = selectedCategory.id.toString()
            map[Constants.INCOME_EXPENSE_CATEGORY_NAME] = selectedCategory.title.toString()
            map[Constants.INCOME_EXPENSE_PAYMENT] = et_admin_add_income_payment.text.toString()
            map[Constants.INCOME_EXPENSE_REASON] = et_admin_add_income_reason.text.toString()
            map[Constants.INCOME_EXPENSE_TYPE] = "Income"
            map[Constants.INCOME_EXPENSE_TIME] = FieldValue.serverTimestamp()

            db.collection(Constants.COLLECTION_INCOME_EXPENSE)
                    .document()
                    .set(map, SetOptions.merge())
                    .addOnCompleteListener { add_task ->
                        if (add_task.isSuccessful) {
                            mProgress!!.dismiss()
                            Utils.showDialog(this, getString(R.string.income_addded))
                            et_admin_add_income_payment.setText("")
                            et_admin_add_income_reason.setText("")

                        } else {
                            mProgress!!.hide()
                            Utils.showDialog(this, add_task.exception?.message.toString())
                        }
                    }


        } else {
            Utils.showDialog(this, getString(R.string.please_fill_necessary_detail))

        }
    }

    private fun validate(): Boolean {
        return if (selectedSite.siteid != null) {
            showValidationWarning(getString(R.string.site_not_selected))
            false
        } else if (selectedCategory.id != null) {
            showValidationWarning(getString(R.string.category_not_selected))
            false
        } else if (TextUtils.isEmpty(et_admin_add_income_payment.text.toString())) {
            showValidationWarning(getString(R.string.payment_missing))
            false
        } else if (TextUtils.isEmpty(et_admin_add_income_reason.text.toString())) {
            showValidationWarning(getString(R.string.reason_missing))
            false
        } else {
            true
        }
    }

    override fun onSiteClick(data: SiteListdata) {

    }
}
