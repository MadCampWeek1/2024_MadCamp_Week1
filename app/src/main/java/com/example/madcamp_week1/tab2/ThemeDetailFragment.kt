package com.example.madcamp_week1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ThemeDetailFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_theme_detail, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(activity, 3)

        // Retrieve the images from arguments
        val themeImages = arguments?.getIntArray("themeImages")?.toList() ?: emptyList()
        imageAdapter = ImageAdapter(requireContext(), themeImages)
        recyclerView.adapter = imageAdapter

        // Setup toolbar
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.apply {
            title = "Theme Detail"
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        return view
    }
}
