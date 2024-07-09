package com.faberapps.mishi.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.*
import com.faberapps.mishi.R
import com.faberapps.mishi.games.MenuGameActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import es.dmoral.toasty.Toasty

//Classe per gestire la pagina dello scanner QR.
class QrFragment : Fragment() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_qr, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        val activity = requireActivity()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        codeScanner = CodeScanner(activity, scannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        //Funzione per la scansione del QR.
        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                checkLocation(it.text)
            }
        }
        scannerView.setOnClickListener {
            when (PackageManager.PERMISSION_GRANTED) {
                activity.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.CAMERA
                    )
                } -> {
                    codeScanner.startPreview()
                }
                else -> {
                    Toasty.warning(
                        activity,
                        "Attiva i permessi dell'app!",
                        Toast.LENGTH_LONG,
                        true
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        when (PackageManager.PERMISSION_GRANTED) {
            activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } -> {
                codeScanner.startPreview()
            }
            else -> {
                activity?.let {
                    Toasty.warning(
                        it,
                        "Attiva i permessi dell'app!",
                        Toast.LENGTH_LONG,
                        true
                    ).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
    }

    //Funzione per controllare che l'utente sia abbastanza vicino al negozio di cui scansiona il QR (100 metri).
    private fun checkLocation(idNeg: String) {
        var ref = FirebaseDatabase.getInstance().getReference("/shops/")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.hasChild(idNeg)) {
                        ref = FirebaseDatabase.getInstance().getReference("/shops/$idNeg/position/")
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                val lat = snapshot.child("latitude").value as Double
                                val long = snapshot.child("longitude").value as Double
                                if (activity?.let {
                                        ActivityCompat.checkSelfPermission(
                                            it,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        )
                                    } == PackageManager.PERMISSION_GRANTED) {
                                    //Acquisizione ultima posizione
                                    fusedLocationClient.lastLocation
                                        .addOnSuccessListener {
                                            if (it != null) {
                                                val distance =
                                                    it.distanceTo(Location(LocationManager.GPS_PROVIDER).apply {
                                                        latitude = lat
                                                        longitude = long
                                                    })
                                                if (distance <= 100) {
                                                    startGame(idNeg)
                                                } else {
                                                    Toasty.error(
                                                        activity!!,
                                                        "Non sei abbastanza vicino al negozio.\nClicca lo schermo per scansionare un nuovo QR.",
                                                        Toast.LENGTH_LONG,
                                                        true
                                                    ).show()
                                                }
                                            } else {
                                                Toasty.warning(
                                                    activity!!,
                                                    "Localizzazione disattivata.\nAttiva la localizzazione.",
                                                    Toast.LENGTH_LONG,
                                                    true
                                                ).show()
                                            }
                                        }
                                        .addOnFailureListener {
                                            Log.e("LocationError", "$it")
                                        }
                                }
                            }
                        })
                    } else {
                        Toasty.error(
                            activity!!,
                            "Codice QR non valido.\nClicca lo schermo per scansionare un nuovo QR.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: DatabaseException) {
                    Toasty.error(
                        activity!!,
                        "Codice QR non valido.\nClicca lo schermo per scansionare un nuovo QR.",
                        Toast.LENGTH_LONG,
                        true
                    ).show()
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    //Funzione per fare inziare uno dei gioco.
    private fun startGame(idNeg: String) {
        val intentMenuGameActivity = Intent(activity, MenuGameActivity::class.java)
        intentMenuGameActivity.putExtra("id negozio", idNeg)
        startActivity(intentMenuGameActivity)
    }
}