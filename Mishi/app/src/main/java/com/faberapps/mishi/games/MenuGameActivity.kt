package com.faberapps.mishi.games

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.faberapps.mishi.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_menu_game.*

//Classe per il menu del gioco.
class MenuGameActivity : AppCompatActivity() {

    private lateinit var diff: String
    private var idNeg : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_game)

        idNeg = intent.getStringExtra("id negozio")

        colorButton()

        //Quando clicco su uno dei bottoni faccio partire uno dei giochi a random a quel livello di difficolta.
        buttonEasy.setOnClickListener {
            diff = "1"
            when (takeAGame()) {
                0 -> {
                    val intentSchiaccia = Intent(this, SchiacciaLaTalpaActivity::class.java)
                    intentSchiaccia.putExtra("id negozio", idNeg)
                    intentSchiaccia.putExtra("difficolta", diff)
                    startActivity(intentSchiaccia)
                    finish()
                }
                1 -> {
                    val intentRuota = Intent(this, RuotaFortunataActivity::class.java)
                    intentRuota.putExtra("id negozio", idNeg)
                    intentRuota.putExtra("difficolta", diff)
                    startActivity(intentRuota)
                    finish()
                }
            }
        }
        buttonMedium.setOnClickListener {
            diff = "2"
            when (takeAGame()) {
                0 -> {
                    val intentSchiaccia = Intent(this, SchiacciaLaTalpaActivity::class.java)
                    intentSchiaccia.putExtra("id negozio", idNeg)
                    intentSchiaccia.putExtra("difficolta", diff)
                    startActivity(intentSchiaccia)
                    finish()
                }
                1 -> {
                    val intentRuota = Intent(this, RuotaFortunataActivity::class.java)
                    intentRuota.putExtra("id negozio", idNeg)
                    intentRuota.putExtra("difficolta", diff)
                    startActivity(intentRuota)
                    finish()
                }
            }

        }
        buttonHard.setOnClickListener {
            diff = "3"
            when (takeAGame()) {
                0 -> {
                    val intentSchiaccia = Intent(this, SchiacciaLaTalpaActivity::class.java)
                    intentSchiaccia.putExtra("id negozio", idNeg)
                    intentSchiaccia.putExtra("difficolta", diff)
                    startActivity(intentSchiaccia)
                    finish()
                }
                1 -> {
                    val intentRuota = Intent(this, RuotaFortunataActivity::class.java)
                    intentRuota.putExtra("id negozio", idNeg)
                    intentRuota.putExtra("difficolta", diff)
                    startActivity(intentRuota)
                    finish()
                }
            }
        }
    }

    //Funzione per colorare i bottoni se non sono presenti coupon di quel livello sul DB.
    private fun colorButton() {
        val ref = FirebaseDatabase.getInstance().getReference("shops/$idNeg/coupons/")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child("1")
                        .exists() && snapshot.child("1/activeCouponFlag").value == false
                ) {
                    buttonEasy.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_negative_button))
                    buttonEasy.setTextColor(Color.rgb(0, 0, 0))
                    buttonEasy.isClickable = false
                    buttonEasy.isEnabled = false
                }
                if (snapshot.child("2")
                        .exists() && snapshot.child("2/activeCouponFlag").value == false
                ) {
                    buttonMedium.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_negative_button))
                    buttonMedium.setTextColor(Color.rgb(0, 0, 0))
                    buttonMedium.isClickable = false
                    buttonMedium.isEnabled = false
                }
                if (snapshot.child("3")
                        .exists() && snapshot.child("3/activeCouponFlag").value == false
                ) {
                    buttonHard.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_negative_button))
                    buttonHard.setTextColor(Color.rgb(0, 0, 0))
                    buttonHard.isClickable = false
                    buttonHard.isEnabled = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    //Funzione per prendere un random che identifichi il gioco da fare.
    private fun takeAGame(): Int {
        return (0..2).random()
    }
}