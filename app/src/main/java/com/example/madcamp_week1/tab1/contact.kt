package com.example.madcamp_week1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class Tab2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact_list, container, false)
    }
}

data class Contact(
    val name: String,
    val phone: String,
    val profileImage: String,
    val gender: String,
    val age: Int,
    val introduction: String,
    val writing: MutableList<String> = mutableListOf(),
    var isPendingDelete: Boolean = false,
    val owner: Boolean
)
