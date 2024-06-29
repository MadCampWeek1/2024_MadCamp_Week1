package com.example.madcamp_week1

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.android.synthetic.main.fragment_geul.*
import java.io.FileInputStream
import java.io.FileNotFoundException

class Tab3Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_geul, container, false)
        val writingsContainer = view.findViewById<LinearLayout>(R.id.writings_container)

        val contactList = readContactsFromJson(requireContext())

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

    fun readContactsFromJson(context: Context): List<Contact> {
        val contacts = mutableListOf<Contact>()
        try {
            // Open an InputStream to read the raw resource
            val inputStream = context.resources.openRawResource(R.raw.contact)
            val reader = BufferedReader(InputStreamReader(inputStream))

            // Read the JSON data into a StringBuilder
            val jsonStringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                jsonStringBuilder.append(line)
            }

            // Parse JSON using Gson
            val type = object : TypeToken<List<Contact>>() {}.type
            contacts.addAll(Gson().fromJson(jsonStringBuilder.toString(), type))

            // Close the reader
            reader.close()
        } catch (e: Exception) {
            Log.e("Tab3Fragment", "Error reading contacts from JSON: ${e.message}")
        }
        return contacts
    }


    private fun populateWritings(contactList: List<Contact>, container: LinearLayout) {
        container.removeAllViews() // Clear the container before populating
        for (contact in contactList) {
            for (writing in contact.writing) {
                val writingView = createWritingView(contact, writing)
                container.addView(writingView)
            }
        }
    }

    private fun createWritingView(contact: Contact, text: String): View {
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
        authorTextView.text = contact.name
        authorTextView.textSize = 25f
        authorTextView.setPadding(16, 0, 0, 0) // Adjust padding as needed
        authorTextView.setOnClickListener {
            showContactDialog(contact)
        }

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
    private fun showContactDialog(contact: Contact) {
        val dialog = Dialog(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_contact_info, null)

        val contactImageView: ImageView = dialogView.findViewById(R.id.contact_image)
        val contactNameTextView: TextView = dialogView.findViewById(R.id.contact_name)
        val contactPhoneTextView: TextView = dialogView.findViewById(R.id.contact_phone)
        val sendMessageButton: Button = dialogView.findViewById(R.id.send_message_button)

        // Load contact image using Glide
        Glide.with(requireContext())
            .load(contact.profileImage)
            .placeholder(R.drawable.ic_contact_placeholder)
            .error(R.drawable.ic_contact_placeholder) // Add error placeholder
            .into(contactImageView)

        contactNameTextView.text = contact.name
        contactPhoneTextView.text = contact.phone

        // Set button click listener
        sendMessageButton.setOnClickListener {
            Toast.makeText(requireContext(), "Message sent to ${contact.name}", Toast.LENGTH_SHORT).show()
            // Perform any additional actions here
        }

        dialog.setContentView(dialogView)
        dialog.show()

        // Set dialog width and height
        val window = dialog.window
        if (window != null) {
            val metrics = DisplayMetrics()
            window.windowManager.defaultDisplay.getMetrics(metrics)
            val width = (metrics.widthPixels * 0.8).toInt()
            val height = (metrics.heightPixels * 0.45).toInt()
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
