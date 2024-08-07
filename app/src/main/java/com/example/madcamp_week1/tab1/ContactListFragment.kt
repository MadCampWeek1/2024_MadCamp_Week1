package com.example.madcamp_week1

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*

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

        // Find the TextView in the Toolbar and set a click listener
        val appTitle: TextView = view.findViewById(R.id.appTitle)
        appTitle.setOnClickListener {
            // Navigate to IntroActivity
            val intent = Intent(activity, IntroActivity::class.java)
            startActivity(intent)
            // Finish the current activity
            activity?.finish()
        }

        // Add new contact button
        val addButton: FloatingActionButton = view.findViewById(R.id.fab_add)
        addButton.setOnClickListener {
            showAddContactDialog()
        }

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
        loadContactsFromJson()
    }

    private fun loadContactsFromJson() {
        try {
            val contacts = ContactUtils.readContactsFromJson(requireContext())
            contactViewModel.loadContacts(contacts)
        } catch (e: Exception) {
            Log.e("ContactListFragment", "Error loading contacts from JSON", e)
            contactViewModel.loadContacts(emptyList())
        }
    }

    fun removeContact(contact: Contact) {
        contactViewModel.removeContact(contact)
        saveContactsToJson()
    }

    private fun saveContactsToJson() {
        try {
            val contacts = contactViewModel.contactList.value ?: return
            ContactUtils.saveContactsToJson(requireContext(), contacts)
        } catch (e: Exception) {
            Log.e("ContactListFragment", "Error saving contacts to JSON", e)
        }
    }

    private fun showAddContactDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_contact)

        val nameEditText = dialog.findViewById<EditText>(R.id.edit_text_name)
        val phoneEditText = dialog.findViewById<EditText>(R.id.edit_text_phone)
        val ageEditText = dialog.findViewById<EditText>(R.id.edit_text_age)
        val radioGroupGender = dialog.findViewById<RadioGroup>(R.id.radio_group_gender)
        val maleRadioButton = dialog.findViewById<RadioButton>(R.id.radio_male)
        val femaleRadioButton = dialog.findViewById<RadioButton>(R.id.radio_female)
        val introductionEditText = dialog.findViewById<EditText>(R.id.edit_text_introduction)
        val saveButton = dialog.findViewById<Button>(R.id.button_save_contact)

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val age = ageEditText.text.toString().trim().toIntOrNull() ?: 0
            val gender = when (radioGroupGender.checkedRadioButtonId) {
                R.id.radio_male -> "남"
                R.id.radio_female -> "여"
                else -> ""
            }
            val introduction = introductionEditText.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || gender.isEmpty() || introduction.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                val newContact = Contact(
                    name = name,
                    phone = phone,
                    profileImage = R.drawable.ic_person.toString(),
                    isPendingDelete = false,
                    owner = false,
                    age = age,
                    gender = gender,
                    introduction = introduction,
                    writing = mutableListOf()
                )

                contactViewModel.addContact(newContact)
                saveContactsToJson()
                dialog.dismiss()
                Toast.makeText(requireContext(), "Contact added successfully", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()

        // Set the dialog window size to a percentage of the screen size
        val window = dialog.window
        val metrics = resources.displayMetrics
        val width = (metrics.widthPixels * 0.9).toInt() // 90% of screen width
        val height = (metrics.heightPixels * 0.7).toInt() // 70% of screen height

        if (window != null) {
            window.setLayout(width, height)
        }
    }
}
