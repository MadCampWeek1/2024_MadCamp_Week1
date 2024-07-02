package com.example.madcamp_week1.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madcamp_week1.Contact

class ContactViewModel : ViewModel() {
    val contactList = MutableLiveData<MutableList<Contact>>(mutableListOf())

    fun loadContacts(contacts: List<Contact>) {
        contactList.value = contacts.sortedWith(compareByDescending<Contact> { it.owner }.thenBy { it.name }).toMutableList()
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
        val currentList = contactList.value
        currentList?.let {
            it.remove(contact)
            contactList.value = it.sortedWith(compareByDescending<Contact> { it.owner }.thenBy { it.name }).toMutableList() // Trigger LiveData update with sorted list
        }
    }

    fun addContact(contact: Contact) {
        val currentList = contactList.value ?: mutableListOf()
        currentList.add(contact)
        contactList.value = currentList.sortedWith(compareByDescending<Contact> { it.owner }.thenBy { it.name }).toMutableList() // Trigger LiveData update with sorted list
    }
}
