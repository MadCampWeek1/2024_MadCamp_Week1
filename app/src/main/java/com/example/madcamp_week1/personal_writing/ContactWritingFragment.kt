package com.example.madcamp_week1

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

class ContactWritingsFragment : Fragment() {

    private lateinit var writingsContainer: LinearLayout
    private var contact: Contact? = null
    private var isEditMode = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_writings, container, false)
        writingsContainer = view.findViewById(R.id.writings_container)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar3)
        val contactName = arguments?.getString("contactName")
        toolbar.title = contactName
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setHasOptionsMenu(true) // Ensure this line is present

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contactJson = arguments?.getString("contact")
        contact = Gson().fromJson(contactJson, Contact::class.java)

        contact?.let {
            // Load liked state from JSON file
            loadLikedStateFromJson()
            populateWritings(it.writing, it)
        }
    }



    private fun populateWritings(writings: List<Writing>, contact: Contact) {
        writingsContainer.removeAllViews()
        for (writing in writings) {
            val itemView = LayoutInflater.from(context).inflate(R.layout.writing_item_layout, writingsContainer, false)
            val textView = itemView.findViewById<TextView>(R.id.text_view)
            val heartButton = itemView.findViewById<ImageButton>(R.id.heart_button)
            val authorLayout = itemView.findViewById<LinearLayout>(R.id.author_layout)
            val profileImageView = itemView.findViewById<ImageView>(R.id.person_icon)
            val authorTextView = itemView.findViewById<TextView>(R.id.author_text_view)
            val deleteButton = itemView.findViewById<ImageButton>(R.id.delete_button)

            textView.text = writing.text
            textView.textSize = 18f

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
                saveLikedStateToJson(contact, writing)
            }

            Glide.with(this)
                .load(contact.profileImage)
                .placeholder(R.drawable.ic_person)
                .into(profileImageView)

            authorTextView.text = contact.name
            authorLayout.setOnClickListener {
                showContactDialog(contact)
            }

            // Show delete button only if in edit mode and owner is true
            if (isEditMode && contact.owner) {
                Log.d("contactwritingfragment", "Contact List: $isEditMode")
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener {
                    showDeleteConfirmationDialog(contact, writing)
                }
            } else {
                deleteButton.visibility = View.GONE
            }

            writingsContainer.addView(itemView)
        }
    }

    private fun showDeleteConfirmationDialog(contact: Contact, writing: Writing) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("삭제하겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                contact.writing.remove(writing)
                saveContactToJson(contact)
                populateWritings(contact.writing, contact)
            }
            .setNegativeButton("취소", null)
        builder.create().show()
    }

    private fun saveContactToJson(contact: Contact) {
        val gson = Gson()
        val context = requireContext()

        try {
            val filename = "contact.json"
            val file = File(context.filesDir, filename)

            val contactList: MutableList<Contact> = if (file.exists()) {
                try {
                    val inputStream = FileInputStream(file)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val jsonStringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        jsonStringBuilder.append(line)
                    }
                    reader.close()
                    gson.fromJson(jsonStringBuilder.toString(), object : TypeToken<MutableList<Contact>>() {}.type)
                } catch (e: JsonSyntaxException) {
                    Log.e("ContactWritingsFragment", "Error parsing JSON: ${e.message}")
                    mutableListOf<Contact>()
                }
            } else {
                mutableListOf()
            }

            val contactIndex = contactList.indexOfFirst { it.name == contact.name }
            if (contactIndex != -1) {
                contactList[contactIndex] = contact
            } else {
                contactList.add(contact)
            }

            val json = gson.toJson(contactList)
            context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }

        } catch (e: IOException) {
            Log.e("ContactWritingsFragment", "Error saving contact: ${e.message}")
        } catch (e: Exception) {
            Log.e("ContactWritingsFragment", "Unexpected error: ${e.message}")
        }
    }

    private fun saveLikedStateToJson(contact: Contact, writing: Writing) {
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

        } catch (e: Exception) {
            Log.e("ContactWritingsFragment", "Error saving liked state: ${e.message}")
        }
    }

    private fun loadLikedStateFromJson() {
        val gson = Gson()
        val context = requireContext()

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

                val contactList: List<Contact> = gson.fromJson(jsonStringBuilder.toString(), object : TypeToken<List<Contact>>() {}.type)
                contactList.forEach { savedContact ->
                    if (savedContact.name == contact?.name) {
                        contact?.writing?.forEach { writing ->
                            val savedWriting = savedContact.writing.find { it.text == writing.text }
                            if (savedWriting != null) {
                                writing.isLiked = savedWriting.isLiked
                                writing.likeNum = savedWriting.likeNum
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ContactWritingsFragment", "Error loading liked state: ${e.message}")
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
        val closeButton: ImageButton = dialogView.findViewById(R.id.btn_close)

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
