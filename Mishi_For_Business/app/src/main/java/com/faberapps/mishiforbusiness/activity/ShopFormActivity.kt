package com.faberapps.mishiforbusiness.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.faberapps.mishiforbusiness.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_shop_form.*

//Classe per gestire il form per l'inserimento del negozio?
class ShopFormActivity : AppCompatActivity() {

    private var category: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_form)

        val spinner: Spinner = findViewById(R.id.spinnerCategory)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                category = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        ArrayAdapter.createFromResource(
            this,
            R.array.type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        sendButton.setOnClickListener {
            sendEmail()
        }
    }

    override fun onResume() {
        super.onResume()
        val uid = FirebaseAuth.getInstance().uid
        val refUser = FirebaseDatabase.getInstance().getReference("users/$uid/")
        refUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child("requestDoneFlag").value == true && snapshot.child("requestDoneFlag")
                        .exists()
                ) {
                    val intentWait =
                        Intent(this@ShopFormActivity, WaitActivity::class.java)
                    startActivity(intentWait)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    //Funzione per inviare mail di richiesta registrazione, tramite il client email del telefono.
    private fun sendEmail() {
        val nomeNeg = nameShopEditText.text.toString()
        val nomeProprietario = firstnameEditText.text.toString()
        val cognomeProprietario = lastnameEditText.text.toString()
        val tel = phoneEditText.text.toString()
        val addr = addressEditText.text.toString()


        if (nomeNeg.isEmpty() || nomeProprietario.isEmpty() || cognomeProprietario.isEmpty() || tel.isEmpty() || addr.isEmpty()) {
            Toasty.warning(
                this,
                "Alcuni campi vuoti. Compila tutti i campi.",
                Toast.LENGTH_LONG,
                true
            ).show()
        } else {
            //Set campi email.
            val email = "mishi.faber@gmail.com"
            val subject = "Richiesta attivazione $nomeNeg"
            val text = "$nomeProprietario $cognomeProprietario $tel $addr $category"
            //Intent client email.
            val intentEmail = Intent(Intent.ACTION_SENDTO).apply {
                type = "*/*"
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            if (intent.resolveActivity(packageManager) != null) {
                val uid = FirebaseAuth.getInstance().uid
                val ref = FirebaseDatabase.getInstance().getReference("users/$uid/")
                ref.child("requestDoneFlag").setValue(true)
                startActivity(intentEmail)
            }
        }
    }

}