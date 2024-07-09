package com.faberapps.mishi.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.faberapps.mishi.MainActivity
import com.faberapps.mishi.R
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_single_qr.*

//Classe utilizzata per rappresentare il singolo coupon da mostrare al negoziante
class SingleQrActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        //Start transition
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(layoutSingle)
            duration = 1000L
        }

        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(layoutSingle)
            duration = 250L
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_qr)


        //Acquisizione dati da extra intent
        val idQr = intent.extras?.getString("idcouponQr")
        val id = intent.extras?.getString("id coupon")
        val nomeNeg = intent.extras?.getString("nomeNeg")
        val valore = intent.extras?.getString("valore")
        val dataAcquisizione = intent.extras?.getString("dataAcquisizione")
        val dataScadenza = intent.extras?.getString("dataScadenza")
        if (idQr != null) {
            nomeTextViewSingleQr.text = nomeNeg
            valoreTextViewSingleQr.text = valore
            dataAcquisizioneTextViewSingleQr.text = dataAcquisizione
            dataScadenzaTextViewSingleQr.text = dataScadenza
            createQR(idQr)
        }
        //Quando il negoziante scansiona il QR ritorno alla Main
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid/coupons/$id")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val intentMain = Intent(this@SingleQrActivity, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intentMain)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    //Funzione per creare il QR code
    private fun createQR(idQr: String) {
        val barcodeEncoder = BarcodeEncoder()
        val bitmap: Bitmap = barcodeEncoder.encodeBitmap(idQr, BarcodeFormat.QR_CODE, 600, 600)
        qrImageView.setImageBitmap(bitmap)
    }
}