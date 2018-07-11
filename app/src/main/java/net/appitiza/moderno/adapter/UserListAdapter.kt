package net.appitiza.moderno.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_users_list.view.*
import net.appitiza.moderno.R
import net.appitiza.moderno.model.UserListdata
import net.appitiza.moderno.ui.activities.interfaces.UserListItemClick

class UserListAdapter(private val callback : UserListItemClick,private val mList: ArrayList<UserListdata>) : RecyclerView.Adapter<UserListAdapter.UserListHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_users_list, parent, false)
        return UserListHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: UserListHolder, position: Int) {
        holder.bindItems(callback,mList[position])
        holder.itemView.tv_delete_user.setOnClickListener{callback.onDeleteClick(mList[position])}
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return mList.size
    }

    //the class is hodling the list view
    class UserListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(callback:UserListItemClick,data: UserListdata) {
            itemView.tv_name.text = "Name : " + data.username
            itemView.tv_email.text = "Email : " + data.emailId
            itemView.tv_salary.text = "Salary : " + data.salary.toString() + " ₹"

        }
    }

}