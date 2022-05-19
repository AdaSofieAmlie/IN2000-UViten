package com.example.appen.ui.Map

//
import Pos
import Uv
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import com.mapbox.geojson.Point
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.appen.MainActivity
import com.example.appen.R
import com.example.appen.databinding.FragmentMapBinding
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point.fromLngLat
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.image.image
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.category.SearchCategoriesBottomSheetView
import com.mapbox.search.ui.view.feedback.SearchFeedbackBottomSheetView
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import java.lang.ref.WeakReference
import com.example.appen.SearchViewBottomSheetsMediator
import com.example.appen.ViewModelMet
import com.example.appen.databinding.ItemCalloutViewBinding
import com.google.android.gms.maps.MapsInitializer.initialize
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.search.ui.view.place.SearchPlace
import java.text.SimpleDateFormat
import java.util.*


class MapFragment : Fragment(), OnMapClickListener{ //OnMapReadyCallback

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val viewModelMet: ViewModelMet by viewModels()
    var uvTime: Float = 0.0F

    //To UV-pointer
    lateinit var main:LifecycleOwner
    //

    lateinit var mainView: View
    var mapOpened: Boolean = false
    //Mapbox
        //User location
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapView: MapView
    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private lateinit var searchBottomSheetView: SearchBottomSheetView
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView
    private lateinit var searchCategoriesView: SearchCategoriesBottomSheetView
    private lateinit var feedbackBottomSheetView: SearchFeedbackBottomSheetView

    private lateinit var cardsMediator: SearchViewBottomSheetsMediator

    private lateinit var viewAnnotationManager: ViewAnnotationManager

    private var markerCoordinates = mutableListOf<Point>()

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }
        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }
    //MapBox -Search-test
    private lateinit var reverseGeocoding: ReverseGeocodingSearchEngine
    private lateinit var searchRequestTask: SearchRequestTask

    private val searchCallback = object : SearchCallback {

        override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
            if (results.isEmpty()) {
                Log.i("SearchApiExample", "No reverse geocoding results")
            } else {
                Log.i("SearchApiExample", "Reverse geocoding results: $results")
            }
        }

        override fun onError(e: Exception) {
            Log.i("SearchApiExample", "Reverse geocoding error", e)
        }
    }
    //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Mapbox.getInstance(this.requireContext(), "pk.eyJ1IjoiaW4yMDAwdGVhbTEiLCJhIjoiY2wwdHczdTMyMHB1NTNjbm1hYm93cWM3byJ9.KO3KIArfPC0qscDIi3ik7Q")

        val dashboardViewModel =
            ViewModelProvider(this).get(MapViewModel::class.java)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //OnMapClick
        //add a blue marker view to a drawable then
        viewAnnotationManager = binding.mapView.viewAnnotationManager
        //OnMapClick

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        mapView = binding.mapView
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)


        mapboxMap = binding.mapView.getMapboxMap().apply {
            addOnMapClickListener(this@MapFragment)
        }

        mapboxMap.loadStyle(
            style(styleUri = getMapStyleUri()) {
                +geoJsonSource(SEARCH_PIN_SOURCE_ID) {
                    featureCollection(
                        FeatureCollection.fromFeatures(
                            markerCoordinates.map {
                                Feature.fromGeometry(it)
                            }
                        )
                    )
                }
                +image(SEARCH_PIN_IMAGE_ID) {
                    bitmap(createSearchPinDrawable().toBitmap(config = Bitmap.Config.ARGB_8888))
                }
                +symbolLayer(SEARCH_PIN_LAYER_ID, SEARCH_PIN_SOURCE_ID) {
                    iconImage(SEARCH_PIN_IMAGE_ID)
                    iconAllowOverlap(true)
                }
            }
        )


        mapView
        locationPermissionHelper = LocationPermissionHelper(WeakReference(activity))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        main = (activity as MainActivity?)!!

        searchBottomSheetView = binding.searchView
        searchBottomSheetView.initializeSearch(savedInstanceState, SearchBottomSheetView.Configuration())

        searchPlaceView = binding.searchPlaceView.apply {
            initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))

            isNavigateButtonVisible = false
            isShareButtonVisible = false
            isFavoriteButtonVisible = false
        }

        searchCategoriesView = binding.searchCategoriesView
        searchCategoriesView.initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))

        feedbackBottomSheetView = binding.searchFeedbackView
        feedbackBottomSheetView.initialize(savedInstanceState)

        cardsMediator = SearchViewBottomSheetsMediator(
            searchBottomSheetView,
            searchPlaceView,
            searchCategoriesView,
            feedbackBottomSheetView,
        )

        savedInstanceState?.let {
            cardsMediator.onRestoreInstanceState(it)
        }

        cardsMediator.addSearchBottomSheetsEventsListener(object :
            SearchViewBottomSheetsMediator.SearchBottomSheetsEventsListener {
            override fun onOpenPlaceBottomSheet(place: SearchPlace) {
                showMarker(place.coordinate)
            }

            override fun onOpenCategoriesBottomSheet(category: Category) {}

            override fun onBackToMainBottomSheet() {
                clearMarkers()
            }
        })

        searchCategoriesView.addCategoryLoadingStateListener(object :
            SearchCategoriesBottomSheetView.CategoryLoadingStateListener {
            override fun onLoadingStart(category: Category) {}

            override fun onCategoryResultsLoaded(
                category: Category,
                searchResults: List<SearchResult>,
                responseInfo: ResponseInfo,
            ) {
                showMarkers(searchResults.mapNotNull { it.coordinate })
            }

            override fun onLoadingError(category: Category, e: Exception) {}
        })

    }
    //Endringer herfra - Med OnMapClick


    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            Log.d("TEST","Før locationComp og GestureListener")
            initLocationComponent()
            setupGesturesListener()
            Log.d("TEST","LocationCompListener og CameraGestureListener kjører")
        }

    }
    /*
    override fun onBackPressed() {
        if (!cardsMediator.handleOnBackPressed()) {
            super.onBackPressed()
        }
    }

     */



    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        cardsMediator.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    /*
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

     */

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    private fun getMapStyleUri(): String {
        val darkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return when (darkMode) {
            Configuration.UI_MODE_NIGHT_YES -> Style.DARK
            Configuration.UI_MODE_NIGHT_NO,
            Configuration.UI_MODE_NIGHT_UNDEFINED -> Style.MAPBOX_STREETS
            else -> error("Unknown night mode: $darkMode")
        }
    }

    private fun showMarkers(coordinates: List<Point>) {
        if (coordinates.isEmpty()) {
            clearMarkers()
            return
        } else if (coordinates.size == 1) {
            showMarker(coordinates.first())
            return
        }

        val cameraOptions = mapboxMap.cameraForCoordinates(
            coordinates, markersPaddings, bearing = null, pitch = null
        )

        if (cameraOptions.center == null) {
            clearMarkers()
            return
        }

        showMarkers(cameraOptions, coordinates)
    }

    private fun showMarker(coordinate: Point) {
        val cameraOptions = CameraOptions.Builder()
            .center(coordinate)
            .zoom(10.0)
            .build()

        showMarkers(cameraOptions, listOf(coordinate))
    }

    private fun showMarkers(cameraOptions: CameraOptions, coordinates: List<Point>) {
        markerCoordinates.clear()
        markerCoordinates.addAll(coordinates)
        updateMarkersOnMap()

        mapboxMap.setCamera(cameraOptions)
    }

    private fun clearMarkers() {
        markerCoordinates.clear()
        updateMarkersOnMap()
    }

    private fun updateMarkersOnMap() {
        mapboxMap.getStyle()?.getSourceAs<GeoJsonSource>(SEARCH_PIN_SOURCE_ID)?.featureCollection(
            FeatureCollection.fromFeatures(
                markerCoordinates.map {
                    Feature.fromGeometry(it)
                }
            )
        )
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        mapView.location
        val locationComponentPlugin = mapView.location
        Log.d("TEST","TEST: Hentet location")
        locationComponentPlugin.updateSettings {
            this.enabled = true
            LocationPuck2D(
                //getApplicationContext()
                //requireContext()
                topImage = AppCompatResources.getDrawable(
                    requireContext(),
                    com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_icon
                ),
                bearingImage = AppCompatResources.getDrawable(
                    requireContext(),
                    com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_bearing_icon,
                ),
                shadowImage = AppCompatResources.getDrawable(
                    requireContext(),
                    com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_stroke_icon,
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )//.also { this.locationPuck = it }
        }
        Log.d("TEST","Location Component listeners")
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        Log.d("TEST","Location Component Ferdig")
        Log.d("TEST","...\nKart location ferdig \n...")
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(activity, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    /*
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        Log.d(null,"...\nStarter Kart\n...")
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

     */

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
        Log.d("TEST: ","Low memory")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mapView?.onDestroy()
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
        Log.d("TEST: ","... Destroy kart")

    }

    private companion object {

        const val SEARCH_PIN_SOURCE_ID = "search.pin.source.id"
        const val SEARCH_PIN_IMAGE_ID = "search.pin.image.id"
        const val SEARCH_PIN_LAYER_ID = "search.pin.layer.id"

        val markersPaddings: EdgeInsets = dpToPx(64).toDouble()
            .let { mapPadding ->
                EdgeInsets(mapPadding, mapPadding, mapPadding, mapPadding)
            }

        const val PERMISSIONS_REQUEST_LOCATION = 0

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

        fun createSearchPinDrawable(): ShapeDrawable {
            val size = dpToPx(24)
            val drawable = ShapeDrawable(OvalShape())
            drawable.intrinsicWidth = size
            drawable.intrinsicHeight = size
            DrawableCompat.setTint(drawable, Color.RED)
            return drawable
        }

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
        //MapClick
        const val SELECTED_ADD_COEF_PX = 25
        const val STARTUP_TEXT = "Click on a map to add a view annotation."
    }

    override fun onMapClick(point: Point): Boolean {
        addViewAnnotation(point)
        return true
    }
    lateinit var tvText:TextView
    private fun addViewAnnotation(point: Point) {
        val viewAnnotation = viewAnnotationManager.addViewAnnotation(
            resId = R.layout.item_callout_view,
            options = viewAnnotationOptions {
                geometry(point)
                allowOverlap(true)
            }
        )
        ItemCalloutViewBinding.bind(viewAnnotation).apply {

            var position = Pos(point.altitude().toInt(),point.latitude().toFloat(), point.longitude().toFloat())
            viewModelMet.updatePositionMet(position)
            viewModelMet.getUvPaaSted().observe(main) {
                updateUi(it)
            }


            tvText = textNativeView

            closeNativeView.setOnClickListener {
                viewAnnotationManager.removeViewAnnotation(viewAnnotation)
            }

        }
    }
    fun updateUi (innUv : Uv){
        val simpleDateFormat = SimpleDateFormat("HH")
        val currentDateAndTime: String = simpleDateFormat.format(Date())

        //val tv = simple.findViewById<TextView>(R.id.tvSimple)
        val tv = R.layout.item_callout_view

        for (i in innUv.properties.timeseries){
            val time = i.time.split("T")
            val clock = time[1].split(":")
            val hour = clock[0]
            if (hour.toInt() == currentDateAndTime.toInt() ){
                Log.d("TEST: Uv for nå", i.toString())
                uvTime = i.data.instant.details.ultraviolet_index_clear_sky.toFloat()
                tvText.text = uvTime.toString()
                break
            }
        }
    }
}