/*
 * Copyright (C) 2019-2022 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */
package com.example.openDataCoursework

import android.location.LocationManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.openDataCoursework.PermissionsRequestor.ResultListener
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.LocationListener
import com.here.sdk.core.engine.SDKBuildInformation
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView


//const val EXTRA_MESSAGE = "com.example.openDataCoursework.MESSAGE"
//const val Origin = "com.example.openDataCoursework.Origin"
//const val Destination = "com.example.openDataCoursework.Destination"

class search_and_routing : AppCompatActivity() {

    private lateinit var routeExample: Routing
    private lateinit var permissionsRequestor: PermissionsRequestor
    private lateinit var mapView: MapView
    // private lateinit var searchExample: SearchExample
    var originList: MutableList<GeoCoordinates> = ArrayList()
    var destinationList: MutableList<GeoCoordinates> = ArrayList()

    private lateinit var searchExampleJava: SearchExampleJava

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide();// Hide ActionBar
        setContentView(R.layout.activity_main)
        Log.d("", "HERE SDK version: " + SDKBuildInformation.sdkVersion().versionName)
        // Get a MapView instance from layout.
        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        handleAndroidPermissions()
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
        var searchContextOrigin: String? = null
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
                searchExampleJava.onGeocodeButtonClicked(searchContextOrigin,"start")
                searchExampleJava.onGeocodeButtonClicked(searchContextDestination,"end")


                //System.out.println(destinationList.get(0))
//                routeExample.setStart(originList.get(0))
//                val desinationList= searchExample.getDestinationList()
//                routeExample.setEnd(desinationList.get(0))
//                routeExample.addExampleRoute()

            }
        }
        //添加imeOptions的监听
        //add the listener of imeOptions
        destination.setOnEditorActionListener(OnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchContextOrigin?.let { search(it) }
            }
            false
        })
        origin.setOnEditorActionListener(OnEditorActionListener { textView, actionId, keyEvent ->
            obtain()
            false
        })






    }
    private fun handleAndroidPermissions() {
        permissionsRequestor = PermissionsRequestor(this)
        permissionsRequestor.request(object : ResultListener {
            override fun permissionsGranted() {
                loadMapScene()
            }

            override fun permissionsDenied() {
                Log.e(TAG, "Permissions denied by user.")
            }
        })

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsRequestor.onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun loadMapScene() {
        mapView.mapScene.loadScene(
            MapScheme.NORMAL_DAY
        ) { mapError ->
            if (mapError == null) {
                routeExample = Routing(this, mapView)
                //searchExample = SearchExample(this@MainActivity, mapView!!)
                searchExampleJava = SearchExampleJava(this, mapView!!)
                // routeExample.setEnd(coord)
                // routeExample.setStart(coord)
            } else {
                Log.d(TAG, "Loading map failed: mapErrorCode: " + mapError.name)
            }
        }
    }

    fun changeStartPoint(coord: GeoCoordinates?) {
        routeExample.setStart(coord);
    }

    fun changeEnd(coord: GeoCoordinates?) {
        routeExample.setEnd(coord);
    }

    fun addExampleRoute(view: View?) {
        originList= searchExampleJava.getOriginList()
        destinationList=searchExampleJava.getDestinationList();
        for (item: GeoCoordinates in destinationList) {
            System.out.println(item.latitude)
            System.out.println(item.longitude)
            System.out.println("here")
        }
        routeExample.setStart(originList.get(0))
        routeExample.setEnd(destinationList.get(0))

        routeExample.addExampleRoute()
    }
    fun clear(view: View) {
        //FAB
        searchExampleJava.clearMap()
        routeExample.clearMap()
    }



//    fun originSearchAchieve(origin: String?) {
//        searchExample!!.originClicked(origin)
//    }
//
//    fun destinationSearchAchieve(destination: String?) {
//        searchExample!!.destinationClicked(destination)
//    }

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

    companion object {
        private val TAG = search_and_routing::class.java.simpleName
    }

    /** Called when the user taps the Send button */

//    fun Marked(view: View) {
//        // Do something in response to button
//        val intent = Intent(this, Marked::class.java)
//        startActivity(intent)
//    }
//    fun Settings(view: View) {
//        // Do something in response to button
//        val intent = Intent(this, Settings::class.java)
//        startActivity(intent)
//    }
}
