package com.example.appen.ui.Map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.appen.databinding.FragmentMapBinding
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import java.lang.ref.WeakReference
import com.mapbox.maps.extension.style.layers.addLayerAbove
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.style.layers.generated.HeatmapLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.heatmapLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.MapProjection


class MapFragment : Fragment(){ //OnMapReadyCallback

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    //Mapbox
        //User location
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapView: MapView
    private lateinit var locationPermissionHelper: LocationPermissionHelper

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {



        val dashboardViewModel =
            ViewModelProvider(this).get(MapViewModel::class.java)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }


        mapView = binding.mapView
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)

        locationPermissionHelper = LocationPermissionHelper(WeakReference(activity))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
        mapboxMap = binding.mapView.getMapboxMap().apply {
            loadStyleUri(
                styleUri = Style.LIGHT
            ) { style -> addRuntimeLayers(style) }
            setMapProjection(MapProjection.Globe)
        }
        return root
    }

    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            Log.d(null,"Før locationComp og GestureListener")
            initLocationComponent()
            setupGesturesListener()
            Log.d(null,"LocationCompListener og CameraGestureListener kjører")
        }

    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        mapView.location
        val locationComponentPlugin = mapView.location
        Log.d(null,"Hentet location")
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
            ).also { this.locationPuck = it }
        }
        Log.d(null,"Location Component listeners")
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        Log.d(null,"Location Component Ferdig")
        Log.d(null,"...\nKart location ferdig \n...")
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

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        Log.d(null,"...\nStarter Kart\n...")
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
        Log.d(null,"Low memory")
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
        Log.d(null,"... Destroy kart")

    }
    private fun addRuntimeLayers(style: Style) {
        style.addSource(createEarthquakeSource())
        style.addLayerAbove(createHeatmapLayer(), "waterway-label")
        style.addLayerBelow(createCircleLayer(), HEATMAP_LAYER_ID)
    }

    private fun createEarthquakeSource(): GeoJsonSource {
        return geoJsonSource(EARTHQUAKE_SOURCE_ID) {
            url(EARTHQUAKE_SOURCE_URL)
        }
    }

    private fun createHeatmapLayer(): HeatmapLayer {
        return heatmapLayer(
            HEATMAP_LAYER_ID,
            EARTHQUAKE_SOURCE_ID
        ) {
            maxZoom(9.0)
            sourceLayer(HEATMAP_LAYER_SOURCE)
            // Begin color ramp at 0-stop with a 0-transparancy color
            // to create a blur-like effect.
            heatmapColor(
                interpolate {
                    linear()
                    heatmapDensity()
                    stop {
                        literal(0)
                        rgba(33.0, 102.0, 172.0, 0.0)
                    }
                    stop {
                        literal(0.2)
                        rgb(103.0, 169.0, 207.0)
                    }
                    stop {
                        literal(0.4)
                        rgb(209.0, 229.0, 240.0)
                    }
                    stop {
                        literal(0.6)
                        rgb(253.0, 219.0, 240.0)
                    }
                    stop {
                        literal(0.8)
                        rgb(239.0, 138.0, 98.0)
                    }
                    stop {
                        literal(1)
                        rgb(178.0, 24.0, 43.0)
                    }
                }
            )
            // Increase the heatmap weight based on frequency and property magnitude
            heatmapWeight(
                interpolate {
                    linear()
                    get { literal("mag") }
                    stop {
                        literal(0)
                        literal(0)
                    }
                    stop {
                        literal(6)
                        literal(1)
                    }
                }
            )
            // Increase the heatmap color weight weight by zoom level
            // heatmap-intensity is a multiplier on top of heatmap-weight
            heatmapIntensity(
                interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0)
                        literal(1)
                    }
                    stop {
                        literal(9)
                        literal(3)
                    }
                }
            )
            // Adjust the heatmap radius by zoom level
            heatmapRadius(
                interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0)
                        literal(2)
                    }
                    stop {
                        literal(9)
                        literal(20)
                    }
                }
            )
            // Transition from heatmap to circle layer by zoom level
            heatmapOpacity(
                interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(7)
                        literal(1)
                    }
                    stop {
                        literal(9)
                        literal(0)
                    }
                }
            )
        }
    }

    private fun createCircleLayer(): CircleLayer {
        return circleLayer(
            CIRCLE_LAYER_ID,
            EARTHQUAKE_SOURCE_ID
        ) {
            circleRadius(
                interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(7)
                        interpolate {
                            linear()
                            get { literal("mag") }
                            stop {
                                literal(1)
                                literal(1)
                            }
                            stop {
                                literal(6)
                                literal(4)
                            }
                        }
                    }
                    stop {
                        literal(16)
                        interpolate {
                            linear()
                            get { literal("mag") }
                            stop {
                                literal(1)
                                literal(5)
                            }
                            stop {
                                literal(6)
                                literal(50)
                            }
                        }
                    }
                }
            )
            circleColor(
                interpolate {
                    linear()
                    get { literal("mag") }
                    stop {
                        literal(1)
                        rgba(33.0, 102.0, 172.0, 0.0)
                    }
                    stop {
                        literal(2)
                        rgb(102.0, 169.0, 207.0)
                    }
                    stop {
                        literal(3)
                        rgb(209.0, 229.0, 240.0)
                    }
                    stop {
                        literal(4)
                        rgb(253.0, 219.0, 199.0)
                    }
                    stop {
                        literal(5)
                        rgb(239.0, 138.0, 98.0)
                    }
                    stop {
                        literal(6)
                        rgb(178.0, 24.0, 43.0)
                    }
                }
            )
            circleOpacity(
                interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(7)
                        literal(0)
                    }
                    stop {
                        literal(8)
                        literal(1)
                    }
                }
            )
            circleStrokeColor("white")
            circleStrokeWidth(0.1)
        }
    }

    companion object {
        private const val EARTHQUAKE_SOURCE_URL =
            "https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson"
        private const val EARTHQUAKE_SOURCE_ID = "earthquakes"
        private const val HEATMAP_LAYER_ID = "earthquakes-heat"
        private const val HEATMAP_LAYER_SOURCE = "earthquakes"
        private const val CIRCLE_LAYER_ID = "earthquakes-circle"
    }

}