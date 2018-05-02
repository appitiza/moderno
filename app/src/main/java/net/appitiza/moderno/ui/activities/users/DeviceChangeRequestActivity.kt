package net.appitiza.moderno.ui.activities.users

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_admin_sites.*
import kotlinx.android.synthetic.main.activity_device_change.*
import kotlinx.android.synthetic.main.activity_register.*
import net.appitiza.moderno.BuildConfig
import net.appitiza.moderno.R
import net.appitiza.moderno.constants.Constants
import net.appitiza.moderno.ui.activities.BaseActivity
import net.appitiza.moderno.utils.PreferenceHelper
import net.appitiza.moderno.utils.Utils
import java.util.HashMap

class DeviceChangeRequestActivity : BaseActivity() {
    private var isLoggedIn by PreferenceHelper(Constants.PREF_KEY_IS_USER_LOGGED_IN, false)
    private var displayName by PreferenceHelper(Constants.PREF_KEY_IS_USER_DISPLAY_NAME, "")
    private var useremail by PreferenceHelper(Constants.PREF_KEY_IS_USER_EMAIL, "")
    private var userpassword by PreferenceHelper(Constants.PREF_KEY_IS_USER_PASSWORD, "")
    private var userimei by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_IMEI, "")
    private var usertype by PreferenceHelper(Constants.PREF_KEY_IS_USER_USER_TYPE, "")
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 33
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_change)
        initializeFireBase()
        setClick()
    }
    private fun initializeFireBase() {
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        if (checkPermissions()) {
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val device_id = tm.deviceId
            et_users_change_device_imei.setText(device_id)
        } else {
            requestPermissions()
        }

    }

    private fun setClick() {
        tv_users_change_device_request.setOnClickListener { sendRequest(et_users_change_device_imei.text.toString(), et_users_change_device_reason.text.toString()) }

    }

    private fun sendRequest(imei: String, reason: String) {
        if (validation(imei, reason)) {
            mProgress?.setTitle(getString(R.string.app_name))
            mProgress?.setMessage(getString(R.string.sending_notification))
            mProgress?.setCancelable(false)
            mProgress?.show()

            // Sign in success, update UI with the signed-in user's information
            val map = HashMap<String, Any>()
            map[Constants.DEVICE_CHANGE_REQUEST_IMEI] = imei
            map[Constants.DEVICE_CHANGE_REQUEST_CURRENT_IMEI] = userimei
            map[Constants.DEVICE_CHANGE_REQUEST_REASON] = reason
            map[Constants.DEVICE_CHANGE_REQUEST_EMAIL] = useremail
            map[Constants.DEVICE_CHANGE_REQUEST_NAME] = displayName
            map[Constants.DEVICE_CHANGE_REQUEST_STATUS] = "Pending"
            map[Constants.DEVICE_CHANGE_REQUEST_TIME] = FieldValue.serverTimestamp()

            db.collection(Constants.COLLECTION_DEVICE_CHANGE_REQUEST)
                    .document()
                    .set(map, SetOptions.merge())
                    .addOnCompleteListener { send_task ->
                        if (send_task.isSuccessful) {
                            mProgress!!.dismiss()
                            Utils.showDialog(this, getString(R.string.request_sent))
                            et_users_change_device_imei.setText("")
                            et_users_change_device_reason.setText("")

                        } else {
                            mProgress!!.hide()
                            Utils.showDialog(this, send_task.exception?.message.toString())
                        }
                    }


        } else {
            Utils.showDialog(this, "Please fill all details")

        }
    }

    private fun validation(imei: String, reason: String): Boolean {
        return if (TextUtils.isEmpty(imei)) {
            showValidationWarning(getString(R.string.title_missing))
            false
        } else if (TextUtils.isEmpty(reason)) {
            showValidationWarning(getString(R.string.message_missing))
            false
        } else if (TextUtils.isEmpty(useremail.toString())) {
            showValidationWarning(getString(R.string.user_missing))
            false
        } else {
            true
        }
    }

    private fun checkPermissions(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE)
        }
        else
        {
            return true
        }
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)

        if (shouldProvideRationale) {
            Snackbar.make(
                    fab_admin_add_site,
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, View.OnClickListener {
                        requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE),
                                REQUEST_PERMISSIONS_REQUEST_CODE)
                    })
                    .show()
        } else {

            requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE),
                    REQUEST_PERMISSIONS_REQUEST_CODE)

        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val device_id = tm.deviceId
                et_users_change_device_imei.setText(device_id)
                et_users_change_device_imei.setSelection(device_id.length)

            } else {
                // Permission denied.
                Snackbar.make(fab_admin_add_site,
                        R.string.permission_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, View.OnClickListener {
                            val intent: Intent = Intent()
                            intent.action =  Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri: Uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null)
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        })
                        .show()

            }
        }


    }
}
