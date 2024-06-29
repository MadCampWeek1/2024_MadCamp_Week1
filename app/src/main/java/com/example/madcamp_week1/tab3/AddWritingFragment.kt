package com.example.madcamp_week1

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.madcamp_week1.databinding.FragmentAddWritingBinding

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

            // Save the writing or perform any other action (not implemented in this example)
            // For demonstration purposes, you can print the entered text
            println("New Writing: $newWriting")

            // Navigate back to the previous fragment (Tab3Fragment in this case)
            findNavController().popBackStack(R.id.notificationsFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
