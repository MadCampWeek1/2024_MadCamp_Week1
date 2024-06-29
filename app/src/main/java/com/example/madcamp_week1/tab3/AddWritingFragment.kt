package com.example.madcamp_week1

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
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

            saveNewWriting(newWriting)

            // Save the writing or perform any other action (not implemented in this example)
            // For demonstration purposes, you can print the entered text

            // Navigate back to the previous fragment (Tab3Fragment in this case)
            findNavController().popBackStack(R.id.notificationsFragment, false)
        }
        binding.btnBack.setOnClickListener {
            // Navigate back to the previous fragment (Tab3Fragment in this case)
            findNavController().popBackStack(R.id.notificationsFragment, false)
        }
    }

    private fun saveNewWriting(newWriting: String) {
        if (newWriting.isEmpty()) return

        val filename = "contact.json"
        val gson = Gson()

        val context = requireContext()

        // Ensure context is not null before proceeding
        context ?: return

        // Construct the file path in internal storage
        val file = File(context.filesDir, filename)

        val contactList: MutableList<Contact> = if (file.exists()) {
            file.inputStream().bufferedReader().use {
                gson.fromJson(it, object : TypeToken<MutableList<Contact>>() {}.type)
            }
        } else {
            mutableListOf()
        }

        // Read the existing contact list from JSON file

        // Find the contact with owner = true and add the new writing
        val ownerContact = contactList.find { it.owner }
        ownerContact?.let {
            it.writing.add(Writing(newWriting, false, 0)) // Add newWriting to the writing list of the owner contact
        }

        // Write the updated contact list back to JSON file
        file.writer().use {
            gson.toJson(contactList, it)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
