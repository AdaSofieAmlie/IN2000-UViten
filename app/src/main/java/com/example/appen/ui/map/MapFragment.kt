package com.example.appen.ui.map

import com.example.appen.base.Pos
import com.example.appen.base.Uv
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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import com.example.appen.MainActivity
import com.example.appen.R
import com.example.appen.databinding.FragmentMapBinding
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.Mapbox
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
import com.example.appen.base.ViewModelMet
import com.example.appen.databinding.MapPopupViewBinding
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
    //Binding
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    //kommuniserer med ViewModelMet for UV
    private val viewModelMet: ViewModelMet by viewModels()
    //To UV-pointer
    private lateinit var main:LifecycleOwner
    private var uvTime: Float = 0.0F

    //Mapbox
        //User location
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapView: MapView
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    //Søkefunksjoner og undermeny
    private lateinit var searchBottomSheetView: SearchBottomSheetView
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView
    private lateinit var searchCategoriesView: SearchCategoriesBottomSheetView
    private lateinit var feedbackBottomSheetView: SearchFeedbackBottomSheetView
    //undermeny og mapbox klasser
    private lateinit var cardsMediator: SearchViewBottomSheetsMediator
    //com.example.appen.base.Uv markør på kartet
    private lateinit var viewAnnotationManager: ViewAnnotationManager
    private var markerCoordinates = mutableListOf<Point>()


    //Listeners for endringer i posisjon
    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }
    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }
    //Noe tomt her ettersom det ikke trengs / brukes
    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }
        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }
        override fun onMoveEnd(detector: MoveGestureDetector) {
        }
    }


    //Første åpning og bygging av kartet
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Mapbox.getInstance(this.requireContext(), "pk.eyJ1IjoiaW4yMDAwdGVhbTEiLCJhIjoiY2wwdHczdTMyMHB1NTNjbm1hYm93cWM3byJ9.KO3KIArfPC0qscDIi3ik7Q")

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root


        viewAnnotationManager = binding.mapView.viewAnnotationManager

        //pekere til mapview
        mapView = binding.mapView
        //Oppsett av kartview
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

        //Legger på listener for maptrykk
        mapboxMap = binding.mapView.getMapboxMap().apply {
            addOnMapClickListener(this@MapFragment)
        }
        //Gjør klart kartstilen med pins/image/symboler
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
        //Bygging vil ikke skje før permissions er gitt...
        mapView
        //Hjelpeklasse for permissions. Blir spurt om permission allerede på hjemskjerm
        //Se egen fil i samme mappe for hjelpeklassen
        locationPermissionHelper = LocationPermissionHelper(WeakReference(activity))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
        return root
    }

    //Kjøres etter onCreateView / gjennoppbyggning og savedinstance før Viewets savedstate
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        main = (activity as MainActivity?)!!

        //initsialiserer elementer for undermeny/søkefunksjon
        //Søkefunksjon
        searchBottomSheetView = binding.searchView
        searchBottomSheetView.initializeSearch(savedInstanceState, SearchBottomSheetView.Configuration())
        //Søke på et bestemt sted
        searchPlaceView = binding.searchPlaceView.apply {
            initialize(CommonSearchViewConfiguration(DistanceUnitType.METRIC))
            isNavigateButtonVisible = false
            isShareButtonVisible = false
            isFavoriteButtonVisible = false
        }
        //Kategorisøk "trykke på kategoriknapp"
        searchCategoriesView = binding.searchCategoriesView
        searchCategoriesView.initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
        //feedback på søk
        feedbackBottomSheetView = binding.searchFeedbackView
        feedbackBottomSheetView.initialize(savedInstanceState)
        //Undermeny og elementene
        cardsMediator = SearchViewBottomSheetsMediator(
            searchBottomSheetView,
            searchPlaceView,
            searchCategoriesView,
            feedbackBottomSheetView,
        )
        //Dersom noen variabler finnes i saveisntance. (tidligere søk osv)
        //...settes de inn her
        savedInstanceState?.let {
            cardsMediator.onRestoreInstanceState(it)
        }

        //Listener for aktivitet/trykk på undermenyen
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
        //Listener for kategori valg/søk/klikk
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
    //Selve oppbygging av kart / build() / Starter Listeners
    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(15.0)
                .build()
        )
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            Log.d("Test Map onMapReady()","Før locationComp og GestureListener")
            initLocationComponent()
            setupGesturesListener()
            Log.d("Test Map OnMapReady()","LocationCompListener og CameraGestureListener kjører")
        }
    }

    //savedInstance for lagring av tidligere søk
    override fun onSaveInstanceState(outState: Bundle) {
        cardsMediator.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

   //Ser etter darkmode og følger systemet
    private fun getMapStyleUri(): String {
       return when (val darkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> Style.DARK
            Configuration.UI_MODE_NIGHT_NO,
            Configuration.UI_MODE_NIGHT_UNDEFINED -> Style.MAPBOX_STREETS
            else -> error("Unknown night mode: $darkMode")
        }
    }

    //Funksjoner som viser pins/markører
    //Viser pin på koordinater/listede punkter
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

    //setter opp onMoveListener på mapviewet
    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    //Map posisjon / viser posisjon / følger nåværende posisjon
    private fun initLocationComponent() {
        mapView.location
        val locationComponentPlugin = mapView.location
        Log.d("Test Map initLocationComponent() Lokasjon","Hentet location med LocComp")
        locationComponentPlugin.updateSettings {
            this.enabled = true
            //puck som viser "bruker" på kartet
            LocationPuck2D(
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
            )
        }
        //Logs for kartoppbygning
        Log.d("Test Map initLocationComponent()","Location Component listeners:")
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        Log.d("Test Map initLocationComponent()","Location Component Ferdig")
        Log.d("Test Map initLocationComponent()","...\nKart location ferdig \n...")
    }

    //Listener for CameraTrack som skrur av tracking Listeners
    private fun onCameraTrackingDismissed() {
        Toast.makeText(activity, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    //PermissionRequest
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private companion object {

        const val SEARCH_PIN_SOURCE_ID = "search.pin.source.id"
        const val SEARCH_PIN_IMAGE_ID = "search.pin.image.id"
        const val SEARCH_PIN_LAYER_ID = "search.pin.layer.id"

        val markersPaddings: EdgeInsets = dpToPx(64).toDouble()
            .let { mapPadding ->
                EdgeInsets(mapPadding, mapPadding, mapPadding, mapPadding)
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
    }
    //Når bruker trykker på kart kalles addViewAnnotation
    override fun onMapClick(point: Point): Boolean {
        addViewAnnotation(point)
        return true
    }
    private lateinit var tvText:TextView
    //Legger til markør og viser UV på et gitt punkt
    private fun addViewAnnotation(point: Point) {
        val viewAnnotation = viewAnnotationManager.addViewAnnotation(
            resId = R.layout.map_popup_view,
            options = viewAnnotationOptions {
                geometry(point)
                allowOverlap(true)
            }
        )
        MapPopupViewBinding.bind(viewAnnotation).apply {

            val position = Pos(point.altitude().toInt(),point.latitude().toFloat(), point.longitude().toFloat())
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

    //Oppdatering av UV/UV textview
    private fun updateUi (innUv : Uv){
        val simpleDateFormat = SimpleDateFormat("HH")
        val currentDateAndTime: String = simpleDateFormat.format(Date())

        for (i in innUv.properties.timeseries){
            val time = i.time.split("T")
            val clock = time[1].split(":")
            val hour = clock[0]
            if (hour.toInt() == currentDateAndTime.toInt() ){
                Log.d("Test Map updateUi() Uv", i.toString())
                uvTime = i.data.instant.details.ultraviolet_index_clear_sky.toFloat()
                tvText.text = uvTime.toString()
                break
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
        Log.d("Memory","Low memory")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mapView.onDestroy()
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
        Log.d("Test Map onDestroyView()","... Destroy kart")

    }
}