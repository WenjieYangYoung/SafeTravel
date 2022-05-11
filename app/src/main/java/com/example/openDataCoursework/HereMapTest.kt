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

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.here.sdk.core.engine.SDKBuildInformation
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView
import com.example.openDataCoursework.PermissionsRequestor.ResultListener

class HereMapTest : AppCompatActivity() {
    private var permissionsRequestor: PermissionsRequestor? = null
    private var mapView: MapView? = null
    private var searchExample: SearchExample? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("", "HERE SDK version: " + SDKBuildInformation.sdkVersion().versionName)

        // Get a MapView instance from layout.
        mapView = findViewById(R.id.map_view)
        mapView!!.onCreate(savedInstanceState)
        handleAndroidPermissions()
    }

    private fun handleAndroidPermissions() {
        permissionsRequestor = PermissionsRequestor(this)
        permissionsRequestor!!.request(object : ResultListener {
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
        permissionsRequestor!!.onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun loadMapScene() {
        // Load a scene from the HERE SDK to render the map with a map scheme.
        mapView!!.mapScene.loadScene(
            MapScheme.NORMAL_DAY
        ) { mapError ->
            if (mapError == null) {
                searchExample = SearchExample(this, mapView!!)
            } else {
                Log.d(TAG, "onLoadScene failed: $mapError")
            }
        }
    }

    fun searchExampleButtonClicked(view: View?) {
        searchExample!!.onSearchButtonClicked()
    }

    fun geocodeAnAddressButtonClicked(view: View?) {
        searchExample!!.onGeocodeButtonClicked()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}