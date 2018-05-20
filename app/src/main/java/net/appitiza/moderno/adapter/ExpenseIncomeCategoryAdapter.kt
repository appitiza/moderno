package net.appitiza.moderno.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_expese_income_category.view.*
import net.appitiza.moderno.R
import net.appitiza.moderno.model.Categorydata
import java.text.SimpleDateFormat
import java.util.*

class ExpenseIncomeCategoryAdapter(private val mList: ArrayList<Categorydata>) : RecyclerView.Adapter<ExpenseIncomeCategoryAdapter.CategoryHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_expese_income_category, parent, false)
        return CategoryHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder.bindItems(mList[position])

    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return mList.size
    }

    //the class is hodling the list view
    class CategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(data: Categorydata) {
            itemView.tv_title.text = data.title


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