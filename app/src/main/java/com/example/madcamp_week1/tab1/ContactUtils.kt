package com.example.madcamp_week1

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*

object ContactUtils {
    private const val FILENAME = "contact.json"
    private val gson = Gson()

    fun readContactsFromJson(context: Context): List<Contact> {
        val contacts = mutableListOf<Contact>()
        try {
            val file = File(context.filesDir, FILENAME)
            if (file.exists()) {
                val inputStream = FileInputStream(file)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonStringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    jsonStringBuilder.append(line)
                }
                reader.close()
                val type = object : TypeToken<List<Contact>>() {}.type
                contacts.addAll(gson.fromJson(jsonStringBuilder.toString(), type))
            }
        } catch (e: Exception) {
            Log.e("ContactUtils", "Error reading contacts from JSON: ${e.message}")
        }
        return contacts
    }

    fun saveContactsToJson(context: Context, contacts: List<Contact>) {
        try {
            val json = gson.toJson(contacts)
            val file = File(context.filesDir, FILENAME)
            if (!file.exists()) {
                file.createNewFile()
            }
            context.openFileOutput(FILENAME, Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
            Log.d("ContactUtils", "Successfully saved contacts to JSON")
        } catch (e: IOException) {
            Log.e("ContactUtils", "I/O Error saving contacts to JSON: ${e.message}")
        } catch (e: Exception) {
            Log.e("ContactUtils", "Error saving contacts to JSON: ${e.message}")
        }
    }
}
