package com.faberapps.mishiforbusiness.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.*
import com.faberapps.mishiforbusiness.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jakewharton.threetenabp.AndroidThreeTen
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.popup_window.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

//Classe per gestire la pagina dello scanner QR.
class QrFragment : Fragment() {

    private lateinit var codeScanner: CodeScanner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        val activity = requireActivity()
        AndroidThreeTen.init(activity)
        codeScanner = CodeScanner(activity, scannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        //Funzione per la scansione del QR
        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                val idcoupon = it.text.substringBefore("///")
                val uid = it.text.substringAfter("///")
                checkValidCoupon(idcoupon, uid)
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

    private fun restartCamera() {
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

    //Funzione per verificare che il coupon sia valido per il negozio che scansiona e che non sia scaduto.
    private fun checkValidCoupon(idcoupon: String, uid: String) {
        val sid = FirebaseAuth.getInstance().uid
        val today: LocalDate = LocalDate.now(ZoneId.of("Europe/Rome"))
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid/coupons/$idcoupon/")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child("idNegozio").value == sid && LocalDate.parse(
                        snapshot.child("dataScadenza").value.toString(),
                        formatter
                    ).isAfter(today)
                ) {
                    ref.removeValue()
                    //Stampo popup valido.
                    val view = layoutInflater.inflate(R.layout.popup_window, null)
                    val window = PopupWindow(activity)
                    val cl = getView()?.findViewById<ConstraintLayout>(R.id.main_activity)
                    window.contentView = view
                    cl?.alpha = 0.2F
                    view.titleTextView.setBackgroundColor(Color.rgb(100,221,23))
                    view.popUpButton.setBackgroundColor(Color.rgb(100,221,23))
                    view.cardPopupView.setCardBackgroundColor(Color.rgb(118,255,3))
                    view.titleTextView.text = "Coupon valido"
                    view.descriptionTextView.text = "Questo coupon può essere utilizzato nel tuo negozio.\nValore del coupon: ${snapshot.child("valore").value.toString()}."
                    view.popUpButton.text = "Continua"
                    view.popUpButton.setOnClickListener {
                        window.dismiss()
                        restartCamera()
                    }
                    window.showAtLocation(getView(), Gravity.CENTER, 0, 0)
                } else {
                    //Stampo popup non valido.
                    val view = layoutInflater.inflate(R.layout.popup_window, null)
                    val window = PopupWindow(activity)
                    val cl = getView()?.findViewById<ConstraintLayout>(R.id.main_activity)
                    window.contentView = view
                    cl?.alpha = 0.2F
                    view.titleTextView.setBackgroundColor(Color.rgb(221,44,0))
                    view.popUpButton.setBackgroundColor(Color.rgb(221,44,0))
                    view.cardPopupView.setCardBackgroundColor(Color.rgb(255,61,0))
                    view.titleTextView.text = "Coupon NON valido"
                    view.descriptionTextView.text = "Questo coupon probabilmente è scaduto o non è valido per il tuo negozio."
                    view.popUpButton.text = "Continua"
                    view.popUpButton.setOnClickListener {
                        window.dismiss()
                        restartCamera()
                    }
                    window.showAtLocation(getView(), Gravity.CENTER, 0, 0)
                }
            }

        })
    }
}