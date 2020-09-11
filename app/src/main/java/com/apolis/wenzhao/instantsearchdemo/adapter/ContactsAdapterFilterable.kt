package com.apolis.wenzhao.instantsearchdemo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.apolis.wenzhao.instantsearchdemo.R
import com.apolis.wenzhao.instantsearchdemo.network.model.ContactsResponseItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.contact_row_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class ContactsAdapterFilterable(
    var mContext: Context,
    var contactList: List<ContactsResponseItem>,
    var listener: ContactsAdapterListener
) : RecyclerView.Adapter<ContactsAdapterFilterable.MyViewHolder>(), Filterable {

    private var contactListFiltered: List<ContactsResponseItem> = ArrayList()

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(contactsResponseItem: ContactsResponseItem) {
            itemView.name.text = contactsResponseItem.name
            itemView.phone.text = contactsResponseItem.phone
            itemView.setOnClickListener {
                listener.onContactSelected(contactListFiltered[adapterPosition]);
            }

            Glide.with(mContext)
                .load(contactsResponseItem.image)
                .apply(RequestOptions.circleCropTransform())
                .into(itemView.thumbnail)
        }
    }

    interface ContactsAdapterListener {
        fun onContactSelected(contact: ContactsResponseItem?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.contact_row_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return contactListFiltered.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val contacts = contactListFiltered[position]
        holder.bind(contacts)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString: String = charSequence.toString()
                contactListFiltered = if (charString.isEmpty()) {
                    contactList
                } else {
                    val filteredList: MutableList<ContactsResponseItem> = ArrayList()
                    for (row in contactList) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if ((row.name?.toLowerCase(Locale.ROOT)?.contains(charString.toLowerCase(Locale.ROOT))!!)
                            ||
                            (row.phone?.contains(charSequence!!)!!))
                        {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = contactListFiltered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                contactListFiltered = (filterResults!!.values as ArrayList<ContactsResponseItem>?)!!
                notifyDataSetChanged()
            }
        }
    }
}