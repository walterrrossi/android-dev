package com.faberapps.mishi.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.faberapps.mishi.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import es.dmoral.toasty.Toasty


//Classe per gestire la pagina della mappa.
class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var client: FusedLocationProviderClient

    //Creo tutti i layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = rootView.findViewById(R.id.map) as MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Imposto il fusedLocation
        client = activity?.let { LocationServices.getFusedLocationProviderClient(it) }!!
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    activity,
                    R.raw.style_json
                )
            )
            if (!success) {
                Log.e("style:", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("style", "Can't find style. Error: ", e)
        }
        //Trovo la mia posizione, controllo permessi prima
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }

        googleMap.setOnMyLocationButtonClickListener { return@setOnMyLocationButtonClickListener false }
        allPlaces()
    }

    //Metodi di MapView
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        animateCamera(mMap)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    //Funzion per spostare la camera della mappa sulla posizione corrente.
    private fun animateCamera(googleMap: GoogleMap) {
        if (activity?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED) {
            client.lastLocation
                .addOnSuccessListener {
                    if (it != null) {
                        val me = LatLng(it.latitude, it.longitude)
                        val moveTo: CameraUpdate = CameraUpdateFactory.newLatLngZoom(me, 17F)
                        googleMap.animateCamera(moveTo)
                    } else {
                        Toasty.warning(
                            requireActivity(),
                            "Localizzazione disattivata.\nAttiva la localizzazione.",
                            Toast.LENGTH_LONG,
                            true
                        ).show()
                    }
                }
                .addOnFailureListener {
                    Log.e("ClientLocationError", "$it")
                }
        }
    }


    //Funzione per aggiungere tutti i marker dei negozi presenti sul DB
    private fun allPlaces() {
        var ref = FirebaseDatabase.getInstance().getReference("/shops/")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    ref = it.ref
                    val nome = it.child("name").value.toString()
                    val via = it.child("street").value.toString()
                    val type = it.child("type").value.toString()
                    ref.child("position")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                val latLng = LatLng(
                                    snapshot.child("latitude").value as Double,
                                    snapshot.child("longitude").value as Double
                                )
                                var b: Bitmap? = null

                                when (type) {
                                    "alimentari" -> {
                                        b = BitmapFactory.decodeResource(
                                            resources,
                                            R.drawable.maps_food
                                        )
                                    }
                                    "abbigliamento" -> {
                                        b = BitmapFactory.decodeResource(
                                            resources,
                                            R.drawable.maps_clothes
                                        )
                                    }
                                    "cultura" -> {
                                        b = BitmapFactory.decodeResource(
                                            resources,
                                            R.drawable.maps_culture
                                        )
                                    }
                                    "giochi" -> {
                                        b = BitmapFactory.decodeResource(
                                            resources,
                                            R.drawable.maps_game
                                        )
                                    }
                                    "vario" -> {
                                        b = BitmapFactory.decodeResource(
                                            resources,
                                            R.drawable.maps_various
                                        )
                                    }

                                }

                                val options =
                                    MarkerOptions().position(latLng).title(nome).snippet(via)
                                val height = 150
                                val width = 150
                                val smallMarker: Bitmap =
                                    Bitmap.createScaledBitmap(b!!, width, height, false)
                                val smallMarkerIcon: BitmapDescriptor =
                                    BitmapDescriptorFactory.fromBitmap(smallMarker)
                                options.icon(smallMarkerIcon)
                                mMap.addMarker(options)
                            }

                        })
                }
            }
        })
    }
}