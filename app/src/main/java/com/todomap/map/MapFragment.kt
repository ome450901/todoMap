package com.todomap.map

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.todomap.R
import com.todomap.database.Todo
import com.todomap.databinding.FragmentMapBinding
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions


@RuntimePermissions
class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    private lateinit var viewModel: MapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMapBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)

        val application = requireNotNull(this.activity).application
        val firestore = Firebase.firestore
        viewModel = MapViewModelFactory(firestore, application).create(MapViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.bottomNavigation.setOnNavigationItemSelectedListener(viewModel)

        setupTodoRecyclerView(binding)

        viewModel.isMapReady.observe(viewLifecycleOwner, Observer { isMapReady ->
            if (isMapReady == true) {
                setupGoogleMapWithPermissionCheck()
            }
        })

        viewModel.cameraPosition.observe(viewLifecycleOwner, Observer {
            googleMap.animateCamera(it)
        })

        viewModel.snackbarEvent.observe(viewLifecycleOwner, Observer {
            showSnackBar(it)
        })

        viewModel.bottomNavigationSelectedItem.observe(viewLifecycleOwner, Observer {
            binding.bottomNavigation.menu.findItem(it.itemId).isChecked = true
        })

        val googleMapFragment =
            childFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment
        googleMapFragment.getMapAsync(this)

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.onBackPressed().not()) {
                        activity?.finish()
                    }
                }
            })

        return binding.root
    }

    private fun setupTodoRecyclerView(binding: FragmentMapBinding) {
        val adapter =
            TodoAdapter(TodoAdapter.TodoAdapterListener(object : TodoAdapter.TodoEventListener {
                override fun onDeleteClick(todoId: String) {
                    viewModel.onTodoDeleted(todoId)
                }

                override fun onSaveClick(todo: Todo) {
                    viewModel.onTodoSaved(todo)
                }
            }))

        binding.recyclerView.adapter = adapter

        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)

        viewModel.allTodoList.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val centerView = snapHelper.findSnapView(recyclerView.layoutManager)
                val pos = recyclerView.layoutManager?.getPosition(centerView!!)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    viewModel.onTodoSelected(pos!!)
                }
            }
        })
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(
            activity!!.findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        viewModel.onMapReady()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun setupGoogleMap() {
        googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            isMyLocationEnabled = true
            setOnMyLocationButtonClickListener {
                requestLastLocationWithPermissionCheck()
                true
            }

            setOnMapClickListener {
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(it)
                        .title("Chosen location")
                )
                viewModel.onMarkerAdded(it, marker)
            }
        }

        requestLastLocationWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun requestLastLocation() {
        viewModel.requestLastLocation()
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onPermissionDenied() {
        viewModel.onPermissionDenied()
    }
}
