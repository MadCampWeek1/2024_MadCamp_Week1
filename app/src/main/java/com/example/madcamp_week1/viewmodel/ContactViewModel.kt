package com.example.madcamp_week1.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madcamp_week1.Contact

class ContactViewModel : ViewModel() {
    val contactList = MutableLiveData<MutableList<Contact>>(mutableListOf())

    fun loadContacts(contacts: List<Contact>) {
        contactList.value = contacts.toMutableList()
    }

    fun filterContacts(query: String): List<Contact> {
        return if (query.isEmpty()) {
            contactList.value ?: mutableListOf()
        } else {
            contactList.value?.filter {
                it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
            } ?: mutableListOf()
        }
    }

    fun removeContact(contact: Contact) {
        val updatedList = contactList.value?.toMutableList()
        if (updatedList?.remove(contact) == true) {
            contactList.value = updatedList // Trigger LiveData update
        }
    }
}
