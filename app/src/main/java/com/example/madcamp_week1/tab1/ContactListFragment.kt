package com.example.madcamp_week1

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
class ContactListFragment : Fragment() {

    private lateinit var contactRecyclerView: RecyclerView
    private lateinit var contactAdapter: ContactAdapter
    private val deletedContacts = mutableListOf<Contact>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ContactListFragment", "onCreateView called")
        val view = inflater.inflate(R.layout.fragment_contact_list, container, false)

        // RecyclerView 설정
        contactRecyclerView = view.findViewById(R.id.recycler_view_contacts)
        if (contactRecyclerView == null) {
            Log.e("ContactListFragment", "RecyclerView is null")
        } else {
            Log.d("ContactListFragment", "RecyclerView found")
        }

        contactRecyclerView.layoutManager = LinearLayoutManager(context)
        contactAdapter = ContactAdapter(getContactList().toMutableList())
        contactRecyclerView.adapter = contactAdapter

        Log.d("ContactListFragment", "Adapter set")

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
                val deletedContact = contactAdapter.removeItem(position)
                deletedContacts.add(deletedContact)
                Log.d("ContactListFragment", "Contact deleted: $deletedContact")
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
                                itemView.left + 3*dX,
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
                                itemView.right + 3*dX,
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
            val contactType = object : TypeToken<List<Contact>>() {}.type
            val contacts: List<Contact> = gson.fromJson(reader, contactType)
            Log.d("ContactListFragment", "Successfully read contacts from JSON")
            contacts
        } catch (e: Exception) {
            Log.e("ContactListFragment", "Error reading contacts from JSON", e)
            emptyList()
        }
    }
}
