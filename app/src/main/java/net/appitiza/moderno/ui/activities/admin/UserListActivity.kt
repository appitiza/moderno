package net.appitiza.moderno.ui.activities.admin

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user_list.*
import net.appitiza.moderno.R
import net.appitiza.moderno.adapter.UserListAdapter
import net.appitiza.moderno.constants.Constants
import net.appitiza.moderno.model.UserListdata
import net.appitiza.moderno.ui.activities.BaseActivity
import net.appitiza.moderno.ui.activities.interfaces.UserListItemClick
import net.appitiza.moderno.utils.PreferenceHelper
import java.util.*



class UserListActivity : BaseActivity(), UserListItemClick {


    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mUserList: ArrayList<UserListdata>
    private lateinit var adapter: UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        initializeFireBase()
        getAllUser()
    }

    private fun initializeFireBase() {
        rv_user_list.layoutManager = LinearLayoutManager(this)
        mUserList = arrayListOf()
        adapter = UserListAdapter(this@UserListActivity,mUserList)
        rv_user_list.adapter = adapter
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun getAllUser() {
        mUserList.clear()
        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.getting_undergoing_site))
        mProgress?.setCancelable(false)
        mProgress?.show()
        db.collection(Constants.COLLECTION_USER)
                .whereEqualTo(Constants.USER_TYPE, "user")
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()

                    if (fetchall_task.isSuccessful) {
                        for (document in fetchall_task.result) {
                            val data = UserListdata()
                            data.emailId = document.data[Constants.USER_EMAIL].toString()
                            data.username = document.data[Constants.USER_DISPLAY_NAME].toString()
                            data.salary = document.data[Constants.USER_SALARY].toString().toDouble()
                            mUserList.add(data)

                        }
                        adapter.notifyDataSetChanged()

                    }
                }
    }

    override fun onDeleteClick(item: UserListdata) {

        Toast.makeText(applicationContext,"Not Implimented",Toast.LENGTH_SHORT).show()
       /* mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.delete_user_message))
        mProgress?.setCancelable(false)
        mProgress?.show()

        val credential = EmailAuthProvider
                .getCredential(item.emailId.toString(), item.)

        // Prompt the user to re-provide their sign-in credentials
        user!!.reauthenticate(credential)
                .addOnCompleteListener(object : OnCompleteListener<Void> {
                    fun onComplete(task: Task<Void>) {
                        user.delete()
                                .addOnCompleteListener(object : OnCompleteListener<Void> {
                                    fun onComplete(task: Task<Void>) {
                                        if (task.isSuccessful()) {
                                            Log.d(FragmentActivity.TAG, "User account deleted.")
                                        }
                                    }
                                })

                    }
                })*/
    }

}
