package com.example.madcamp_week1

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_geul.*
import java.io.*


class Tab3Fragment : Fragment() {

    private lateinit var writingsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_geul, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        writingsContainer = view.findViewById(R.id.writings_container)

        view.findViewById<View>(R.id.fab_add).setOnClickListener {
            findNavController().navigate(R.id.action_tab3Fragment_to_addWritingFragment)
        }

        val args = arguments
        if (args != null && args.containsKey("newWriting")) {
            val newWriting = args.getString("newWriting", "")
            if (!newWriting.isNullOrEmpty()) {
                // Save the new writing to your data (SharedPreferences or JSON file)
                saveNewWriting(newWriting)
            }
        }
    }

    private fun saveNewWriting(newWriting: String) {
        if (newWriting.isEmpty()) return

        val gson = Gson()
        val context = requireContext()

        // Ensure context is not null before proceeding
        context ?: return

        try {
            // Read existing contacts from file
            val filename = "contact.json"
            val file = File(context.filesDir, filename)

            val contactList: MutableList<Contact> = if (file.exists()) {
                // File exists, read its content
                val inputStream = FileInputStream(file)
                val reader = BufferedReader(InputStreamReader(inputStream))

                val jsonStringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    jsonStringBuilder.append(line)
                }

                reader.close()
                gson.fromJson(jsonStringBuilder.toString(), object : TypeToken<MutableList<Contact>>() {}.type)
            } else {
                // File does not exist, create a new list
                mutableListOf()
            }

            // Log current contact list for debugging
            Log.d("Tab3Fragment", "Before Adding: $contactList")

            // Find the contact with owner = true and add the new writing
            val ownerContact = contactList.find { it.owner }
            if (ownerContact != null) {
                ownerContact.writing.add(Writing(newWriting, false, 0))
            } else {
                Log.d("Tab3Fragment", "No owner contact found")
            }

            // Write the updated contact list back to JSON file
            val json = gson.toJson(contactList)
            context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }

            // Log updated contact list for debugging
            Log.d("Tab3Fragment", "After Adding: $contactList")

            // Log the contact list again after updating for debugging
            logContactList()

        } catch (e: Exception) {
            Log.e("Tab3Fragment", "Error saving new writing: ${e.message}")
        }
    }



    private fun logContactList() {
        val filename = "contact.json"
        val gson = Gson()

        // Construct the file path in internal storage
        val file = File(requireContext().filesDir, filename)

        if (file.exists()) {
            val contactList: List<Contact> = file.inputStream().bufferedReader().use {
                gson.fromJson(it, object : TypeToken<List<Contact>>() {}.type)
            }

            // Log the contact list for debugging
            Log.d("Tab3Fragment", "Contact List: $contactList")
        } else {
            Log.d("Tab3Fragment", "Contact file does not exist.")
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data and update UI when fragment resumes
        val contactList = readContactsFromJson(requireContext())
        Log.d("Tab3Fragment", "Read from Json: $contactList")
        populateWritings(contactList, writingsContainer)
    }

    fun readContactsFromJson(context: Context): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val gson = Gson()
        try {
            // Construct the file path in internal storage
            val filename = "contact.json"
            val file = File(context.filesDir, filename)

            // Check if the file exists
            if (file.exists()) {
                // Open an InputStream to read the file
                val inputStream = FileInputStream(file)
                val reader = BufferedReader(InputStreamReader(inputStream))

                // Read the JSON data into a StringBuilder
                val jsonStringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    jsonStringBuilder.append(line)
                }
                reader.close()

                if (jsonStringBuilder.isEmpty()) {
                    // File is empty, read from raw resource
                    loadContactsFromRawResource(context, contacts)
                    Log.d("Tab3Fragment", "Loaded contacts from raw resource as internal storage was empty: $contacts")
                } else {
                    // Parse JSON using Gson
                    val type = object : TypeToken<List<Contact>>() {}.type
                    contacts.addAll(gson.fromJson(jsonStringBuilder.toString(), type))
                    Log.d("Tab3Fragment", "Loaded contacts from internal storage: $contacts")
                }
            } else {
                // File does not exist, read from raw resource
                loadContactsFromRawResource(context, contacts)
                Log.d("Tab3Fragment", "Loaded contacts from raw resource as file did not exist: $contacts")
            }
        } catch (e: Exception) {
            Log.e("Tab3Fragment", "Error reading contacts from JSON: ${e.message}")
        }
        return contacts
    }

    private fun loadContactsFromRawResource(context: Context, contacts: MutableList<Contact>) {
        val gson = Gson()
        try {
            val raw = context.resources.openRawResource(R.raw.contact)
            val reader = BufferedReader(InputStreamReader(raw))
            val jsonStringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                jsonStringBuilder.append(line)
            }
            reader.close()

            val type = object : TypeToken<List<Contact>>() {}.type
            contacts.addAll(gson.fromJson(jsonStringBuilder.toString(), type))

            // Save the loaded contacts to internal storage
            val filename = "contact.json"
            val json = gson.toJson(contacts)
            context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
        } catch (e: Exception) {
            Log.e("Tab3Fragment", "Error loading contacts from raw resource: ${e.message}")
        }
    }


    private fun populateWritings(contactList: List<Contact>, container: LinearLayout) {
        container.removeAllViews() // Clear the container before populating
        for (contact in contactList) {
            for (writing in contact.writing) {
                loadLikedState(contact) // Load liked state for each writing
                val writingView = createWritingView(contact, writing)
                container.addView(writingView)
            }
        }
    }

    private fun createWritingView(contact: Contact, writing: Writing): View {
        val context = requireContext()

        // Create a LinearLayout to hold the entire writing item
        val mainLayout = LinearLayout(context)
        val mainLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setBackgroundResource(R.drawable.outer_box_background)
        mainLayout.layoutParams = mainLayoutParams
        mainLayoutParams.setMargins(0, 10, 0, 0) // Add margins between writings

        /*mainLayoutParams.setMargins(5, 10, 5, 40) // Add margins between writings
        mainLayout.layoutParams = mainLayoutParams
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setBackgroundResource(R.drawable.background_rounded_gray) // Rounded background
*/
        val innerLayout = RelativeLayout(context)
        innerLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        innerLayout.setBackgroundResource(R.drawable.background_rounded_gray)
        innerLayout.setPadding(50, 50, 50, 50)

        val textView = TextView(context)
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textView.text = writing.text
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23f)
        textView.setPadding(50, 50, 50, 150)
        // Set TextView to be not clickable and focusable
        textView.isClickable = false
        textView.isFocusable = false

        innerLayout.addView(textView)

        // Create heart button
        val heartButton = ImageButton(context)
        val heartButtonParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        heartButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        heartButtonParams.addRule(RelativeLayout.ALIGN_PARENT_END)
        heartButton.setImageResource(
            if (writing.isLiked) R.drawable.ic_heart_liked else R.drawable.ic_heart_unliked
        )
        heartButton.setBackgroundResource(android.R.color.transparent) // Make button background transparent
        heartButton.setPadding(16, 16, 16, 16) // Adjust padding as needed
        heartButton.layoutParams = heartButtonParams

        var isLiked = writing.isLiked

        heartButton.setOnClickListener {
            isLiked = !isLiked
            heartButton.setImageResource(
                if (isLiked) R.drawable.ic_heart_liked else R.drawable.ic_heart_unliked
            )
            writing.isLiked = isLiked
            saveLikedState(contact, writing)
        }

        // Add heart button to heartButtonLayout
        innerLayout.addView(heartButton)


        mainLayout.addView(innerLayout)

        // Create horizontal LinearLayout to hold profile icon and author name
        val authorLayout = LinearLayout(context)
        authorLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        authorLayout.orientation = LinearLayout.HORIZONTAL
        authorLayout.gravity = Gravity.CENTER_VERTICAL
        authorLayout.setPadding(50, 25, 50, 50)

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
        authorTextView.textSize = 15f
        authorTextView.setPadding(16, 0, 20, 0) // Adjust padding as needed
        authorTextView.setOnClickListener {
            showContactDialog(contact)
        }
        authorLayout.addView(authorTextView)

        // Add authorLayout (profile icon + author name) to mainLayout
        mainLayout.addView(authorLayout)

        // Add heartButtonLayout to mainLayout
        //mainLayout.addView(heartButtonLayout)

        return mainLayout
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

        // Load contact image using Glide
        Glide.with(requireContext())
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
