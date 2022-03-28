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
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import java.lang.ref.WeakReference

class MapFragment : Fragment(){ //OnMapReadyCallback

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    //Mapbox
        //User location
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

        mapView
        locationPermissionHelper = LocationPermissionHelper(WeakReference(activity))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
        return root
    }

    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
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

}