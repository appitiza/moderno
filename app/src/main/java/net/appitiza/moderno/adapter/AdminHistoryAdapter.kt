package net.appitiza.moderno.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_admin_history.view.*
import net.appitiza.moderno.R
import net.appitiza.moderno.ui.activities.interfaces.AdminWorkHistoryClick
import net.appitiza.moderno.model.CurrentCheckIndata
import net.appitiza.moderno.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

class AdminHistoryAdapter(private var mContext: Context ,private val mList: ArrayList<CurrentCheckIndata>, private val callback : AdminWorkHistoryClick) : RecyclerView.Adapter<AdminHistoryAdapter.HistoryHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_history, parent, false)
        return HistoryHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        val userSalary: Double = mList[position].salary
        holder.bindItems(mContext, mList[position],userSalary)
        holder.itemView.ll_admin_history_root.setOnClickListener { callback.onClick(mList[position]) }

    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return mList.size
    }

    //the class is hodling the list view
    class HistoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(mContext: Context, data: CurrentCheckIndata,salary :Double) {
            itemView.tv_historyitem_date.text = getDate(data.checkintime!!.toLong(), "dd MMM yyyy")
            itemView.tv_historyitem_site.text = data.sitename

            if (data.checkintime != 0L) {
                if (data.checkouttime != 0L) {

                    itemView.tv_historyitem_hours.text = Utils.convertHours((data.checkouttime!!.toLong() - data.checkintime!!.toLong()))
                    if (data.payment.toString() != "null") {
                        val expected = ((data.checkouttime!!.toLong() - data.checkintime!!.toLong()) / (60L * 60L * 1000L)) * salary.toLong()
                        if (data.payment!!.toInt() > expected) {
                            itemView.tv_historyitem_payment.text = mContext.getString(R.string.payment_info, data.payment!!.toInt(),expected, 0)


                        } else {
                            itemView.tv_historyitem_payment.text = mContext.getString(R.string.payment_info, data.payment!!.toInt(),expected, expected - data.payment!!.toInt())

                        }
                    } else {
                        itemView.tv_historyitem_payment.text = mContext.getString(R.string.not_checked_out)
                    }
                } else {
                    itemView.tv_historyitem_hours.text = mContext.getString(R.string.not_checked_out)
                }
            }






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