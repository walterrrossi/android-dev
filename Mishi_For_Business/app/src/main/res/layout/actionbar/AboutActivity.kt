package com.faberapps.mishi.actionbar

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.faberapps.mishi.R
import com.faberapps.mishi.activity.SchiacciaActivity
import com.faberapps.mishi.structures.Coupon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_about.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class AboutActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        
        buttonAbout.setOnClickListener { 
            val intentSchiaccia = Intent(this, SchiacciaActivity::class.java)
            startActivity(intentSchiaccia)
        }

        buttonCoupon.setOnClickListener {
            val instant : Instant = Instant.now()
            val zoneId : ZoneId = ZoneId.of("Europe/Rome")
            val zdt : ZonedDateTime = ZonedDateTime.ofInstant(instant, zoneId)
            saveCouponToFirebaseDatabase(zdt)
            Log.d("debug", zdt.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveCouponToFirebaseDatabase(zdt: ZonedDateTime) {
        val uid = FirebaseAuth.getInstance().uid
        var ref = FirebaseDatabase.getInstance().getReference("/users/$uid/coupons")
        val key = ref.push().key
        ref = FirebaseDatabase.getInstance().getReference("/users/$uid/coupons/$key")

        val coupon = key?.let { Coupon(it,"10%","111","Chicco", zdt.toString().substring(0,10), zdt.plusDays(7).toString().substring(0,10)) }
        ref.setValue(coupon)
            .addOnFailureListener{
                Toast.makeText(this, "Failed coupon on db", Toast.LENGTH_LONG).show()
            }
            .addOnSuccessListener {
                Toast.makeText(this, "Yes, coupon on db!", Toast.LENGTH_LONG).show()
            }
    }
}