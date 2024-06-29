package com.example.madcamp_week1

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.android.synthetic.main.fragment_geul.*

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

        view.findViewById<View>(R.id.fab_add).setOnClickListener {
            val currentDestination = findNavController().currentDestination
            if (currentDestination?.id == R.id.notificationsFragment) {
                // Navigate to AddWritingFragment only if it's not already there
                findNavController().navigate(R.id.action_tab3Fragment_to_addWritingFragment)
            }
        }

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

        // Create a LinearLayout to hold author, text, and profile icon
        val layout = LinearLayout(context)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(5, 10, 5, 40) // Add margins between writings
        layout.layoutParams = layoutParams
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundResource(R.drawable.background_rounded_gray) // Rounded background

        // Create horizontal LinearLayout to hold profile icon and author name
        val authorLayout = LinearLayout(context)
        authorLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        authorLayout.orientation = LinearLayout.HORIZONTAL
        authorLayout.gravity = Gravity.CENTER_VERTICAL
        authorLayout.setPadding(50, 50, 50, 50)

        // Create ImageView for profile icon
        val profileIcon = ImageView(context)
        profileIcon.setImageResource(R.drawable.ic_person) // Replace with your actual icon resource
        profileIcon.layoutParams = LinearLayout.LayoutParams(
            100, // Adjust width as needed
            100  // Adjust height as needed
        )
        profileIcon.scaleType = ImageView.ScaleType.CENTER_CROP // Adjust scale type as needed
        authorLayout.addView(profileIcon)

        // Create TextView for author
        val authorTextView = TextView(context)
        authorTextView.text = author
        authorTextView.textSize = 25f
        authorTextView.setPadding(16, 0, 0, 0) // Adjust padding as needed
        authorLayout.addView(authorTextView)

        // Add authorLayout (profile icon + author name) to main layout
        layout.addView(authorLayout)

        // Create TextView for writing text
        val textView = TextView(context)
        textView.text = text
        textView.textSize = 20f
        textView.setPadding(100, 16, 100, 50) // Padding for writing text
        layout.addView(textView)

        return layout
    }
}
