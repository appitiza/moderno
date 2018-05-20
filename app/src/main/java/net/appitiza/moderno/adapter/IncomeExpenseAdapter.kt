package net.appitiza.moderno.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_admin_income_expense.view.*
import kotlinx.android.synthetic.main.item_users_history.view.*
import net.appitiza.moderno.R
import net.appitiza.moderno.ui.activities.admin.AdminWorkReportsActivity
import net.appitiza.moderno.ui.activities.interfaces.AdminWorkHistoryClick
import net.appitiza.moderno.model.CurrentCheckIndata
import net.appitiza.moderno.model.IncomeExpenseData
import net.appitiza.moderno.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

class IncomeExpenseAdapter(private val mList: ArrayList<IncomeExpenseData>) : RecyclerView.Adapter<IncomeExpenseAdapter.IncomeExpenseHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeExpenseHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_income_expense, parent, false)
        return IncomeExpenseHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: IncomeExpenseHolder, position: Int) {
        holder.bindItems(mList[position])

    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return mList.size
    }

    //the class is hodling the list view
    class IncomeExpenseHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(data: IncomeExpenseData) {
            itemView.tv_income_expense_date.text = getDate(data.time.toLong(), "dd MMM yyyy")
            itemView.tv_income_expense_type.text = data.type
            itemView.tv_income_expense_category.text = data.categoryName
            itemView.tv_income_expense_reason.text = data.reason
            itemView.tv_income_expense_payment.text = data.payment.toString()

        }

        private fun getDate(milli: Long, dateFormat: String): String {
            val format = SimpleDateFormat(dateFormat, Locale.ENGLISH)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = milli
            val value = format.format(calendar.time)
            return value
        }
    }


}