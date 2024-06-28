package com.example.madcamp_week1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(private var contactList: MutableList<Contact>) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.contact_name)
        val phoneTextView: TextView = itemView.findViewById(R.id.contact_phone)
        val deleteImageView: ImageView = itemView.findViewById(R.id.contact_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val currentContact = contactList[position]
        holder.nameTextView.text = currentContact.name
        holder.phoneTextView.text = currentContact.phone
        holder.deleteImageView.visibility = if (currentContact.isPendingDelete) View.VISIBLE else View.GONE

        holder.deleteImageView.setOnClickListener {
            removeItem(position)
        }
    }

    override fun getItemCount() = contactList.size

    fun removeItem(position: Int): Contact {
        val deletedContact = contactList.removeAt(position)
        notifyItemRemoved(position)
        return deletedContact
    }

    fun onItemSwiped(position: Int) {
        contactList[position].isPendingDelete = true
        notifyItemChanged(position)
    }
}
