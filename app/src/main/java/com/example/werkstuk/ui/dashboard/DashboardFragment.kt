package com.example.werkstuk.ui.dashboard

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.werkstuk.Adapter
import com.example.werkstuk.R
import com.example.werkstuk.TodoItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private var exampleList = generateDummyList(0)
    private val adapter = Adapter(exampleList)
    private val keys = ArrayList<String>()

    private var added = false

    // Write a message to the database
    private val database = FirebaseDatabase.getInstance("https://werkstuk-android-c4e4f-default-rtdb.europe-west1.firebasedatabase.app/")

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val rv: RecyclerView = root.findViewById(R.id.todoRecyclerView)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(root.context)
        rv.setHasFixedSize(true)

        val viewList = layoutInflater.inflate(R.layout.todo_layout, null)
        val check = viewList.findViewById<CheckBox>(R.id.todoCheckBox)

        // Read from the database
        var myRef = database.getReference("todos")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (added) return
                val values: Map<String, Object> = dataSnapshot.value as Map<String, Object>

                //keys.clear()
                //exampleList.clear()

                values.forEach {
                    keys.add(it.key)
                }

                keys.forEach {
                    var ref2 = database.getReference("todos/$it")
                    ref2.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val values: Map<String, Object> = dataSnapshot.value as Map<String, Object>

                            values.forEach {
                                if (it.key == "songname") {
                                    val newItem = TodoItem(0, it.value.toString())
                                    exampleList.add(newItem)
                                    adapter.notifyItemInserted(exampleList.size - 1)
                                    Log.d(TAG, "Value is: $it")
                                }
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException())
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        val addItem: FloatingActionButton = root.findViewById(R.id.addButton)
        addItem.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.new_todo, null)
            val window = PopupWindow(view, 1000, 700, true)

            window.contentView = view
            window.showAsDropDown(addItem, -500, -500)

            val newTodoButton = view.findViewById<Button>(R.id.newTodoButton)
            val cancelButton = view.findViewById<Button>(R.id.cancelButton)
            val textField = view.findViewById<EditText>(R.id.newTodoText)

            newTodoButton.setOnClickListener{
                window.dismiss()
                addItem(root, textField.text.toString())
            }

            cancelButton.setOnClickListener{
                window.dismiss()
            }
        }

        return root
    }

    private fun addItem(root: View, textValue: String) {
        added = true

        val newItem = TodoItem(0, textValue)

        val myRef = database.getReference("todos/todo" + (keys.size + 1))
        myRef.setValue(newItem)

        exampleList.add(newItem)
        adapter.notifyItemInserted(exampleList.size - 1)

        //Toast.makeText(activity, "U mama",Toast.LENGTH_SHORT).show()
    }

    private fun generateDummyList(size: Int): ArrayList<TodoItem> {
        val list = ArrayList<TodoItem>()
        for (i in 0 until size) {
            val item = TodoItem(0, "Item $i")
            list += item
        }
        return list
    }
}
