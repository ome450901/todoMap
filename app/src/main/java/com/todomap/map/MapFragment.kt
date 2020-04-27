package com.todomap.map

import android.Manifest
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.todomap.R
import com.todomap.databinding.FragmentMapBinding
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    private lateinit var viewModel: MapViewModel

    private lateinit var behavior: BottomSheetBehavior<ConstraintLayout>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMapBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)

        val application = requireNotNull(this.activity).application

        viewModel = MapViewModelFactory(application).create(MapViewModel::class.java)

        viewModel.isMapReady.observe(viewLifecycleOwner, Observer { isMapReady ->
            if (isMapReady == true) {
                requestLastLocationWithPermissionCheck()
            }
        })

        setupBottomSheet(binding)

        viewModel.location.observe(viewLifecycleOwner, Observer {
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(it.latitude, it.longitude))
                .zoom(17f)
                .build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        })

        viewModel.snackbarEvent.observe(viewLifecycleOwner, Observer {
            Snackbar.make(
                activity!!.findViewById(android.R.id.content),
                it,
                Snackbar.LENGTH_LONG
            ).show()
        })

        binding.btnMyLocation.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        val googleMapFragment =
            childFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment
        googleMapFragment.getMapAsync(this)

        binding.lifecycleOwner = this

        return binding.root
    }

    private fun setupBottomSheet(binding: FragmentMapBinding) {
        behavior = BottomSheetBehavior.from(binding.layout.bottomSheetLayout)

        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)

        behavior.isFitToContents = false
        //        behavior.halfExpandedRatio = 0.75f
        behavior.expandedOffset = (displayMetrics.heightPixels * 0.2).toInt()

        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        viewModel.onMapReady()
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun requestLastLocation() {
        viewModel.onRequestLastLocation()
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onPermissionDenied() {
        viewModel.onPermissionDenied()
    }

}
