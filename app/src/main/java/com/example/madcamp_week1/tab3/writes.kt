package com.example.madcamp_week1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class Tab3Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_geul, container, false)
        val writingsContainer = view.findViewById<LinearLayout>(R.id.writings_container)

        val contactList = readContactsFromJson()

        populateWritings(contactList, writingsContainer)

        return view
    }

    private fun readContactsFromJson(): List<Contact> {
        val jsonFile = resources.openRawResource(R.raw.contact)
        val jsonString = BufferedReader(InputStreamReader(jsonFile)).use { it.readText() }
        val gson = Gson()
        return gson.fromJson(jsonString, object : TypeToken<List<Contact>>() {}.type)
    }

    private fun populateWritings(contactList: List<Contact>, container: LinearLayout) {
        for (contact in contactList) {
            for (writing in contact.writing) {
                val writingView = createWritingView(contact.name, writing)
                container.addView(writingView)
            }
        }
    }

    private fun createWritingView(author: String, text: String): View {
        val context = requireContext()

        // Create a LinearLayout to hold author and text
        val layout = LinearLayout(context)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 16) // Add margins between writings
        layout.layoutParams = layoutParams
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundResource(R.drawable.background_rounded_gray) // Rounded background

        // Create TextView for author
        val authorTextView = TextView(context)
        authorTextView.text = author
        authorTextView.textSize = 18f
        authorTextView.setPadding(16, 8, 16, 0) // Padding for author text
        layout.addView(authorTextView)

        // Create TextView for writing text
        val textView = TextView(context)
        textView.text = text
        textView.textSize = 16f
        textView.setPadding(16, 0, 16, 8) // Padding for writing text
        layout.addView(textView)

        return layout
    }
}
