package com.example.openDataCoursework

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity

//const val Origin = "com.example.openDataCoursework.Origin"
//const val Destination = "com.example.openDataCoursework.Destination"

class NavigationFunction : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_function)
        // These functions below need be set in the onCreat() function, important !!!
        // Otherwise, they do not work
        val origin = this.findViewById<EditText>(R.id.originSearch)
        val destination = this.findViewById<EditText>(R.id.destinationSearch)
        //在该Editview获得焦点的时候将“回车”键改为“搜索”
        // Change the "Enter" key to "Search" when the Editview gets focus.
        destination.imeOptions = EditorInfo.IME_ACTION_SEARCH
        destination.inputType = EditorInfo.TYPE_CLASS_TEXT
        origin.inputType = EditorInfo.TYPE_CLASS_TEXT
        //设置单行输入，不然回车【搜索】会换行
        // Set single line input, otherwise, the “Enter” key will realize the line feed function
        destination.isSingleLine = true
        var searchContextOrigin: String = ""
        // Define the Obtain function
        fun obtain() {
            searchContextOrigin = origin.text.toString()
            if (TextUtils.isEmpty(searchContextOrigin)) {
                Toast.makeText(this, "Please enter the Origin", Toast.LENGTH_SHORT).show()
            }
        }

        // Define the Search function
        fun search(searchContextOrigin: String) {
            val searchContextDestination: String = destination.text.toString()
            if (TextUtils.isEmpty(searchContextDestination) || TextUtils.isEmpty(searchContextOrigin)) {
                Toast.makeText(this, "Please enter the Origin and Destination", Toast.LENGTH_SHORT).show()
            } else {
//             Here we should input the API that we use to use the map to search the data
//             Here is an example you can check
                val intent = Intent(this, ResultNavigation::class.java).apply {
                    putExtra(Origin, searchContextOrigin)
                    putExtra(Destination, searchContextDestination)
                }
                startActivity(intent)

            }
        }
        //添加imeOptions的监听
        //add the listener of imeOptions
        destination.setOnEditorActionListener(OnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(searchContextOrigin)
            }
            false
        })
        origin.setOnEditorActionListener(OnEditorActionListener { textView, actionId, keyEvent ->
                obtain()
            false
        })
    }
}