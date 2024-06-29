// app/src/main/java/com/example/madcamp_week1/viewmodel/ContactViewModel.kt

package com.example.madcamp_week1.viewmodel

import androidx.lifecycle.ViewModel
import com.example.madcamp_week1.Contact

class ContactViewModel : ViewModel() {
    var contactList: MutableList<Contact> = mutableListOf()

    // Method to load contacts if the list is empty
    fun loadContacts(contacts: List<Contact>) {
        if (contactList.isEmpty()) {
            contactList.addAll(contacts)
        }
    }
}
