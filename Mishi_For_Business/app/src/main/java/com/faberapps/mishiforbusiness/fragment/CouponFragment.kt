package com.faberapps.mishiforbusiness.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.faberapps.mishiforbusiness.R
import com.faberapps.mishiforbusiness.actionbar.AboutActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_coupon.*
import java.util.*

//Classe per gestire la pagina dei coupon
class CouponFragment : Fragment() {

    private var selectedPhotoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_coupon, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getProfileDataFromDatabase()
        getCouponFromDatabase()

        iconProfileImageView.setOnClickListener {
            //Intent per la selezione dell'immagine di profilo.
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        editImageViewFacile.setOnClickListener {
            editCoupon(1)
        }
        editImageViewMedio.setOnClickListener {
            editCoupon(2)
        }

        editImageViewDifficile.setOnClickListener {
            editCoupon(3)
        }

        saveButtonFacile.setOnClickListener {
            editTextFacile.setBackgroundDrawable(null)
            editTextFacile.inputType = 0
            saveCouponOnDatabase(1)
        }
        saveButtonMedio.setOnClickListener {
            editTextMedio.setBackgroundDrawable(null)
            editTextMedio.inputType = 0
            saveCouponOnDatabase(2)
        }
        saveButtonDifficile.setOnClickListener {
            editTextDifficile.setBackgroundDrawable(null)
            editTextDifficile.inputType = 0
            saveCouponOnDatabase(3)
        }

        //Controlli switch.
        switchFacile.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                switchFacile.text = "Attivato"
                val uid = FirebaseAuth.getInstance().uid
                val ref = FirebaseDatabase.getInstance()
                    .getReference("shops/$uid/coupons/1/activeCouponFlag")
                ref.setValue(true)
            } else {
                switchFacile.text = "Disattivato"
                val uid = FirebaseAuth.getInstance().uid
                val ref = FirebaseDatabase.getInstance()
                    .getReference("shops/$uid/coupons/1/activeCouponFlag")
                ref.setValue(false)
            }
        }

        switchMedio.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                switchMedio.text = "Attivato"
                val uid = FirebaseAuth.getInstance().uid
                val ref = FirebaseDatabase.getInstance()
                    .getReference("shops/$uid/coupons/2/activeCouponFlag")
                ref.setValue(true)
            } else {
                switchMedio.text = "Disattivato"
                val uid = FirebaseAuth.getInstance().uid
                val ref = FirebaseDatabase.getInstance()
                    .getReference("shops/$uid/coupons/2/activeCouponFlag")
                ref.setValue(false)
            }
        }

        switchDifficile.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                switchDifficile.text = "Attivato"
                val uid = FirebaseAuth.getInstance().uid
                val ref = FirebaseDatabase.getInstance()
                    .getReference("shops/$uid/coupons/3/activeCouponFlag")
                ref.setValue(true)
            } else {
                switchDifficile.text = "Disattivato"
                val uid = FirebaseAuth.getInstance().uid
                val ref = FirebaseDatabase.getInstance()
                    .getReference("shops/$uid/coupons/3/activeCouponFlag")
                ref.setValue(false)
            }
        }
    }

    //Funzione per gestire edit dei valorei dei coupon.
    private fun editCoupon(index: Int) {
        when (index) {
            1 -> {
                editTextFacile.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_edittext))
                editTextFacile.inputType = 1
                editTextFacile.isClickable = true
                editTextFacile.isLongClickable = true
                editTextFacile.isContextClickable = true
            }
            2 -> {
                editTextMedio.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_edittext))
                editTextMedio.inputType = 1
                editTextMedio.isClickable = true
                editTextMedio.isLongClickable = true
                editTextMedio.isContextClickable = true
            }
            3 -> {
                editTextDifficile.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_edittext))
                editTextDifficile.inputType = 1
                editTextDifficile.isClickable = true
                editTextDifficile.isLongClickable = true
                editTextDifficile.isContextClickable = true
            }
        }
    }

    //Funzione per salvare il valore dei coupon editati.
    private fun saveCouponOnDatabase(index: Int) {
        val uid = FirebaseAuth.getInstance().uid
        when (index) {
            1 -> {
                if (!editTextFacile.text.isEmpty()) {
                    val descrizione = editTextFacile.text.toString()
                    val ref = FirebaseDatabase.getInstance().getReference("shops/$uid/coupons/")
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.hasChild("$index")) {
                                snapshot.child("$index/descrizione").ref.setValue(descrizione)
                                snapshot.child("$index/activeCouponFlag").ref.setValue(true)
                            } else {
                                snapshot.ref.child("$index/descrizione").push()
                                snapshot.child("$index/descrizione").ref.setValue(descrizione)
                                snapshot.child("$index/activeCouponFlag").ref.setValue(true)
                            }
                        }

                    })

                    editTextFacile.hint = descrizione
                    editTextFacile.text = null
                    editTextFacile.isClickable = false
                    editTextFacile.isLongClickable = false
                    editTextFacile.isContextClickable = false
                }
            }

            2 -> {
                if (!editTextMedio.text.isEmpty()) {
                    val descrizione = editTextMedio.text.toString()
                    val ref = FirebaseDatabase.getInstance().getReference("shops/$uid/coupons/")
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.hasChild("$index")) {
                                snapshot.child("$index/descrizione").ref.setValue(descrizione)
                                snapshot.child("$index/activeCouponFlag").ref.setValue(true)
                            } else {
                                snapshot.ref.child("$index/descrizione").push()
                                snapshot.child("$index/descrizione").ref.setValue(descrizione)
                                snapshot.child("$index/activeCouponFlag").ref.setValue(true)
                            }
                        }

                    })

                    editTextMedio.hint = descrizione
                    editTextMedio.text = null
                    editTextMedio.isClickable = false
                    editTextMedio.isLongClickable = false
                    editTextMedio.isContextClickable = false
                }
            }

            3 -> {
                if (!editTextDifficile.text.isEmpty()) {
                    val descrizione = editTextDifficile.text.toString()
                    val ref = FirebaseDatabase.getInstance().getReference("shops/$uid/coupons/")
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.hasChild("$index")) {
                                snapshot.child("$index/descrizione").ref.setValue(descrizione)
                                snapshot.child("$index/activeCouponFlag").ref.setValue(true)
                            } else {
                                snapshot.ref.child("$index/descrizione").push()
                                snapshot.child("$index/descrizione").ref.setValue(descrizione)
                                snapshot.child("$index/activeCouponFlag").ref.setValue(true)
                            }
                        }

                    })

                    editTextDifficile.hint = descrizione
                    editTextDifficile.text = null
                    editTextDifficile.isClickable = false
                    editTextDifficile.isLongClickable = false
                    editTextDifficile.isContextClickable = false
                }
            }
        }
    }

    //Funzione per scaricare immagine profilo dal DB.
    private fun getProfileDataFromDatabase() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("shops/$uid/")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                benvenutoTextView.text = "Benvenuto, " + snapshot.child("name").value.toString()
                if (snapshot.child("logoImage").exists()) {
                    Picasso.get().load(snapshot.child("logoImage").value.toString())
                        .into(iconProfileImageView)
                } else {
                    val b: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_logo)
                    iconProfileImageView.setImageBitmap(b)
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    //Funzione per scaricare i valori dei coupon dal DB.
    private fun getCouponFromDatabase() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/shops/$uid/coupons/")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.hasChild("1/descrizione")) {
                    editTextFacile.hint =
                        snapshot.child("1/descrizione").value.toString()
                    switchFacile.isChecked = snapshot.child("1/activeCouponFlag").value as Boolean
                    //In base al valore dello switch cambio background carta.
                    if (switchFacile.isChecked) {
                        colorViewFacile.setBackgroundColor(resources.getColor(R.color.active_green))
                    } else {
                        colorViewFacile.setBackgroundColor(resources.getColor(R.color.non_active_red))
                    }
                }
                if (snapshot.hasChild("2/descrizione")) {
                    editTextMedio.hint =
                        snapshot.child("2/descrizione").value.toString()
                    switchMedio.isChecked = snapshot.child("2/activeCouponFlag").value as Boolean
                    //In base al valore dello switch cambio background carta.
                    if (switchMedio.isChecked) {
                        colorViewMedio.setBackgroundColor(resources.getColor(R.color.active_green))
                    } else {
                        colorViewMedio.setBackgroundColor(resources.getColor(R.color.non_active_red))
                    }

                }
                if (snapshot.hasChild("3/descrizione")) {
                    editTextDifficile.hint =
                        snapshot.child("3/descrizione").value.toString()
                    switchDifficile.isChecked =
                        snapshot.child("3/activeCouponFlag").value as Boolean
                    //In base al valore dello switch cambio background carta.
                    if (switchDifficile.isChecked) {
                        colorViewDifficile.setBackgroundColor(resources.getColor(R.color.active_green))
                    } else {
                        colorViewDifficile.setBackgroundColor(resources.getColor(R.color.non_active_red))
                    }
                }
            }
        })
    }

    //ActivityResult della selezione dell'immagine.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            //Intent per ridimensionare immagine selezionata.
            val intent = activity?.applicationContext?.let {
                CropImage.activity(selectedPhotoUri)
                    .setFixAspectRatio(true)
                    .getIntent(it)
            }
            startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == AppCompatActivity.RESULT_OK) {
                val resultUri = result.uri
                val bitmapPhoto =
                    MediaStore.Images.Media.getBitmap(activity?.contentResolver, resultUri)
                iconProfileImageView.setImageBitmap(bitmapPhoto)
                uploadPhotoToDatabase(resultUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    //Funzione per salvare foto sul DB(Storage).
    private fun uploadPhotoToDatabase(resultUri: Uri) {

        val filename = UUID.randomUUID().toString()

        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(resultUri)
            .addOnSuccessListener {
                Log.d(AboutActivity::class.java.name, "Image uploaded")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d(AboutActivity::class.java.name, it.toString())
                    val uid = FirebaseAuth.getInstance().uid
                    val refShop = FirebaseDatabase.getInstance().getReference("shops/$uid/")
                    refShop.child("logoImage").setValue(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d(AboutActivity::class.java.name, "Image not uploaded")
                activity?.let { it ->
                    Toasty.error(
                        it,
                        "Immagine non caricata. Riprovare.",
                        Toast.LENGTH_LONG,
                        true
                    ).show()
                }
            }
    }

}

