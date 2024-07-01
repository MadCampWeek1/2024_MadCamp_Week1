package com.example.madcamp_week1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ImageDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_detail, container, false)

        val zoomableImageView = view.findViewById<ZoomableImageView>(R.id.imageDetail)
        val imageResId = arguments?.getInt("imageResId")

        if (imageResId != null) {
            zoomableImageView.setImageResource(imageResId)
        }

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }
}
