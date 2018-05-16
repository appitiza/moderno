package net.appitiza.moderno.ui.activities.admin

import android.app.ProgressDialog
import android.os.Bundle
import android.text.TextUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_income_category.*
import net.appitiza.moderno.R
import net.appitiza.moderno.constants.Constants
import net.appitiza.moderno.ui.activities.BaseActivity
import net.appitiza.moderno.utils.Utils

class IncomeCategoryActivity : BaseActivity() {
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income_category)
        initializeFireBase()
        setClick()
    }

    private fun initializeFireBase() {
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun setClick() {
        tv_admin_income_category_add.setOnClickListener { addCategory(et_admin_income_category_title.text.toString()) }


    }

    private fun addCategory(title: String) {
        if (!TextUtils.isEmpty(title)) {
            mProgress?.setTitle(getString(R.string.app_name))
            mProgress?.setMessage(getString(R.string.adding_category))
            mProgress?.setCancelable(false)
            mProgress?.show()

            // Sign in success, update UI with the signed-in user's information
            val map = HashMap<String, Any>()
            map[Constants.INCOME_TITLE] = title
            map[Constants.INCOME_TIME] = FieldValue.serverTimestamp()

            db.collection(Constants.COLLECTION_INCOME_CATEGORY)
                    .document()
                    .set(map, SetOptions.merge())
                    .addOnCompleteListener { add_task ->
                        if (add_task.isSuccessful) {
                            mProgress!!.dismiss()
                            Utils.showDialog(this, getString(R.string.income_category_addded))
                            et_admin_income_category_title.setText("")

                        } else {
                            mProgress!!.hide()
                            Utils.showDialog(this, add_task.exception?.message.toString())
                        }
                    }


        } else {
            Utils.showDialog(this, getString(R.string.please_fill_necessary_detail))

        }
    }
}
