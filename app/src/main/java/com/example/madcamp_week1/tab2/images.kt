package com.example.madcamp_week1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter

    // Array of all image resource IDs
    private val allImageList = listOf(
        R.drawable.dog1,
        // Add more image resource IDs for different themes
    )

    // Arrays for different themes
    private val theme1Images = listOf(R.drawable.dog1, )
    private val theme2Images = listOf(R.drawable.food1, )
    // Define more themes as needed

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_images, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(activity, 3) // Adjust span count as needed

        // Initially show all images
        imageAdapter = ImageAdapter(requireContext(), allImageList)
        recyclerView.adapter = imageAdapter

        // Setup buttons for different themes
        view.findViewById<Button>(R.id.btnTheme1).setOnClickListener {
            imageAdapter.updateData(theme1Images)
        }

        view.findViewById<Button>(R.id.btnTheme2).setOnClickListener {
            imageAdapter.updateData(theme2Images)
        }

        // Add more button setups for additional themes

        return view
    }
}
