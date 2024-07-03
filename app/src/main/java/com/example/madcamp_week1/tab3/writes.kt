package com.example.madcamp_week1

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
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
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class Tab3Fragment : Fragment() {

    private lateinit var writingsContainer: LinearLayout
    private var showingLikedOnly = false
    private lateinit var contactList: List<Contact>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_geul, container, false)

        // Find the TextView in the Toolbar and set a click listener
        val appTitle: TextView = view.findViewById(R.id.appTitle)
        appTitle.setOnClickListener {
            // Navigate to IntroActivity
            val intent = Intent(requireActivity(), IntroActivity::class.java)
            startActivity(intent)
        }

        val upArrowButton: ImageButton = view.findViewById(R.id.btn_up_arrow)
        upArrowButton.setOnClickListener {
            Log.d("upbottom", "Before Adding")

            // Navigate to ViewPagerFragment
            findNavController().navigate(R.id.action_tab3Fragment_to_viewPagerFragment)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        writingsContainer = view.findViewById(R.id.writings_container)

        view.findViewById<View>(R.id.fab_add).setOnClickListener {
            findNavController().navigate(R.id.action_tab3Fragment_to_addWritingFragment)
        }

        val heartButton = view.findViewById<ImageButton>(R.id.btn_heart)
        var isHeartLiked = false // 초기 상태 설정

        heartButton.setOnClickListener {
            // 색상 변경 기능
            isHeartLiked = !isHeartLiked
            heartButton.setImageResource(
                if (isHeartLiked) R.drawable.ic_heart_liked else R.drawable.ic_heart_unliked
            )

            // 기존 기능 유지
            showingLikedOnly = !showingLikedOnly
            if (showingLikedOnly) {
                val likedWritings = getLikedWritings()
                populateWritings(likedWritings)
            } else {
                populateWritings(contactList.flatMap { it.writing })
            }
        }

        val args = arguments
        if (args != null && args.containsKey("newWriting")) {
            val newWriting = args.getString("newWriting", "")
            if (!newWriting.isNullOrEmpty()) {
                saveNewWriting(newWriting)
            }
        }

        contactList = readContactsFromJson(requireContext())
        populateWritings(contactList.flatMap { it.writing })
    }

    private fun getLikedWritings(): List<Writing> {
        return contactList.flatMap { it.writing }.filter { it.isLiked }
    }

    private fun saveNewWriting(newWriting: String) {
        if (newWriting.isEmpty()) return

        val gson = Gson()
        val context = requireContext()

        try {
            val filename = "contact.json"
            val file = File(context.filesDir, filename)

            val contactList: MutableList<Contact> = if (file.exists()) {
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
                mutableListOf()
            }

            val ownerContact = contactList.find { it.owner }
            if (ownerContact != null) {
                ownerContact.writing.add(Writing(newWriting, false, 0))
            } else {
                Log.d("Tab3Fragment", "No owner contact found")
            }

            val json = gson.toJson(contactList)
            context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }

            logContactList()

        } catch (e: Exception) {
            Log.e("Tab3Fragment", "Error saving new writing: ${e.message}")
        }
    }

    private fun logContactList() {
        val filename = "contact.json"
        val gson = Gson()

        val file = File(requireContext().filesDir, filename)

        if (file.exists()) {
            val contactList: List<Contact> = file.inputStream().bufferedReader().use {
                gson.fromJson(it, object : TypeToken<List<Contact>>() {}.type)
            }

            Log.d("Tab3Fragment", "Contact List: $contactList")
        } else {
            Log.d("Tab3Fragment", "Contact file does not exist.")
        }
    }

    override fun onResume() {
        super.onResume()
        contactList = readContactsFromJson(requireContext())
        populateWritings(contactList.flatMap { it.writing })
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
                loadContactsFromRawResource(context, contacts)
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

            val filename = "contact.json"
            val json = gson.toJson(contacts)
            context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
        } catch (e: Exception) {
            Log.e("Tab3Fragment", "Error loading contacts from raw resource: ${e.message}")
        }
    }

    private fun populateWritings(writings: List<Writing>) {
        writingsContainer.removeAllViews()
        for (writing in writings) {
            val contact = findContactForWriting(writing)
            val writingView = createWritingView(contact, writing)
            writingsContainer.addView(writingView)
        }
    }

    private fun findContactForWriting(writing: Writing): Contact? {
        return contactList.find { it.writing.contains(writing) }
    }

    private fun createWritingView(contact: Contact?, writing: Writing): View {
        val context = requireContext()

        // Main layout for the entire writing view
        val mainLayout = LinearLayout(context)
        val mainLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setBackgroundResource(R.drawable.outer_box_background)
        mainLayout.layoutParams = mainLayoutParams
        mainLayoutParams.setMargins(0, 10, 0, 150)

        // Layout for the profile image and author name
        val authorLayout = LinearLayout(context)
        authorLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        authorLayout.orientation = LinearLayout.HORIZONTAL
        authorLayout.gravity = Gravity.CENTER_VERTICAL
        authorLayout.setPadding(30, 25, 50, 10) // Adjust padding as needed

        // Profile image view
        val profileIcon = ImageView(context)
        Glide.with(this)
            .load(contact?.profileImage ?: R.drawable.ic_person) // Use placeholder if no profile image
            .placeholder(R.drawable.ic_person)
            .into(profileIcon)
        profileIcon.layoutParams = LinearLayout.LayoutParams(
            100,
            100
        )
        profileIcon.scaleType = ImageView.ScaleType.CENTER_CROP
        authorLayout.addView(profileIcon)

        // Add click listener to show dialog
        profileIcon.setOnClickListener {
            if (contact != null) {
                showContactDialog(contact)
            }
        }

        // Author name text view
        val authorTextView = TextView(context)
        authorTextView.text = contact?.name ?: "Unknown Author" // Use default text if no contact
        authorTextView.textSize = 18f // Adjust text size as needed
        authorTextView.setPadding(16, 0, 0, 0) // Adjust padding as needed
        authorLayout.addView(authorTextView)

        // Add click listener to show dialog
        authorTextView.setOnClickListener {
            if (contact != null) {
                showContactDialog(contact)
            }
        }

        mainLayout.addView(authorLayout)

        // Layout for the main content with text and heart button
        val innerLayout = RelativeLayout(context)
        innerLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        innerLayout.setBackgroundResource(R.drawable.background_rounded_gray)
        innerLayout.setPadding(50, 50, 50, 50)

        // Text view for writing content
        val textView = TextView(context)
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textView.text = writing.text
        textView.setTextSize(20f)
        textView.setPadding(0, 0, 0, 150) // Adjust padding as needed
        textView.isClickable = false
        textView.isFocusable = false
        innerLayout.addView(textView)

        // Heart button
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
        heartButton.setBackgroundResource(android.R.color.transparent)
        heartButton.setPadding(16, 16, 16, 16)
        heartButton.layoutParams = heartButtonParams

        heartButton.setOnClickListener {
            writing.isLiked = !writing.isLiked
            heartButton.setImageResource(
                if (writing.isLiked) R.drawable.ic_heart_liked else R.drawable.ic_heart_unliked
            )
            saveLikedStateToJson(contact, writing)
        }

        innerLayout.addView(heartButton)

        mainLayout.addView(innerLayout)

        return mainLayout
    }

    private fun saveLikedStateToJson(contact: Contact?, writing: Writing) {
        val gson = Gson()
        val context = requireContext()

        try {
            val filename = "contact.json"
            val file = File(context.filesDir, filename)

            val contactList: MutableList<Contact> = if (file.exists()) {
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
                mutableListOf()
            }

            if (contact != null) {
                val contactIndex = contactList.indexOfFirst { it.name == contact.name }
                if (contactIndex != -1) {
                    val contactToUpdate = contactList[contactIndex]
                    val writingIndex = contactToUpdate.writing.indexOfFirst { it.text == writing.text }
                    if (writingIndex != -1) {
                        contactToUpdate.writing[writingIndex] = writing
                    }
                }

                val json = gson.toJson(contactList)
                context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                    it.write(json.toByteArray())
                }
            }
        } catch (e: Exception) {
            Log.e("Tab3Fragment", "Error saving liked state: ${e.message}")
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
        val closeButton: ImageButton = dialogView.findViewById(R.id.btn_close) // Add this line

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

        closeButton.setOnClickListener { // Add this block
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
