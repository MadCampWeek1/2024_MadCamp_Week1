package com.example.madcamp_week1

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class WritingPagerAdapter(
    private val contacts: List<Contact>,
    private val context: Context,
    private val onAuthorClickListener: ViewPagerFragment
) : RecyclerView.Adapter<WritingPagerAdapter.WritingViewHolder>() {

    // Map to store current index for each contact
    private val currentIndexMap = mutableMapOf<Int, Int>()

    init {
        // Initialize currentIndex for each contact to 0
        contacts.forEachIndexed { index, contact ->
            currentIndexMap[index] = 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WritingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.swipe_writing, parent, false)
        return WritingViewHolder(view)
    }

    override fun onBindViewHolder(holder: WritingViewHolder, position: Int) {
        holder.bind(contacts[position], currentIndexMap[position] ?: 0)
    }

    override fun getItemCount(): Int = contacts.size

    fun setCurrentIndex(contactPosition: Int, newIndex: Int) {
        currentIndexMap[contactPosition] = newIndex
        notifyDataSetChanged()
    }

    inner class WritingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val authorTextView: TextView = itemView.findViewById(R.id.author_text_view)
        private val heartButton: ImageButton = itemView.findViewById(R.id.heart_button)
        private val writingTextView: TextView = itemView.findViewById(R.id.text_view)
        private val profileImageView: ImageView = itemView.findViewById(R.id.person_icon)

        fun bind(contact: Contact, currentIndex: Int) {
            authorTextView.text = contact.name // Display contact's name

            // Check if currentIndex is within bounds of writings
            if (currentIndex >= 0 && currentIndex < contact.writing.size) {
                val writing = contact.writing[currentIndex]
                writingTextView.text = writing.text

                // Handle like button
                heartButton.setImageResource(if (writing.isLiked) R.drawable.ic_heart_liked else R.drawable.ic_heart_unliked)
                heartButton.setOnClickListener {
                    writing.isLiked = !writing.isLiked
                    heartButton.setImageResource(if (writing.isLiked) R.drawable.ic_heart_liked else R.drawable.ic_heart_unliked)
                    saveLikedState(context, contact, writing)
                }

                // Load profile image
                Glide.with(context)
                    .load(contact.profileImage)
                    .placeholder(R.drawable.ic_contact_placeholder)
                    .error(R.drawable.ic_contact_placeholder)
                    .into(profileImageView)

                // Handle profile image click
                profileImageView.setOnClickListener {
                    onAuthorClickListener.onAuthorClick(contact)
                }

                // Handle author name click
                authorTextView.setOnClickListener {
                    onAuthorClickListener.onAuthorClick(contact)
                }
            } else {
                // Handle case where currentIndex is out of bounds
                writingTextView.text = ""
                heartButton.setImageResource(R.drawable.ic_heart_unliked)
                heartButton.setOnClickListener(null)
            }
        }
    }

    private fun saveLikedState(context: Context, contact: Contact, writing: Writing) {
        val gson = Gson()

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
            context.openFileOutput(filename, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(json.toByteArray())
            }

        } catch (e: Exception) {
            Log.e("WritingPagerAdapter", "Error saving liked state: ${e.message}")
        }
    }

}
