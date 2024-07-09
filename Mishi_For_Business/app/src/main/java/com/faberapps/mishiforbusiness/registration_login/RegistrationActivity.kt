package com.faberapps.mishiforbusiness.registration_login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.faberapps.mishiforbusiness.R
import com.faberapps.mishiforbusiness.activity.ShopFormActivity
import com.faberapps.mishiforbusiness.structures.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_registration.*

//Classe per la gestione della registrazione del negoziante.
class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        buttonRegistration.setOnClickListener {
            performRegister()
        }

        textViewRegistration.setOnClickListener {
            val intentLogin = Intent(this, LoginActivity::class.java)
            startActivity(intentLogin)
        }
    }

    //Funzione per effettuare la registrazione tramite Firebase.
    private fun performRegister() {
        buttonRegistration.isClickable = false
        val email = emailEditTextRegistration.text.toString()
        val password = passwordEditTextRegistration.text.toString()
        val confirmPassword = confirmPasswordEditTextRegistration.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Campi vuoti", Toast.LENGTH_LONG).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Le password non corrispondono", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnFailureListener {
                Toasty.error(
                    this,
                    "Errore del database: ${it.message}",
                    Toast.LENGTH_LONG,
                    true
                ).show()
                Log.d(RegistrationActivity::class.java.name, "Error on registration ${it.message}")
            }
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d(RegistrationActivity::class.java.name, "Success")
                saveUserToFirebaseDatabase()
            }
    }

    //Funzione per salvare l'utente sul DB.
    private fun saveUserToFirebaseDatabase() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val email = emailEditTextRegistration.text.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, email)
        ref.setValue(user)
            .addOnFailureListener {
                Toasty.error(
                    this,
                    "Utente non creato correttamente. Ripetere la registrazione.",
                    Toast.LENGTH_LONG,
                    true
                ).show()
            }
            .addOnSuccessListener {
                val intentForm = Intent(this, ShopFormActivity::class.java)
                startActivity(intentForm)
                finish()
            }
    }
}