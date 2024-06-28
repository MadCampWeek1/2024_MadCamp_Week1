package com.example.madcamp_week1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController

class HomeFragment : Fragment() {

    private val theme1Images = listOf(R.drawable.dog1, R.drawable.dog2, R.drawable.cat1, R.drawable.cat2, R.drawable.cat3, R.drawable.fish1, R.drawable.fish2)
    private val theme2Images = listOf(R.drawable.food1, R.drawable.food2, R.drawable.food3, R.drawable.food4, R.drawable.food5, R.drawable.food6, R.drawable.food7)
    private val theme3Images = listOf(R.drawable.people1, R.drawable.people2, R.drawable.people3, R.drawable.people4, R.drawable.people5, R.drawable.people6)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_images, container, false)

        // Setup buttons for different themes
        view.findViewById<Button>(R.id.btnTheme1).apply {
            setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.dog1, 0, 0) // Set image on top
            setOnClickListener {
                onCategoryItemClicked(R.id.action_homeFragment_to_themeDetailFragment, theme1Images)
            }
        }

        view.findViewById<Button>(R.id.btnTheme2).apply {
            setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.food1, 0, 0) // Set image on top
            setOnClickListener {
                onCategoryItemClicked(R.id.action_homeFragment_to_themeDetailFragment, theme2Images)
            }
        }

        // Add more buttons for additional themes
        // Example: For theme3Images and theme4Images
        view.findViewById<Button>(R.id.btnTheme3).apply {
            setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.people1, 0, 0) // Set image on top
            setOnClickListener {
                onCategoryItemClicked(R.id.action_homeFragment_to_themeDetailFragment, theme3Images)
            }
        }

        /*view.findViewById<Button>(R.id.btnTheme4).apply {
            setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.image4, 0, 0) // Set image on top
            setOnClickListener {
                onCategoryItemClicked(R.id.action_homeFragment_to_themeDetailFragment, theme4Images)
            }
        }*/

        // Add more buttons setups for additional themes as needed

        return view
    }

    private fun onCategoryItemClicked(actionId: Int, themeImages: List<Int>) {
        val imageIntArray = themeImages.toIntArray() // Convert List<Int> to IntArray

        val bundle = Bundle().apply {
            putIntArray("themeImages", imageIntArray)
        }
        view?.findNavController()?.navigate(actionId, bundle)
    }
}
