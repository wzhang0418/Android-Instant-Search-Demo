package com.apolis.wenzhao.instantsearchdemo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apolis.wenzhao.instantsearchdemo.R
import com.apolis.wenzhao.instantsearchdemo.network.model.ContactsResponseItem
import com.apolis.wenzhao.instantsearchdemo.view.RemoteSearchActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.contact_row_item.view.*

class ContactsAdapter(
    var mContext: Context,
    var contactList: List<ContactsResponseItem>,
    var listener: RemoteSearchActivity
) : RecyclerView.Adapter<ContactsAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(contactsResponseItem: ContactsResponseItem) {
            itemView.name.text = contactsResponseItem.name
            itemView.phone.text = contactsResponseItem.phone
            itemView.setOnClickListener {
                listener.onContactSelected(contactList[adapterPosition]);
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
        return contactList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val contacts = contactList[position]
        holder.bind(contacts)
    }
}