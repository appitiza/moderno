package net.appitiza.moderno.ui.activities.admin

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_admin_pay.*
import kotlinx.android.synthetic.main.activity_set_time.*
import net.appitiza.moderno.R
import net.appitiza.moderno.adapter.AdminSpnrUserAdapter
import net.appitiza.moderno.constants.Constants
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
    private var mCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_pay)
        initializeFireBase()
        getUser()
        setClick()
    }

    private fun initializeFireBase() {
        mUserList = arrayListOf()
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun setClick() {
        tv_admin_submit_pay_user.setOnClickListener {
            if (validate()) {
                insertPay()
            }

        }
        spnr_admin_pay_user_user.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                user = mUserList[position]
            }

        }
        et_admin_pay_date.setOnClickListener { loadCalendar() }

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


    override fun onUserClick(data: UserListdata) {

    }

    override fun onSiteClick(data: SiteListdata) {

    }

    private fun insertPay() {
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.syn))
        mProgress?.setCancelable(false)
        mProgress?.show()
        val map = HashMap<String, Any>()
        map[Constants.CHECKIN_SITE] = "0"
        map[Constants.CHECKIN_SITENAME] = "admin pay"
        map[Constants.CHECKIN_CHECKIN] = mCalendar.time
        map[Constants.CHECKIN_USEREMAIL] = user?.emailId.toString()
        map[Constants.CHECKIN_CHECKOUT] = mCalendar.time
        map[Constants.CHECKIN_PAYMENT] = et_admin_pay_user_payment.text.toString()
        map[Constants.CHECKIN_USERNAME] = user?.username.toString()
        map[Constants.CHECKIN_PAYMENT_TYPE] = "admin"


        db.collection(Constants.COLLECTION_CHECKIN_HISTORY)
                .add(map)
                .addOnSuccessListener { documentReference ->

                    et_admin_pay_user_payment.setText("")
                    et_admin_pay_date.setText("")
                    mProgress!!.dismiss()
                    Utils.showDialog(this, getString(R.string.payment_added))

                }
                .addOnFailureListener { e ->
                    mProgress!!.dismiss()

                }
    }

    private fun loadCalendar() {
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = android.app.DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                    loadTimer(year, monthOfYear, dayOfMonth)

                }, mYear, mMonth, mDay)

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - 1000


        datePickerDialog.setTitle(null)
        datePickerDialog.setCancelable(false)
        datePickerDialog.show()
    }

    private fun loadTimer(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val c = Calendar.getInstance()
        val mHour = c.get(Calendar.HOUR_OF_DAY)
        val mMinute = c.get(Calendar.MINUTE)


        val timePickerDialog = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->

                    mCalendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute, 1)
                    et_admin_pay_date.setText(Utils.convertDate(mCalendar.timeInMillis, "dd MMM yyyy HH:mm"))


                }, mHour, mMinute, false)
        timePickerDialog.setCancelable(false)
        timePickerDialog.show()
    }

    private fun validate(): Boolean {
        if (TextUtils.isEmpty(et_admin_pay_user_payment.text.toString())) {
            Utils.showDialog(this, getString(R.string.please_provide_payment_amount))
            return false
        } else if (TextUtils.isEmpty(et_admin_pay_date.text.toString())) {
            Utils.showDialog(this, getString(R.string.please_provide_payment_amount))
            return false
        } else {
            return true
        }

    }
}
