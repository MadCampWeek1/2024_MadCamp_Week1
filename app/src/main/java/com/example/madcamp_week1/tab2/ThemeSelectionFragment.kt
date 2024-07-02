package com.example.madcamp_week1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ThemeSelectionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_images, container, false)

        view.findViewById<View>(R.id.btnTheme1).setOnClickListener {
            navigateToThemeDetailFragment("반려동물", intArrayOf(R.drawable.dog1, R.drawable.dog2, R.drawable.cat1))
        }

        view.findViewById<View>(R.id.btnTheme2).setOnClickListener {
            navigateToThemeDetailFragment("음식 사진 모음", intArrayOf(R.drawable.food1, R.drawable.food2, R.drawable.food3))
        }

        view.findViewById<View>(R.id.btnTheme3).setOnClickListener {
            navigateToThemeDetailFragment("소중한 사람들과", intArrayOf(R.drawable.people1, R.drawable.people2, R.drawable.people3))
        }

        return view
    }

    private fun navigateToThemeDetailFragment(title: String, images: IntArray) {
        val bundle = Bundle().apply {
            putIntArray("themeImages", images)
            putString("themeTitle", title)
        }
        findNavController().navigate(R.id.action_themeDetailFragment_to_imageDetailFragment, bundle)
    }
}
