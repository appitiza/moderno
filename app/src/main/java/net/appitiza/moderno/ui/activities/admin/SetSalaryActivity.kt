package net.appitiza.moderno.ui.activities.admin

import android.app.ProgressDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_set_salary.*
import net.appitiza.moderno.R
import net.appitiza.moderno.constants.Constants
import net.appitiza.moderno.ui.activities.BaseActivity
import net.appitiza.moderno.adapter.AdminSpnrUserAdapter
import net.appitiza.moderno.ui.activities.interfaces.UserClick
import net.appitiza.moderno.model.UserListdata
import net.appitiza.moderno.utils.PreferenceHelper
import net.appitiza.moderno.utils.Utils
import java.util.*

class SetSalaryActivity : BaseActivity(), UserClick {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_salary)
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
        tv_admin_salary_set.setOnClickListener { setSalary(et_admin_salary.text.toString()) }

        spnr_admin_salary_to.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                user = mUserList[position]
                if(user?.salary != 0) {
                    et_admin_salary.setText(user?.salary.toString())
                    et_admin_salary.setSelection(user?.salary.toString().length)
                }

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
                            data.salary = document.data[Constants.USER_SALARY].toString().toInt()
                            mUserList.add(data)

                        }

                        userAdapter = AdminSpnrUserAdapter(this, mUserList, this)
                        spnr_admin_salary_to.adapter = userAdapter
                        mProgress?.dismiss()

                    } else {
                        Utils.showDialog(this, fetchall_task.exception?.message.toString())
                    }
                }


    }

    private fun setSalary(amount: String) {
        if (validation(amount)) {
            mProgress?.setTitle(getString(R.string.app_name))
            mProgress?.setMessage(getString(R.string.sending_notification))
            mProgress?.setCancelable(false)
            mProgress?.show()

            // Sign in success, update UI with the signed-in user's information
            val map = HashMap<String, Any>()
            map[Constants.USER_SALARY] = amount
            db.collection(Constants.COLLECTION_USER)
                    .document(user?.emailId.toString())
                    .set(map, SetOptions.merge())
                    .addOnCompleteListener { send_task ->
                        if (send_task.isSuccessful) {
                            mProgress!!.dismiss()
                            Utils.showDialog(this, getString(R.string.salary_set_done))
                            et_admin_salary.setText("")

                        } else {
                            mProgress!!.hide()
                            Utils.showDialog(this, send_task.exception?.message.toString())
                        }
                    }


        } else {
            Utils.showDialog(this, "Please fill all details")

        }
    }

    private fun validation(amount: String): Boolean {
        return if (TextUtils.isEmpty(amount)) {
            showValidationWarning(getString(R.string.title_missing))
            false
        } else if (TextUtils.isEmpty(user?.emailId.toString())) {
            showValidationWarning(getString(R.string.user_missing))
            false
        } else {
            true
        }
    }
    override fun onUserClick(data: UserListdata) {

    }
}
