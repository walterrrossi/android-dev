package com.faberapps.mishi.games

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.faberapps.mishi.MainActivity
import com.faberapps.mishi.R
import com.faberapps.mishi.structures.Coupon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_schiaccia_la_talpa.*
import kotlinx.android.synthetic.main.popup_window.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import kotlin.random.Random

//Classe per il gioco Schiaccia la Talpa
class SchiacciaLaTalpaActivity : AppCompatActivity() {

    private var score: Int = 0
    private var imageArrayTalpe = ArrayList<ImageView>()
    private var imageArrayNegative = ArrayList<ImageView>()
    private var handler: Handler = Handler()
    private var runnable: Runnable = Runnable { }
    private var diff: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schiaccia_la_talpa)
        AndroidThreeTen.init(this)

        diff = intent.getStringExtra("difficolta")

        //Quando sciaccio start viene tolto il tutorial e inzia il gioco.
        buttonSchiacciaStart.setOnClickListener {
            tutorialImageView.visibility = View.INVISIBLE
            buttonSchiacciaStart.visibility = View.INVISIBLE
            runGame()
        }

    }

    override fun onBackPressed() {
    }

    //Funzione di inzio gioco.
    private fun runGame() {
        tablePointImageView.visibility = View.VISIBLE
        scoreText.visibility = View.VISIBLE
        timeText.visibility = View.VISIBLE
        val imageArrayTerre = arrayListOf(
            terraImageView1,
            terraImageView2,
            terraImageView3,
            terraImageView4,
            terraImageView5,
            terraImageView6,
            terraImageView7,
            terraImageView8,
            terraImageView9
        )

        for (image in imageArrayTerre) {
            image.visibility = View.VISIBLE
        }
        //Creo oggetto CountDownTimer. Alla fine tutte le immagini diventano invisibili, ogni tick aggiorno il testo.
        object : CountDownTimer(10000, 1000) {
            //Alla fine del countDown tutte le immagini tornano invisibili e controllo il punteggio.
            override fun onFinish() {
                timeText.text = "Fine"
                handler.removeCallbacks(runnable)
                for (image in imageArrayTalpe) {
                    image.visibility = View.INVISIBLE
                }
                for (image in imageArrayNegative) {
                    image.visibility = View.INVISIBLE
                }
                if (score >= 10) {
                    saveCouponToDatabase()
                } else {
                    //Stampo popup di sconfitta.
                    val view = layoutInflater.inflate(R.layout.popup_window, null)
                    val window = PopupWindow(this@SchiacciaLaTalpaActivity)
                    val cl = findViewById<ConstraintLayout>(R.id.schiaccia_activity)
                    window.contentView = view
                    cl.alpha = 0.2F
                    view.titleTextView.text = getString(R.string.peccato)
                    view.descriptionTextView.text = getString(R.string.non_hai_vinto)
                    view.popUpButton.text = getString(R.string.continua)
                    view.popUpButton.setOnClickListener {
                        window.dismiss()
                        val intentMainActivity =
                            Intent(this@SchiacciaLaTalpaActivity, MainActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intentMainActivity)
                        finish()
                    }
                    window.showAtLocation(schiaccia_activity, Gravity.CENTER, 0, 0)
                }
            }

            //Ogni tick aggiorno il testo del tempo.
            override fun onTick(millisUntilFinished: Long) {
                timeText.text = "Time: " + millisUntilFinished / 1000
            }

        }.start()

        //Rendo visibili le talpe.
        imageArrayTalpe = arrayListOf(
            talpaImageView1,
            talpaImageView2,
            talpaImageView3,
            talpaImageView4,
            talpaImageView5,
            talpaImageView6,
            talpaImageView7,
            talpaImageView8,
            talpaImageView9
        )
        imageArrayNegative = arrayListOf(
            negativeImageView1,
            negativeImageView2,
            negativeImageView3,
            negativeImageView4,
            negativeImageView5,
            negativeImageView6,
            negativeImageView7,
            negativeImageView8,
            negativeImageView9
        )
        hideImages()
        score = 0
    }

    //Funzione per aumentare il punteggio ogni volta che viene cliccata una talpa buona.
    fun increaseScore(view: View) {
        score++

        scoreText.text = "Score: " + score
        view.visibility = View.INVISIBLE
    }

    //Funzione per diminuire il punteggio ogni volta che viene cliccata una talpa cattiva.
    fun decreaseScore(view: View) {
        if (score >= 1) {
            score--
        }
        scoreText.text = "Score: " + score
        view.visibility = View.INVISIBLE
    }

    //Funzione per nascondere le immagini. Grazie ad un Runnable e ad un Handler posso scegliere l'intervallo in cui rendere visibili le immagini
    fun hideImages() {

        runnable = Runnable {
            for (image in imageArrayTalpe) {
                image.visibility = View.INVISIBLE
            }
            for (image in imageArrayNegative) {
                image.visibility = View.INVISIBLE
            }
            //Prendo valori random per selezionare le talpe da mostrare
            val indexesGood = IntArray(2) {
                Random.nextInt(0, 9)
            }.asList()
            var indexNegative: Int
            do {
                indexNegative = Random.nextInt(0, 9)
            } while (indexesGood.contains(indexNegative))

            //Animazioni di salita delle talpe.
            val translateAnimation1 = TranslateAnimation(0f, 0f, 45f, 0f)
            translateAnimation1.duration = (100..250).random().toLong()
            val translateAnimation2 = TranslateAnimation(0f, 0f, 45f, 0f)
            translateAnimation2.duration = (100..250).random().toLong()
            val translateAnimation3 = TranslateAnimation(0f, 0f, 45f, 0f)
            translateAnimation3.duration = (100..250).random().toLong()


            imageArrayTalpe[indexesGood[0]].visibility = View.VISIBLE
            imageArrayTalpe[indexesGood[0]].startAnimation(translateAnimation1)
            imageArrayTalpe[indexesGood[1]].visibility = View.VISIBLE
            imageArrayTalpe[indexesGood[1]].startAnimation(translateAnimation2)
            imageArrayNegative[indexNegative].visibility = View.VISIBLE
            imageArrayNegative[indexNegative].startAnimation(translateAnimation3)

            //Scelta della velocità con cui compaiono le talpe.
            when (diff) {
                "1" -> handler.postDelayed(runnable, 500)
                "2" -> handler.postDelayed(runnable, 430)
                "3" -> handler.postDelayed(runnable, 370)
            }
        }
        handler.post(runnable)
    }

    //Funzone per salvare il coupon sul DB.
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
                            Intent(this@SchiacciaLaTalpaActivity, MainActivity::class.java)
                        startActivity(intentMainActivity)
                        finish()
                    }
                    .addOnSuccessListener {
                        //Stampo popup di vittoria.
                        val view = layoutInflater.inflate(R.layout.popup_window, null)
                        val window = PopupWindow(this@SchiacciaLaTalpaActivity)
                        val cl = findViewById<ConstraintLayout>(R.id.schiaccia_activity)
                        window.contentView = view
                        cl.alpha = 0.2F
                        view.titleTextView.text = getString(R.string.complimenti)
                        view.descriptionTextView.text = getString(R.string.hai_vinto_un_coupon)
                        view.popUpButton.text = getString(R.string.continua)
                        view.popUpButton.setOnClickListener {
                            window.dismiss()
                            val intentMainActivity =
                                Intent(this@SchiacciaLaTalpaActivity, MainActivity::class.java)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intentMainActivity)
                            finish()
                        }
                        window.showAtLocation(schiaccia_activity, Gravity.CENTER, 0, 0)
                    }
            }

        })
    }
}