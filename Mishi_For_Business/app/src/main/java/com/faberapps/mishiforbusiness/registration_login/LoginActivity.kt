package com.faberapps.mishiforbusiness.registration_login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.faberapps.mishiforbusiness.MainActivity
import com.faberapps.mishiforbusiness.R
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.*

//Classe per la gestione del login del negoziante.
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        buttonLogin.setOnClickListener {
            performLogin()
        }

        loginTextView.setOnClickListener {
            val intentRegistration = Intent(this, RegistrationActivity::class.java)
            startActivity(intentRegistration)
        }
    }

    //Funzione per effettuare il login tramite Firebase.
    private fun performLogin() {
        buttonLogin.isClickable = false
        val email = emailEditTextLogin.text.toString()
        val password = passwordEditTextLogin.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toasty.warning(
                this,
                "Alcuni campi vuoti.",
                Toast.LENGTH_LONG,
                true
            ).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnFailureListener {
                Toasty.error(
                    this,
                    "Errore del database: ${it.message}",
                    Toast.LENGTH_LONG,
                    true
                ).show()
                Log.d(LoginActivity::class.java.name, "Error on login ${it.message}")
            }
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d(LoginActivity::class.java.name, "Success")
                val intentMain = Intent(this, MainActivity::class.java)
                startActivity(intentMain)
                finish()
            }
    }
}