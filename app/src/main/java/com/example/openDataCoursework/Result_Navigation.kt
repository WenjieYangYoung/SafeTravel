package com.example.openDataCoursework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class ResultNavigation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_navigation)

        // Get the Intent that started this activity and extract the string
        val origin = intent.getStringExtra(Origin)
        val destination = intent.getStringExtra(Destination)

        // Capture the layout's TextView and set the string as its text
        val originResult = this.findViewById<TextView>(R.id.Origin).apply {
            text = origin }
        val destinationResult = this.findViewById<TextView>(R.id.Destination).apply {
            text = destination }
    }
}