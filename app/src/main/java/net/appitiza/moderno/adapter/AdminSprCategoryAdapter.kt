package net.appitiza.moderno.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import net.appitiza.moderno.R
import net.appitiza.moderno.model.Categorydata
import net.appitiza.moderno.ui.activities.interfaces.UserSiteClick
import net.appitiza.moderno.model.SiteListdata

class AdminSprCategoryAdapter(context: Context, private var categoryList: ArrayList<Categorydata>) : BaseAdapter() {

    private var context: Context? = context

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        val view: View?
        val vh: AdminSpnrCategoryHolder

        if (convertView == null) {
            val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            view = inflater.inflate(R.layout.item_admin_spnr_sitelist, parent, false)
            vh = AdminSpnrCategoryHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as AdminSpnrCategoryHolder
        }

        vh.tvTitle.text = categoryList[position].title
        return view
    }

    override fun getItem(position: Int): Any {
        return categoryList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return categoryList.size
    }
}

private class AdminSpnrCategoryHolder(view: View?) {
    val tvTitle: TextView = view?.findViewById(R.id.tv_checkin_site_item_name) as TextView

}
