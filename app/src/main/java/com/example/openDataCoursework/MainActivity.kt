package com.example.openDataCoursework

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapview.MapScene
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView


private lateinit var mapView: MapView;

const val EXTRA_MESSAGE = "com.example.openDataCoursework.MESSAGE"
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        loadMapScene();
        // These functions below need be set in the onCreat() function, important !!!
        // Otherwise, they do not work
        val editText = this.findViewById<EditText>(R.id.searchArea)
        //在该Editview获得焦点的时候将“回车”键改为“搜索”
        // Change the "Enter" key to "Search" when the Editview gets focus.
        editText.imeOptions = EditorInfo.IME_ACTION_SEARCH
        editText.inputType = EditorInfo.TYPE_CLASS_TEXT
        //设置单行输入，不然回车【搜索】会换行
        // Set single line input, otherwise, the “Enter” key will realize the line feed function
        editText.isSingleLine = true
        // Define search function
        fun search() {
            val searchContext: String = editText.text.toString()
            if (TextUtils.isEmpty(searchContext)) {
                Toast.makeText(this, "The input box is empty, please enter", Toast.LENGTH_SHORT).show()
            } else {
//             Here we should input the API that we use to use the map to search the data
//             Here is an example you can check
                val intent = Intent(this, ResultOfSearchArea::class.java).apply {
                    putExtra(EXTRA_MESSAGE, searchContext) }
                startActivity(intent)
            }
        }
        //添加imeOptions的监听
        //add the listener of imeOptions
        editText.setOnEditorActionListener(OnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search()
            }
            false
        })
    }

    /** Called when the user taps the Send button */
    fun Navigation(view: View) {
        // Do something in response to button
        val intent = Intent(this, NavigationFunction::class.java)
        startActivity(intent)
    }
    fun Marked(view: View) {
        // Do something in response to button
        val intent = Intent(this, Marked::class.java)
        startActivity(intent)
    }
    fun Settings(view: View) {
        // Do something in response to button
        val intent = Intent(this, Settings::class.java)
        startActivity(intent)
    }
    private fun loadMapScene() {
        // Load a scene from the HERE SDK to render the map with a map scheme.
        mapView.mapScene.loadScene(
            MapScheme.NORMAL_DAY,
            MapScene.LoadSceneCallback { mapError ->
                if (mapError == null) {
                    val distanceInMeters = (1000 * 10).toDouble()
                    mapView.camera.lookAt(
                        GeoCoordinates(50.90395, -1.40428), distanceInMeters
                    )
                } else {
                    Log.d(TAG, "Loading map failed: mapError: " + mapError.name)
                }
            })
    }
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}