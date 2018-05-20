package net.appitiza.moderno.ui.activities.admin

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_expense_category.*
import kotlinx.android.synthetic.main.activity_income_category.*
import kotlinx.android.synthetic.main.activity_notification.*
import net.appitiza.moderno.R
import net.appitiza.moderno.adapter.ExpenseIncomeCategoryAdapter
import net.appitiza.moderno.constants.Constants
import net.appitiza.moderno.model.Categorydata
import net.appitiza.moderno.ui.activities.BaseActivity
import net.appitiza.moderno.utils.Utils

class ExpenseCategoryActivity : BaseActivity() {
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore
    private var mProgress: ProgressDialog? = null
    private lateinit var mList : ArrayList<Categorydata>
    private lateinit var adapter: ExpenseIncomeCategoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_category)
        initializeFireBase()
        loadCategory()
        setClick()
    }

    private fun initializeFireBase() {
        mProgress = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        mList = arrayListOf()
        rv_category_expense.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseIncomeCategoryAdapter(mList)
        rv_category_expense.adapter = adapter
    }
    private fun loadCategory() {

        mProgress?.setTitle(getString(R.string.app_name))
        mProgress?.setMessage(getString(R.string.fetching_data))
        mProgress?.setCancelable(false)
        mProgress?.show()
        mList.clear()
        db.collection(Constants.COLLECTION_EXPENSE_CATEGORY)
                .get()
                .addOnCompleteListener { fetchall_task ->
                    mProgress?.dismiss()

                    if (fetchall_task.isSuccessful) {
                        for (document in fetchall_task.result) {
                            val mCategory = Categorydata()
                            mCategory.id = document.id
                            mCategory.title = document.data[Constants.EXPENSE_TITLE].toString()
                            mList.add(mCategory)

                        }
                        adapter.notifyDataSetChanged()
                    }
                }
    }
    private fun setClick() {
        tv_admin_expense_category_add.setOnClickListener { addCategory(et_admin_expense_category_title.text.toString()) }


    }

    private fun addCategory(title: String) {
        if (!TextUtils.isEmpty(title)) {
            mProgress?.setTitle(getString(R.string.app_name))
            mProgress?.setMessage(getString(R.string.adding_category))
            mProgress?.setCancelable(false)
            mProgress?.show()

            // Sign in success, update UI with the signed-in user's information
            val map = HashMap<String, Any>()
            map[Constants.EXPENSE_TITLE] = title
            map[Constants.EXPENSE_TIME] = FieldValue.serverTimestamp()

            db.collection(Constants.COLLECTION_EXPENSE_CATEGORY)
                    .document()
                    .set(map, SetOptions.merge())
                    .addOnCompleteListener { add_task ->
                        if (add_task.isSuccessful) {
                            mProgress!!.dismiss()
                            Utils.showDialog(this, getString(R.string.expense_category_addded ))
                            et_admin_expense_category_title.setText("")

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
