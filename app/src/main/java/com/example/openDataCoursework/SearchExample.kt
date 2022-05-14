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

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.here.sdk.core.*
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.gestures.GestureState
import com.here.sdk.gestures.LongPressListener
import com.here.sdk.gestures.TapListener
import com.here.sdk.mapview.*
import com.here.sdk.mapview.MapViewBase.PickMapItemsCallback
import com.here.sdk.search.*

class SearchExample(private val context: Context, private val mapView: MapView) {

    private lateinit var routeExample: Routing
    private var camera: MapCamera
    private val mapMarkerList: MutableList<MapMarker> = ArrayList()
    val originList: MutableList<GeoCoordinates> = ArrayList()
    private val destinationList: MutableList<GeoCoordinates> = ArrayList()
    private var searchEngine: SearchEngine? = null

    fun onSearchButtonClicked() {
        // Search for "Pizza" and show the results on the map.
        searchExample()

        // Search for auto suggestions and log the results to the console.
        autoSuggestExample()
    }

    fun onGeocodeButtonClicked() {
        // Search for the location that belongs to an address and show it on the map.
        geocodeAnAddress()
    }

    fun originClicked(origin:String?) {
        // Search for the location that belongs to an address and show it on the map.
        if (origin != null) {
            originSearch(origin)
        }
    }

    fun destinationClicked(destination: String?) {
        // Search for the location that belongs to an address and show it on the map.
        if (destination != null) {
            destinationSearch(destination)
        }
    }

    private fun searchExample() {
        val searchTerm = "Pizza"
        Toast.makeText(context, "Searching in viewport: $searchTerm", Toast.LENGTH_LONG).show()
        searchInViewport(searchTerm)
    }

    private fun geocodeAnAddress() {
        // Set map to expected location.
//        val geoCoordinates = GeoCoordinates(52.53086, 13.38469)
        val geoCoordinates = GeoCoordinates(50.93638, -1.396233)
//        camera.lookAt(geoCoordinates, (1000 * 7).toDouble())
        val queryString = "Invalidenstraße 116, Berlin"
        Toast.makeText(
            context,
            "Finding locations for: " + queryString
                    + ". Tap marker to see the coordinates. Check the logs for the address.",
            Toast.LENGTH_LONG
        ).show()
        geocodeAddressAtLocation(queryString, geoCoordinates)
    }

    private fun originSearch(origin:String?) {  // Search origin
        // Set map to expected location.
//        val geoCoordinates = GeoCoordinates(52.53086, 13.38469)
        val geoCoordinates = GeoCoordinates(50.93638, -1.396233)
//        camera.lookAt(geoCoordinates, (1000 * 7).toDouble())
        val queryString = origin
        Toast.makeText(
            context,
            "Finding locations for: " + queryString
                    + ". Tap marker to see the coordinates. Check the logs for the address.",
            Toast.LENGTH_LONG
        ).show()
        if (queryString != null) {
            geocodeOriginAddress(queryString, geoCoordinates)
        }
    }

    @JvmName("getOriginList1")
    public fun getOriginList(): MutableList<GeoCoordinates> {
        System.out.println("here origin")
        System.out.println(originList)
        return this.originList;
    }

    public fun getDestinationList(): MutableList<GeoCoordinates> {
        return destinationList;
    }

    private fun destinationSearch(destination:String?) { // Search destination
        // Set map to expected location.
//        val geoCoordinates = GeoCoordinates(52.53086, 13.38469)
        val geoCoordinates = GeoCoordinates(50.93638, -1.396233)
//        camera.lookAt(geoCoordinates, (1000 * 7).toDouble())
        val queryString = destination
        Toast.makeText(
            context,
            "Finding locations for: " + queryString
                    + ". Tap marker to see the coordinates. Check the logs for the address.",
            Toast.LENGTH_LONG
        ).show()
        if (queryString != null) {
            geocodeDestinationAddress(queryString, geoCoordinates)
        }
    }

    private fun setTapGestureHandler() {
        mapView.gestures.tapListener = TapListener { touchPoint: Point2D ->
            pickMapMarker(
                touchPoint
            )
        }
    }

    private fun setLongPressGestureHandler() {
        mapView.gestures.longPressListener =
            LongPressListener setLongPressListener@{ gestureState: GestureState, touchPoint: Point2D? ->
                if (gestureState == GestureState.BEGIN) {
                    val geoCoordinates =
                        mapView.viewToGeoCoordinates(touchPoint!!) ?: return@setLongPressListener
                    addPoiMapMarker(geoCoordinates)
                    getAddressForCoordinates(geoCoordinates)
                }
            }
    }

    private fun getAddressForCoordinates(geoCoordinates: GeoCoordinates) {
        val reverseGeocodingOptions = SearchOptions()
        reverseGeocodingOptions.languageCode = LanguageCode.EN_GB
        reverseGeocodingOptions.maxItems = 1
        searchEngine!!.search(geoCoordinates, reverseGeocodingOptions, addressSearchCallback)
    }

    private val addressSearchCallback =
        SearchCallback { searchError, list ->
            if (searchError != null) {
                showDialog("Reverse geocoding", "Error: $searchError")
                return@SearchCallback
            }

            // If error is null, list is guaranteed to be not empty.
            showDialog("Reverse geocoded address:", list!![0].address.addressText)
        }

    private fun pickMapMarker(point2D: Point2D) {
        val radiusInPixel = 2f
        mapView.pickMapItems(point2D, radiusInPixel.toDouble(),
            PickMapItemsCallback { pickMapItemsResult ->
                if (pickMapItemsResult == null) {
                    return@PickMapItemsCallback
                }
                val mapMarkerList = pickMapItemsResult.markers
                if (mapMarkerList.size == 0) {
                    return@PickMapItemsCallback
                }
                val topmostMapMarker = mapMarkerList[0]
                val metadata = topmostMapMarker.metadata
                if (metadata != null) {
                    val customMetadataValue = metadata.getCustomValue("key_search_result")
                    if (customMetadataValue != null) {
                        val searchResultMetadata = customMetadataValue as SearchResultMetadata
                        val title = searchResultMetadata.searchResult.title
                        val vicinity = searchResultMetadata.searchResult.address.addressText
                        showDialog("Picked Search Result", "$title. Vicinity: $vicinity")
                        return@PickMapItemsCallback
                    }
                }
                showDialog(
                    "Picked Map Marker",
                    "Geographic coordinates: " +
                            topmostMapMarker.coordinates.latitude + ", " +
                            topmostMapMarker.coordinates.longitude
                )
            })
    }

    private fun searchInViewport(queryString: String) {
        clearMap()
        val viewportGeoBox = mapViewGeoBox
        val query = TextQuery(queryString, viewportGeoBox)
        val searchOptions = SearchOptions()
        searchOptions.languageCode = LanguageCode.EN_US
        searchOptions.maxItems = 30
        searchEngine!!.search(query, searchOptions, querySearchCallback)
    }

    private val querySearchCallback =
        SearchCallback { searchError, list ->
            if (searchError != null) {
                showDialog("Search", "Error: $searchError")
                return@SearchCallback
            }
            // If error is null, list is guaranteed to be not empty.
            showDialog("Search", "Results: " + list!!.size)

            // Add new marker for each search result on map.
            for (searchResult in list) {
                val metadata = Metadata()
                metadata.setCustomValue("key_search_result", SearchResultMetadata(searchResult))
                // Note: getGeoCoordinates() may return null only for Suggestions.
                addPoiMapMarker(searchResult.geoCoordinates, metadata)
            }
        }

    private class SearchResultMetadata(val searchResult: Place) :
        CustomMetadataValue {
        override fun getTag(): String {
            return "SearchResult Metadata"
        }
    }

    private val autosuggestCallback =
        SuggestCallback { searchError, list ->
            if (searchError != null) {
                Log.d(LOG_TAG, "Autosuggest Error: " + searchError.name)
                return@SuggestCallback
            }

            // If error is null, list is guaranteed to be not empty.
            Log.d(LOG_TAG, "Autosuggest results: " + list!!.size)
            for (autosuggestResult in list) {
                var addressText = "Not a place."
                val place = autosuggestResult.place
                if (place != null) {
                    addressText = place.address.addressText
                }
                Log.d(
                    LOG_TAG, "Autosuggest result: " + autosuggestResult.title +
                            " addressText: " + addressText
                )
            }
        }

    private fun autoSuggestExample() {
        val centerGeoCoordinates = mapViewCenter
        val searchOptions = SearchOptions()
        searchOptions.languageCode = LanguageCode.EN_US
        searchOptions.maxItems = 5

        // Simulate a user typing a search term.
        searchEngine!!.suggest(
            TextQuery(
                "p",  // User typed "p".
                centerGeoCoordinates
            ),
            searchOptions,
            autosuggestCallback
        )
        searchEngine!!.suggest(
            TextQuery(
                "pi",  // User typed "pi".
                centerGeoCoordinates
            ),
            searchOptions,
            autosuggestCallback
        )
        searchEngine!!.suggest(
            TextQuery(
                "piz",  // User typed "piz".
                centerGeoCoordinates
            ),
            searchOptions,
            autosuggestCallback
        )
    }

    private fun geocodeAddressAtLocation(queryString: String, geoCoordinates: GeoCoordinates) {
        clearMap()
//        val query = AddressQuery(queryString, geoCoordinates)
        val query = TextQuery(queryString, geoCoordinates)
        val options = SearchOptions()
        options.languageCode = LanguageCode.EN_GB
        options.maxItems = 30
        searchEngine!!.search(query, options, geocodeAddressSearchCallback)
    }

    private fun geocodeOriginAddress(queryString: String, geoCoordinates: GeoCoordinates) {
        clearMap()
//        val query = AddressQuery(queryString, geoCoordinates)
        val query = TextQuery(queryString, geoCoordinates)
        val options = SearchOptions()
        options.languageCode = LanguageCode.EN_GB
        options.maxItems = 1
        searchEngine!!.search(query, options, originSearchCallback)
    }

    private fun geocodeDestinationAddress(queryString: String, geoCoordinates: GeoCoordinates) {
//        clearMap()
//        val query = AddressQuery(queryString, geoCoordinates)
        val query = TextQuery(queryString, geoCoordinates)
        val options = SearchOptions()
        options.languageCode = LanguageCode.EN_GB
        options.maxItems = 1
        searchEngine!!.search(query, options, destinationSearchCallback)
    }

    private val geocodeAddressSearchCallback =
        SearchCallback { searchError, list ->
            camera = mapView.camera
            if (searchError != null) {
                showDialog("Geocoding", "Error: $searchError")
                return@SearchCallback
            }
            for (geocodingResult in list!!) {
                // Note: getGeoCoordinates() may return null only for Suggestions.
                val geoCoordinates = geocodingResult.geoCoordinates
                val address = geocodingResult.address
                val locationDetails = (address.addressText
                        + ". GeoCoordinates: " + geoCoordinates!!.latitude
                        + ", " + geoCoordinates.longitude)
                Log.d(
                    LOG_TAG,
                    "GeocodingResult: $locationDetails"
                )
                camera.lookAt(geoCoordinates, (1000 * 7).toDouble())
                addPoiMapMarker(geoCoordinates)
            }
            showDialog("Geocoding result", "Size: " + list.size)
        }

    private val originSearchCallback =
        SearchCallback { searchError, list ->
            camera = mapView.camera
            if (searchError != null) {
                showDialog("Geocoding", "Error: $searchError")
                return@SearchCallback
            }
            for (originResult in list!!) {
                // Note: getGeoCoordinates() may return null only for Suggestions.
                val originCoordinates = originResult.geoCoordinates
                val originAddress = originResult.address
                val locationDetails = (originAddress.addressText
                        + ". originCoordinates: " + originCoordinates!!.latitude
                        + ", " + originCoordinates.longitude)
                Log.d(
                    LOG_TAG,
                    "GeocodingResult: $locationDetails"
                )
                originList.add(originCoordinates)
                addOriginMarker(originCoordinates)


            }
            Log.i("Origin result", "Size: " + list.size)
        }

    private val destinationSearchCallback =
        SearchCallback { searchError, list ->
            camera = mapView.camera
            if (searchError != null) {
                showDialog("Geocoding", "Error: $searchError")
                return@SearchCallback
            }
            for (destinationResult in list!!) {
                // Note: getGeoCoordinates() may return null only for Suggestions.
                val destinationCoordinates = destinationResult.geoCoordinates
                val destinationAddress = destinationResult.address
                val locationDetails = (destinationAddress.addressText
                        + ". GeoCoordinates: " + destinationCoordinates!!.latitude
                        + ", " + destinationCoordinates.longitude)
                Log.d(
                    LOG_TAG,
                    "GeocodingResult: $locationDetails"
                )
                camera.lookAt(destinationCoordinates, (1000 * 20).toDouble())
                addDestinationMarker(destinationCoordinates)
                destinationList.add(destinationCoordinates)
            }
            Log.i("Destination result", "Size: " + list.size)
        }

    private fun addOriginMarker(geoCoordinates: GeoCoordinates?) {
        val mapMarker = createOriginMarker(geoCoordinates)
        mapView.mapScene.addMapMarker(mapMarker)
        mapMarkerList.add(mapMarker)
    }

    private fun addDestinationMarker(geoCoordinates: GeoCoordinates?) {
        val mapMarker = createDestinationMarker(geoCoordinates)
        mapView.mapScene.addMapMarker(mapMarker)
        mapMarkerList.add(mapMarker)
    }

    private fun addPoiMapMarker(geoCoordinates: GeoCoordinates?) {
        val mapMarker = createPoiMapMarker(geoCoordinates)
        mapView.mapScene.addMapMarker(mapMarker)
        mapMarkerList.add(mapMarker)
    }

    private fun addPoiMapMarker(geoCoordinates: GeoCoordinates?, metadata: Metadata) {
        val mapMarker = createPoiMapMarker(geoCoordinates)
        mapMarker.metadata = metadata
        mapView.mapScene.addMapMarker(mapMarker)
        mapMarkerList.add(mapMarker)
    }

    private fun createPoiMapMarker(geoCoordinates: GeoCoordinates?): MapMarker {
        val mapImage = MapImageFactory.fromResource(context.resources, R.drawable.select)
        return MapMarker(geoCoordinates!!, mapImage, Anchor2D(0.5, 1.0))
    }

    private fun createOriginMarker(geoCoordinates: GeoCoordinates?): MapMarker {
        val mapImage = MapImageFactory.fromResource(context.resources, R.drawable.origin)
        return MapMarker(geoCoordinates!!, mapImage, Anchor2D(0.5, 1.0))
    }

    private fun createDestinationMarker(geoCoordinates: GeoCoordinates?): MapMarker {
        val mapImage = MapImageFactory.fromResource(context.resources, R.drawable.destination)
        return MapMarker(geoCoordinates!!, mapImage, Anchor2D(0.5, 1.0))
    }

    private val mapViewCenter: GeoCoordinates
        get() = mapView.camera.state.targetCoordinates

    // Note: This algorithm assumes an unrotated map view.
    private val mapViewGeoBox: GeoBox
        get() {
            val mapViewWidthInPixels = mapView.width
            val mapViewHeightInPixels = mapView.height
            val bottomLeftPoint2D = Point2D(
                0.0,
                mapViewHeightInPixels.toDouble()
            )
            val topRightPoint2D = Point2D(
                mapViewWidthInPixels.toDouble(), 0.0
            )
            val southWestCorner = mapView.viewToGeoCoordinates(bottomLeftPoint2D)
            val northEastCorner = mapView.viewToGeoCoordinates(topRightPoint2D)
            if (southWestCorner == null || northEastCorner == null) {
                throw RuntimeException("GeoBox creation failed, corners are null.")
            }

            // Note: This algorithm assumes an unrotated map view.
            return GeoBox(southWestCorner, northEastCorner)
        }

    fun clearMap() {
        for (mapMarker in mapMarkerList) {
            mapView.mapScene.removeMapMarker(mapMarker)
        }
        mapMarkerList.clear()
    }

    private fun showDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(
            context
        )
        builder.setTitle(title)
        builder.setMessage(message)
        builder.show()
    }


    companion object {
        private val LOG_TAG = SearchExample::class.java.name
    }

    init { // Initialisation
        camera = mapView.camera
        val distanceInMeters = (1000 * 20).toDouble()
        camera.lookAt(GeoCoordinates(50.918608, -1.404146), distanceInMeters)
        searchEngine = try { // Set search engine
            SearchEngine()
        } catch (e: InstantiationErrorException) {
            throw RuntimeException("Initialization of SearchEngine failed: " + e.error.name)
        }
        setTapGestureHandler()
        setLongPressGestureHandler()
        Toast.makeText(
            context,
            "Long press on map to get the address for that position using reverse geocoding.",
            Toast.LENGTH_LONG
        ).show()
    }
}