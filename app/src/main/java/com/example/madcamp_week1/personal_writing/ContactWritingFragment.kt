package com.example.madcamp_week1

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson

class ContactWritingsFragment : Fragment() {

    private lateinit var writingsContainer: LinearLayout
    private var contact: Contact? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_writings, container, false)
        writingsContainer = view.findViewById(R.id.writings_container)

        // Set the toolbar title to the contact's name and handle the back button
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar3)
        val contactName = arguments?.getString("contactName")
        toolbar.title = contactName
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contactJson = arguments?.getString("contact")
        contact = Gson().fromJson(contactJson, Contact::class.java)

        contact?.let { populateWritings(it.writing) }
    }

    private fun populateWritings(writings: List<Writing>) {
        writingsContainer.removeAllViews()
        for (writing in writings) {
            val itemView = LayoutInflater.from(context).inflate(R.layout.writing_item_layout, writingsContainer, false)
            val textView = itemView.findViewById<TextView>(R.id.text_view)
            val heartButton = itemView.findViewById<ImageButton>(R.id.heart_button)

            textView.text = writing.text
            textView.textSize = 18f

            // Use ResourcesCompat to get the font
            val font = ResourcesCompat.getFont(requireContext(), R.font.roboto_regular)
            textView.typeface = font

            heartButton.setImageResource(
                if (writing.isLiked) R.drawable.ic_heart_liked else R.drawable.ic_heart_unliked
            )

            var isLiked = writing.isLiked
            heartButton.setOnClickListener {
                isLiked = !isLiked
                heartButton.setImageResource(
                    if (isLiked) R.drawable.ic_heart_liked else R.drawable.ic_heart_unliked
                )
                writing.isLiked = isLiked
                saveLikedState(contact!!, writing)
            }

            writingsContainer.addView(itemView)
        }
    }

    private fun saveLikedState(contact: Contact, writing: Writing) {
        val prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Find the index of the writing in the contact's list
        val index = contact.writing.indexOfFirst { it.text == writing.text }
        if (index != -1) {
            // Update the writing object in the list
            contact.writing[index] = writing

            // Serialize the contact object to JSON
            val gson = Gson()
            val json = gson.toJson(contact)

            // Save the JSON string to SharedPreferences using the contact's name as key
            editor.putString(contact.name, json)
            editor.apply()
        }
    }

    private fun saveContact(contact: Contact) {
        val prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(contact)
        editor.putString(contact.name, json)
        editor.apply()
    }

    private fun loadLikedState(contact: Contact) {
        val prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(contact.name, null)

        if (json != null) {
            // Deserialize JSON to Contact object
            val savedContact = gson.fromJson(json, Contact::class.java)

            // Update each writing's liked state based on savedContact
            for (savedWriting in savedContact.writing) {
                val writing = contact.writing.find { it.text == savedWriting.text }
                writing?.isLiked = savedWriting.isLiked
                writing?.likeNum = savedWriting.likeNum
            }
        }
    }
}
