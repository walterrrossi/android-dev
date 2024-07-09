package com.faberapps.mishiforbusiness.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.faberapps.mishiforbusiness.R
import com.faberapps.mishiforbusiness.registration_login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_wait.*

//Classe utilizzata per l'attesa della conferma dell'avvenuta registrazione.
class WaitActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wait)

        buttonForm.setOnClickListener {
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("users/$uid/")
            ref.child("requestDoneFlag").setValue(false)
            val intentShopForm = Intent(this, ShopFormActivity::class.java)
            startActivity(intentShopForm)
            finish()
        }

        buttonLogout.setOnClickListener {
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("users/$uid/")
            ref.child("requestDoneFlag").setValue(false)
            FirebaseAuth.getInstance().signOut()
            val intentLogin = Intent(this, LoginActivity::class.java)
            startActivity(intentLogin)
            finish()
        }
    }
}