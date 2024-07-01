package com.example.madcamp_week1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.madcamp_week1.R
import com.google.android.material.appbar.MaterialToolbar

class LikedWritingsFragment : Fragment() {

    private lateinit var toolbar: MaterialToolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.liked_writings, container, false)

        toolbar = view.findViewById(R.id.topAppBarLikedWritings)

        // Set navigation click listener for back button
        val backButton = view.findViewById<ImageButton>(R.id.btn_back)
        backButton.setOnClickListener {
            requireActivity().onBackPressed() // Navigate back to previous fragment/activity
        }

        // You can dynamically add items to liked_writings_container here if needed

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up toolbar if needed (e.g., setting navigation icon, etc.)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed() // Navigate back to previous fragment/activity
        }
    }
}
