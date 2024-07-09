package com.faberapps.mishiforbusiness

import android.Manifest
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.faberapps.mishiforbusiness.actionbar.AboutActivity
import com.faberapps.mishiforbusiness.activity.ShopFormActivity
import com.faberapps.mishiforbusiness.activity.WaitActivity
import com.faberapps.mishiforbusiness.registration_login.LoginActivity
import com.faberapps.mishiforbusiness.ui.main.SectionsAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private val tabTitleArray = arrayListOf(
        "SCAN",
        "COUPON"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsAdapter = SectionsAdapter(this)
        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsAdapter
        viewPager.isUserInputEnabled = false


        val tabs: TabLayout = findViewById(R.id.tabs)
        val tabIconArray = arrayListOf<Drawable>(
            resources.getDrawable(R.drawable.cam_unselected),
            resources.getDrawable(R.drawable.coupon_selected)
        )
        //Creo i tab e setto come iniziale la pagina dei coupon.
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            viewPager.currentItem = 1
            tab.text = tabTitleArray[position]
            tab.icon = tabIconArray[position]
        }.attach()
        viewPager.offscreenPageLimit = 1

        //Cambio icone tab ogni volta che si va in un'altra pagina.
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        tabs.getTabAt(0)?.setIcon(R.drawable.cam_selected)
                        tabs.getTabAt(1)?.setIcon(R.drawable.coupon_unselected)
                    }
                    1 -> {
                        tabs.getTabAt(0)?.setIcon(R.drawable.cam_unselected)
                        tabs.getTabAt(1)?.setIcon(R.drawable.coupon_selected)
                    }
                }
            }
        })

        //Menu AppBar (Material)
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_about -> {
                    val intentAbout = Intent(this, AboutActivity::class.java)
                    startActivity(intentAbout)
                    true
                }
                R.id.menu_logout -> {
                    val intentLogin = Intent(this, LoginActivity::class.java)
                    FirebaseAuth.getInstance().signOut()
                    startActivity(intentLogin)
                    finish()
                    true
                }


                else -> false
            }
        }


        //Controllo se l'utente è già loggato
        verifyUserLoggedIn()

        //Verifica dei permessi necessari con Dexter, se qualche permesso è rifiutato perennemente mostro un dialog
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0 != null) {
                        if (p0.areAllPermissionsGranted()) {
                            Log.i("Permissions: ", "All permissions granted")
                        }
                        if (p0.isAnyPermissionPermanentlyDenied) {
                            showSettingsDialog()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }
            })
            .withErrorListener { p0 -> Log.e("Dexter", "Errore permessi:$p0") }
            .onSameThread()
            .check()

    }


    //Funzione per mostrare il dialog. Posso aprire le impostazioni del'app per accettare i permessi o chiudere l'app.
    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Permessi richiesti")
            .setMessage("Questa app ha bisogno del permesso della fotocamera attivato per funzionare. Per piacere, accettare il permesso.")
            .setPositiveButton("Impostazioni") { dialog, which ->
                dialog.cancel()
                openSettings()
            }
            .setNegativeButton("Chiudi App") { dialog, which ->
                dialog.cancel()
                finish()
                exitProcess(0)
            }
            .show()
    }

    //Funzione per le impostazioni relative all'app.
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    //Funzione per verificare che l'utente sia loggato. Controllo anche se la richiesta di registrazione è gia stata fatta.
    private fun verifyUserLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intentLogin = Intent(this, LoginActivity::class.java)
            startActivity(intentLogin)
            finish()
        } else {
            val ref = FirebaseDatabase.getInstance().getReference("/shops/$uid")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val refUser = FirebaseDatabase.getInstance().getReference("users/$uid/")
                        refUser.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.child("requestDoneFlag").value == true && snapshot.child(
                                        "requestDoneFlag"
                                    ).exists()
                                ) {
                                    val intentWait =
                                        Intent(this@MainActivity, WaitActivity::class.java)
                                    startActivity(intentWait)
                                    finish()
                                } else {
                                    val intentShopForm =
                                        Intent(this@MainActivity, ShopFormActivity::class.java)
                                    startActivity(intentShopForm)
                                    finish()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })
                    }
                }
            })
        }
    }
}