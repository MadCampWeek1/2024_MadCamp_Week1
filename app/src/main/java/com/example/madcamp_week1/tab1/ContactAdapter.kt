package com.example.madcamp_week1

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.madcamp_week1.viewmodel.ContactViewModel
import com.google.gson.Gson

class ContactAdapter(
    private val context: Context,
    var contactList: MutableList<Contact>,
    private val contactViewModel: ContactViewModel
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

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

        holder.itemView.setOnClickListener {
            showContactDialog(currentContact, holder.itemView)
        }

        holder.deleteImageView.setOnClickListener {
            contactViewModel.removeContact(currentContact)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, contactList.size)
            Toast.makeText(context, "Contact deleted: ${currentContact.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = contactList.size

    fun updateList(newList: List<Contact>) {
        contactList = newList.toMutableList()
        notifyDataSetChanged()
    }

    private fun showContactDialog(contact: Contact, itemView: View) {
        val dialog = Dialog(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_contact_info, null)

        val contactImageView: ImageView = dialogView.findViewById(R.id.contact_image)
        val contactNameTextView: TextView = dialogView.findViewById(R.id.contact_name)
        val contactPhoneTextView: TextView = dialogView.findViewById(R.id.contact_phone)
        val contactGenderTextView: TextView = dialogView.findViewById(R.id.contact_gender)
        val contactAgeTextView: TextView = dialogView.findViewById(R.id.contact_age)
        val contactIntroductionTextView: TextView = dialogView.findViewById(R.id.contact_introduction)
        val sendMessageButton: Button = dialogView.findViewById(R.id.send_message_button)
        val closeButton: ImageButton = dialogView.findViewById(R.id.btn_close)

        // Load contact image using Glide
        Glide.with(context)
            .load(contact.profileImage)
            .placeholder(R.drawable.ic_contact_placeholder)
            .error(R.drawable.ic_contact_placeholder)
            .into(contactImageView)

        contactNameTextView.text = contact.name
        contactPhoneTextView.text = contact.phone
        contactGenderTextView.text = "Gender: ${contact.gender}"
        contactAgeTextView.text = "Age: ${contact.age?.toString()}"
        contactIntroductionTextView.text = contact.introduction

        // Set button click listener
        sendMessageButton.setOnClickListener {
            dialog.dismiss()
            val contactJson = Gson().toJson(contact)
            val bundle = Bundle().apply {
                putString("contact", contactJson)
                putString("contactName", contact.name)  // Pass the contact name
            }
            itemView.findNavController().navigate(R.id.action_contactListFragment_to_contactWritingsFragment, bundle)
        }

        // Set close button click listener
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(dialogView)
        dialog.show()

        // Set dialog width and height
        val window = dialog.window
        if (window != null) {
            val metrics = DisplayMetrics()
            window.windowManager.defaultDisplay.getMetrics(metrics)
            val width = (metrics.widthPixels * 0.8).toInt()
            val height = (metrics.heightPixels * 0.5).toInt()
            window.setLayout(width, height)

            // Set button width to match 80% of dialog width
            val buttonWidth = (width * 0.7).toInt()
            sendMessageButton.post {
                val layoutParams = sendMessageButton.layoutParams
                layoutParams.width = buttonWidth
                sendMessageButton.layoutParams = layoutParams
            }
        }
    }
}
