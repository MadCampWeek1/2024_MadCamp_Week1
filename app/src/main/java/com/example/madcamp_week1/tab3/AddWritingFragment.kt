package com.example.madcamp_week1

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.madcamp_week1.databinding.FragmentAddWritingBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class AddWritingFragment : Fragment() {

    private var _binding: FragmentAddWritingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddWritingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the hint message to be visible all the time
        binding.tvHint.visibility = View.VISIBLE

        // Set max length to 150 characters and set custom input filter
        val maxLength = 150
        binding.etWriting.filters = arrayOf(InputFilter.LengthFilter(maxLength))

        // Handle save button click
        binding.btnSave.setOnClickListener {
            // Get the entered writing text
            val newWriting = binding.etWriting.text.toString().trim()

            Log.d("AddWritingFragment", "New Writing: $newWriting") // Log newWriting value
            print(newWriting)

            val args = Bundle()
            args.putString("newWriting", newWriting)

            findNavController().navigate(
                R.id.action_addWritingFragment_to_tab3Fragment,
                args
            )
            //findNavController().popBackStack(R.id.notificationsFragment, false)
        }
        binding.btnBack.setOnClickListener {
            // Navigate back to the previous fragment (Tab3Fragment in this case)
            findNavController().popBackStack(R.id.notificationsFragment, false)
        }
    }

    private fun saveNewWriting(newWriting: String) {
        if (newWriting.isEmpty()) return

        val gson = Gson()

        val context = requireContext()

        // Ensure context is not null before proceeding
        context ?: return

        // Access the raw resource file
        val inputStream = context.resources.openRawResource(R.raw.contact)

        val contactList: MutableList<Contact> = try {
            inputStream.bufferedReader().use {
                gson.fromJson(it, object : TypeToken<MutableList<Contact>>() {}.type)
            }
        } catch (e: Exception) {
            Log.e("AddWritingFragment", "Error reading JSON file: ${e.message}")
            mutableListOf()
        }

        // Log current contact list for debugging
        Log.d("AddWritingFragment", "Before Adding: $contactList")

        // Find the contact with owner = true and add the new writing
        val ownerContact = contactList.find { it.owner }
        if (ownerContact != null) {
            ownerContact.writing.add(Writing(newWriting, false, 0))
        } else {
            Log.d("AddWritingFragment", "No owner contact found")
        }

        // Close the input stream
        inputStream.close()

        // Write the updated contact list back to JSON file
        try {
            // Re-open the output stream to write the updated JSON
            context.openFileOutput("contact.json", Context.MODE_PRIVATE).use {
                OutputStreamWriter(it).use { writer ->
                    gson.toJson(contactList, writer)
                }
            }
        } catch (e: Exception) {
            Log.e("AddWritingFragment", "Error writing JSON file: ${e.message}")
        }

        // Log updated contact list for debugging
        Log.d("AddWritingFragment", "After Adding: $contactList")

        // Log the contact list again after updating for debugging
        logContactList()
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
            Log.d("AddWritingFragment", "Contact List: $contactList")
        } else {
            Log.d("AddWritingFragment", "Contact file does not exist.")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
