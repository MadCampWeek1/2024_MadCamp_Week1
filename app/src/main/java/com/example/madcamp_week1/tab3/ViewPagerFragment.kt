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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class ViewPagerFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: WritingPagerAdapter
    private lateinit var contactList: List<Contact>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

        contactList = readContactsFromJson(requireContext())
        adapter = WritingPagerAdapter(contactList, requireContext(), this)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                adapter.setCurrentIndex(position, 0) // Reset currentIndex to 0 for the selected contact
            }
        })
    }

    private fun readContactsFromJson(context: Context): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val gson = Gson()
        try {
            val filename = "contact.json"
            val file = File(context.filesDir, filename)

            if (file.exists()) {
                val inputStream = FileInputStream(file)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonStringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    jsonStringBuilder.append(line)
                }
                reader.close()

                val type = object : TypeToken<List<Contact>>() {}.type
                contacts.addAll(gson.fromJson(jsonStringBuilder.toString(), type))
            } else {
                // Handle case where file does not exist
            }
        } catch (e: Exception) {
            // Handle exception
        }
        return contacts
    }
    fun onAuthorClick(contact: Contact) {
        showContactDialog(contact)
    }

    private fun showContactDialog(contact: Contact) {
        val dialog = Dialog(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_contact_info, null)

        val contactImageView: ImageView = dialogView.findViewById(R.id.contact_image)
        val contactNameTextView: TextView = dialogView.findViewById(R.id.contact_name)
        val contactPhoneTextView: TextView = dialogView.findViewById(R.id.contact_phone)
        val contactGenderTextView: TextView = dialogView.findViewById(R.id.contact_gender)
        val contactAgeTextView: TextView = dialogView.findViewById(R.id.contact_age)
        val contactIntroductionTextView: TextView = dialogView.findViewById(R.id.contact_introduction)
        val sendMessageButton: Button = dialogView.findViewById(R.id.send_message_button)
        val closeButton: ImageButton = dialogView.findViewById(R.id.btn_close)

        Glide.with(requireContext())
            .load(contact.profileImage)
            .placeholder(R.drawable.ic_contact_placeholder)
            .error(R.drawable.ic_contact_placeholder)
            .into(contactImageView)

        contactNameTextView.text = contact.name
        contactPhoneTextView.text = contact.phone
        contactGenderTextView.text = "성별: ${contact.gender}"
        contactAgeTextView.text = "나이: ${contact.age?.toString()}"
        contactIntroductionTextView.text = contact.introduction

        sendMessageButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("contactName", contact.name)
                putString("contact", Gson().toJson(contact))
            }
            findNavController().navigate(R.id.action_contactListFragment_to_contactWritingsFragment, bundle)
            dialog.dismiss()
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(dialogView)
        dialog.show()

        val window = dialog.window
        if (window != null) {
            val metrics = DisplayMetrics()
            window.windowManager.defaultDisplay.getMetrics(metrics)
            val width = (metrics.widthPixels * 0.8).toInt()
            val height = (metrics.heightPixels * 0.65).toInt()
            window.setLayout(width, height)

            val buttonWidth = (width * 0.8).toInt()
            sendMessageButton.post {
                val layoutParams = sendMessageButton.layoutParams
                layoutParams.width = buttonWidth
                sendMessageButton.layoutParams = layoutParams
            }
        }
    }
}
