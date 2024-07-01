package com.example.madcamp_week1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madcamp_week1.viewmodel.ContactViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
//import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
class ContactListFragment : Fragment() {

    private lateinit var contactRecyclerView: RecyclerView
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var searchView: SearchView

    private val contactViewModel: ContactViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ContactListFragment", "onCreateView called")
        val view = inflater.inflate(R.layout.fragment_contact_list, container, false)

        // RecyclerView 설정
        contactRecyclerView = view.findViewById(R.id.recycler_view_contacts)
        contactRecyclerView.layoutManager = LinearLayoutManager(context)
        contactAdapter = ContactAdapter(requireContext(), mutableListOf(), contactViewModel)
        contactRecyclerView.adapter = contactAdapter

        // LiveData 옵저버 설정
        contactViewModel.contactList.observe(viewLifecycleOwner, Observer { contacts ->
            contactAdapter.updateList(contacts)
        })

        // SearchView 설정
        searchView = view.findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredContacts = contactViewModel.filterContacts(newText ?: "")
                contactAdapter.updateList(filteredContacts)
                return true
            }
        })

        // 구분선 추가
        val dividerItemDecoration = DividerItemDecoration(requireContext(), R.drawable.divider)
        contactRecyclerView.addItemDecoration(dividerItemDecoration)

        // ItemTouchHelper 설정
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedContact = contactAdapter.contactList[position]
                if (!deletedContact.owner) {
                    removeContact(deletedContact)
                    Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                    Log.d("ContactListFragment", "Contact deleted: $deletedContact")
                } else {
                    // If contact is owner, show a toast message and reset the swipe
                    Toast.makeText(context, "Can't delete your information", Toast.LENGTH_SHORT).show()
                    contactAdapter.notifyItemChanged(position)
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val position = viewHolder.adapterPosition
                val contact = contactAdapter.contactList[position]

                return if (contact.owner) {
                    0 // Disable swipe for owner's contact
                } else {
                    super.getSwipeDirs(recyclerView, viewHolder)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val paint = Paint().apply {
                        color = Color.RED
                    }
                    val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)!!

                    val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
                    val iconTop = itemView.top + iconMargin
                    val iconBottom = iconTop + icon.intrinsicHeight

                    when {
                        dX > 0 -> { // 스와이프 오른쪽
                            val iconLeft = itemView.left + iconMargin
                            val iconRight = iconLeft + icon.intrinsicWidth
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                            c.drawRect(
                                itemView.left.toFloat(),
                                itemView.top.toFloat(),
                                itemView.left + dX,
                                itemView.bottom.toFloat(),
                                paint
                            )
                            icon.draw(c)
                        }
                        dX < 0 -> { // 스와이프 왼쪽
                            val iconRight = itemView.right - iconMargin
                            val iconLeft = iconRight - icon.intrinsicWidth
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                            c.drawRect(
                                itemView.right + dX,
                                itemView.top.toFloat(),
                                itemView.right.toFloat(),
                                itemView.bottom.toFloat(),
                                paint
                            )
                            icon.draw(c)
                        }
                        else -> { // 스와이프되지 않음
                            c.drawRect(0f, 0f, 0f, 0f, paint)
                        }
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(contactRecyclerView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadContactsFromSharedPreferences()
    }

    private fun getContactList(): List<Contact> {
        Log.d("ContactListFragment", "getContactList called")
        val contactList: List<Contact> = readContactsFromJson()
        Log.d("ContactListFragment", "Contact list created with size: ${contactList.size}")
        return contactList
    }

    private fun readContactsFromJson(): List<Contact> {
        return try {
            val inputStream = resources.openRawResource(R.raw.contact)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val gson = Gson()
            val contactType = object : com.google.gson.reflect.TypeToken<List<Contact>>() {}.type
            val contacts: List<Contact> = gson.fromJson(reader, contactType)
            Log.d("ContactListFragment", "Successfully read contacts from JSON")
            contacts
        } catch (e: Exception) {
            Log.e("ContactListFragment", "Error reading contacts from JSON", e)
            emptyList()
        }
    }

    private fun saveContactsToSharedPreferences() {
        try {
            val sharedPreferences = requireActivity().getSharedPreferences("contacts", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val json = gson.toJson(contactViewModel.contactList.value)
            editor.putString("contact_list", json)
            editor.apply()
        } catch (e: Exception) {
            Log.e("ContactListFragment", "Error saving contacts to SharedPreferences", e)
        }
    }

    private fun loadContactsFromSharedPreferences() {
        try {
            val sharedPreferences = requireActivity().getSharedPreferences("contacts", Context.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPreferences.getString("contact_list", null)
            if (json != null) {
                val contactType = object : TypeToken<List<Contact>>() {}.type
                val contacts: List<Contact> = gson.fromJson(json, contactType)
                contactViewModel.loadContacts(contacts)
            } else {
                contactViewModel.loadContacts(getContactList())
            }
        } catch (e: Exception) {
            Log.e("ContactListFragment", "Error loading contacts from SharedPreferences", e)
            contactViewModel.loadContacts(getContactList())
        }
    }

    fun removeContact(contact: Contact) {
        contactViewModel.removeContact(contact)
        saveContactsToSharedPreferences()
    }
}
