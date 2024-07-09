package com.faberapps.mishi

import android.Manifest
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.faberapps.mishi.actionbar.AboutActivity
import com.faberapps.mishi.registration_login.LoginActivity
import com.faberapps.mishi.ui.main.SectionsAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private val tabTitleArray = arrayListOf(
        "SCAN",
        "COUPON",
        "MAP"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        //Trasition
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementsUseOverlay = false
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsAdapter = SectionsAdapter(this)
        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsAdapter
        viewPager.isUserInputEnabled = false


        val tabs: TabLayout = findViewById(R.id.tabs)
        val tabIconArray = arrayListOf<Drawable>(
            resources.getDrawable(R.drawable.cam_unselected),
            resources.getDrawable(R.drawable.coupon_selected),
            resources.getDrawable(R.drawable.map_unselected)
        )
        //Creo i tab e setto come iniziale la pagina dei coupon.
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            viewPager.currentItem = 1
            tab.text = tabTitleArray[position]
            tab.icon = tabIconArray[position]
        }.attach()
        viewPager.offscreenPageLimit = 2


        //Cambio icone tab ogni volta che si va in un'altra pagina.
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        tabs.getTabAt(0)?.setIcon(R.drawable.cam_selected)
                        tabs.getTabAt(1)?.setIcon(R.drawable.coupon_unselected)
                        tabs.getTabAt(2)?.setIcon(R.drawable.map_unselected)
                    }
                    1 -> {
                        tabs.getTabAt(0)?.setIcon(R.drawable.cam_unselected)
                        tabs.getTabAt(1)?.setIcon(R.drawable.coupon_selected)
                        tabs.getTabAt(2)?.setIcon(R.drawable.map_unselected)
                    }
                    2 -> {
                        tabs.getTabAt(0)?.setIcon(R.drawable.cam_unselected)
                        tabs.getTabAt(1)?.setIcon(R.drawable.coupon_unselected)
                        tabs.getTabAt(2)?.setIcon(R.drawable.map_selected)
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

        //Verifica dei permessi necessari con Dexter, se qualche permesso è rifiutato perennemente mostro un dialog.
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION
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
            .setMessage("Questa app ha bisogno dei permessi della fotocamera e della posizione attivati per funzionare. Per piacere, accettare i permessi.")
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

    //Funzione per verificare che l'utente sia loggato.
    private fun verifyUserLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intentLogin = Intent(this, LoginActivity::class.java)
            startActivity(intentLogin)
        }
    }

}
