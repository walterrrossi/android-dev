package com.faberapps.mishi.games

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.faberapps.mishi.MainActivity
import com.faberapps.mishi.R
import com.faberapps.mishi.structures.Coupon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_ruota_fortunata.*
import kotlinx.android.synthetic.main.popup_window.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import kotlin.math.floor
import kotlin.random.Random

//Classe per il gioco della Ruota della Fortuna.
class RuotaFortunataActivity : AppCompatActivity(), Animation.AnimationListener {

    private var intNumeroSpicchi: Int = 8
    private var lngDegrees: Long = 0
    private var diff: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruota_fortunata)

        diff = intent.getStringExtra("difficolta")

        //In base alla difficoltà del gioco imposto l'immagine della ruota.
        when (diff) {
            "1" -> {
                ruotaImageView.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.spinner_wheel_easy,
                        null
                    )
                )
                intNumeroSpicchi = 8
            }
            "2" -> {
                ruotaImageView.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.spinner_wheel_medium,
                        null
                    )
                )
                intNumeroSpicchi = 12
            }
            "3" -> {
                ruotaImageView.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.spinner_wheel_hard,
                        null
                    )
                )
                intNumeroSpicchi = 16
            }
        }

        //Quando clicco Start inizia l'animazione dell'immagine della ruota.
        buttonStart.setOnClickListener {
            //Durata rotazione
            val ran = Random.nextInt(360) + 5000
            val rotateAnimation = RotateAnimation(
                lngDegrees.toFloat(),
                (lngDegrees + ran).toFloat(),
                1,
                0.5f,
                1,
                0.5f
            )
            rotateAnimation.duration = ran.toLong()
            rotateAnimation.fillAfter = true
            rotateAnimation.interpolator = DecelerateInterpolator(2.0f)
            rotateAnimation.setAnimationListener(this)
            ruotaImageView.animation = rotateAnimation
            ruotaImageView.startAnimation(rotateAnimation)
            lngDegrees = ((lngDegrees + ran) % 360)
        }

    }

    override fun onBackPressed() {
    }

    //Metodi dell'AnimationListener.
    override fun onAnimationStart(animation: Animation?) {
    }

    override fun onAnimationEnd(animation: Animation?) {
        //Quando finisce animazione calcolo in che posizione sono arrivato.
        val finalPosition =
            (intNumeroSpicchi.toDouble() - floor(lngDegrees / (360.0 / intNumeroSpicchi.toDouble()))).toInt()
        //Controllo se sono arrivato nella posizione giusta. Se è corretta salvo il coupon sul DB, se è sbagliata genero popup per la perdita.
        if ((diff == "1" && (finalPosition == 3 || finalPosition == 6 || finalPosition == 8)) ||
            (diff == "2" && (finalPosition == 2 || finalPosition == 7 || finalPosition == 10)) ||
            (diff == "3" && (finalPosition == 2 || finalPosition == 8 || finalPosition == 12))
        ) {
            saveCouponToDatabase()
        } else {
            val view = layoutInflater.inflate(R.layout.popup_window, null)
            val window = PopupWindow(this@RuotaFortunataActivity)
            val cl = findViewById<ConstraintLayout>(R.id.ruota_activity)
            window.contentView = view
            cl.alpha = 0.2F
            view.titleTextView.text = getString(R.string.peccato)
            view.descriptionTextView.text = getString(R.string.non_hai_vinto)
            view.popUpButton.text = getString(R.string.continua)
            view.popUpButton.setOnClickListener {
                window.dismiss()
                val intentMainActivity =
                    Intent(this@RuotaFortunataActivity, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intentMainActivity)
                finish()
            }
            window.showAtLocation(ruota_activity, Gravity.CENTER, 0, 0)
        }
    }


    override fun onAnimationRepeat(animation: Animation?) {

    }

    //Funzione per salvare il coupon sul DB.
    private fun saveCouponToDatabase() {
        val idNeg = intent.getStringExtra("id negozio")
        var ref = FirebaseDatabase.getInstance().getReference("/shops/$idNeg/")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                //Acquisizione dati del coupon appena vinto.
                val nome = snapshot.child("name").value.toString()
                val descrizione = snapshot.child("coupons/$diff/descrizione").value.toString()
                val tipo = snapshot.child("type").value.toString()
                val logoImageUrl = snapshot.child("logoImage").value.toString()
                val today: LocalDate = LocalDate.now(org.threeten.bp.ZoneId.of("Europe/Rome"))
                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
                val uid = FirebaseAuth.getInstance().uid
                ref = FirebaseDatabase.getInstance().getReference("/users/$uid/coupons")
                val key = ref.push().key
                ref = FirebaseDatabase.getInstance().getReference("/users/$uid/coupons/$key")
                val coupon = key?.let {
                    Coupon(
                        it,
                        descrizione,
                        tipo,
                        idNeg!!,
                        nome,
                        today.format(formatter).toString(),
                        today.plusDays(7).format(formatter).toString(),
                        logoImageUrl
                    )
                }
                ref.setValue(coupon)
                    .addOnFailureListener {
                        Log.e("FirebaseError", "$it")
                        val intentMainActivity =
                            Intent(this@RuotaFortunataActivity, MainActivity::class.java)
                        startActivity(intentMainActivity)
                        finish()
                    }
                    .addOnSuccessListener {
                        //Stampo popup di vittoria.
                        val view = layoutInflater.inflate(R.layout.popup_window, null)
                        val window = PopupWindow(this@RuotaFortunataActivity)
                        val cl = findViewById<ConstraintLayout>(R.id.ruota_activity)
                        window.contentView = view
                        cl.alpha = 0.2F
                        view.titleTextView.text = getString(R.string.complimenti)
                        view.descriptionTextView.text = getString(R.string.hai_vinto_un_coupon)
                        view.popUpButton.text = getString(R.string.continua)
                        view.popUpButton.setOnClickListener {
                            window.dismiss()
                            val intentMainActivity =
                                Intent(this@RuotaFortunataActivity, MainActivity::class.java)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intentMainActivity)
                            finish()
                        }
                        window.showAtLocation(ruota_activity, Gravity.CENTER, 0, 0)
                    }
            }

        })
    }

}