package net.appitiza.moderno.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_admin_pay.view.*
import net.appitiza.moderno.R
import net.appitiza.moderno.model.AdminPayData
import net.appitiza.moderno.ui.activities.interfaces.AdminPayClick

class AdminPayAdapter(private val mList: ArrayList<AdminPayData>, private val callback: AdminPayClick) : RecyclerView.Adapter<AdminPayAdapter.AdminPayHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminPayHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_pay, parent, false)
        return AdminPayHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: AdminPayHolder, position: Int) {
        holder.bindItems(mList[position])
        holder.itemView.setOnClickListener {
            callback.onClick(mList[position])
        }
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return mList.size
    }

    //the class is hodling the list view
    class AdminPayHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(data: AdminPayData) {
            itemView.tv_payitem_user.text = data.username
            itemView.tv_payitem_payment.text = data.payment + " ₹."
            itemView.tv_payitem_reason.text = data.reason
            itemView.tv_payitem_date.text = data.time


        }
    }

}