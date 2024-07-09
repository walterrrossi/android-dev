package com.faberapps.mishi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.faberapps.mishi.R
import com.faberapps.mishi.structures.Coupon
import com.faberapps.mishi.structures.CouponItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jakewharton.threetenabp.AndroidThreeTen
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_coupon.*
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

//Classe per gestire la pagina dei coupon
class CouponFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_coupon, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AndroidThreeTen.init(activity)
        fetchAndDeleteCoupon()
        searchCoupon()
        filterYourCoupon()


        //Animazione del pulsante di refresh, quando cliccato viene fatto di nuovo il fetch dei coupon
        refreshImageView.setOnClickListener {
            val rotateAnimation = RotateAnimation(
                0F,
                720F,
                1,
                0.5f,
                1,
                0.5f
            )
            rotateAnimation.duration = 720
            rotateAnimation.fillAfter = true
            rotateAnimation.interpolator = DecelerateInterpolator()
            rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }

            })
            refreshImageView.animation = rotateAnimation
            refreshImageView.startAnimation(rotateAnimation)
            fetchAndDeleteCoupon()
            couponSearchView.clearFocus()
            couponSearchView.setQuery("", true)
        }
    }


    override fun onResume() {
        super.onResume()
        fetchAndDeleteCoupon()
    }

    //Funzione per scaricare tutti i coupon, se un coupon è scaduto viene eliminato dal DB.
    private fun fetchAndDeleteCoupon() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/coupons/")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                val today: LocalDate = LocalDate.now(ZoneId.of("Europe/Rome"))
                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
                p0.children.forEach {
                    if (LocalDate.parse(it.child("dataScadenza").value.toString(), formatter)
                            .isBefore(today)
                    ) {
                        it.ref.removeValue()
                    } else {
                        val coupon = it.getValue(Coupon::class.java)
                        if (coupon != null) {
                            adapter.add(CouponItem(coupon))
                        }
                    }
                }
                if (coupon_list != null) {
                    coupon_list.adapter = adapter
                }
            }
        })
    }

    //Funzione per ricercare i coupon sul DB. La ricerca viene fatta sul nome del negozio
    private fun searchCoupon() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/coupons/")

        //Listerner della view di ricerca
        couponSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //Quando si preme invio
            override fun onQueryTextSubmit(query: String?): Boolean {

                ref.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val adapter = GroupAdapter<GroupieViewHolder>()
                        snapshot.children.forEach {
                            val nomeNeg: String =
                                it.child("nomeNegozio").value.toString().toLowerCase(Locale.ROOT)
                            if (nomeNeg.contains(query.toString().toLowerCase(Locale.ROOT))) {
                                val coupon = it.getValue(Coupon::class.java)
                                if (coupon != null) {
                                    adapter.add(CouponItem(coupon))
                                }

                                if (coupon_list != null) {
                                    coupon_list.adapter = adapter
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
                return false
            }

            //Mentre si scrive
            override fun onQueryTextChange(newText: String?): Boolean {
                if (couponSearchView.query.isNotEmpty()) {

                    ref.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val adapter = GroupAdapter<GroupieViewHolder>()
                            p0.children.forEach {
                                val nomeNeg: String =
                                    it.child("nomeNegozio").value.toString()
                                        .toLowerCase(Locale.ROOT)
                                if (nomeNeg.contains(newText.toString().toLowerCase(Locale.ROOT))) {
                                    val coupon = it.getValue(Coupon::class.java)
                                    if (coupon != null) {
                                        adapter.add(CouponItem(coupon))
                                    }

                                    if (coupon_list != null) {
                                        coupon_list.adapter = adapter
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
                } else {
                    ref.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val adapter = GroupAdapter<GroupieViewHolder>()
                            p0.children.forEach {
                                val coupon = it.getValue(Coupon::class.java)
                                if (coupon != null) {
                                    adapter.add(CouponItem(coupon))
                                }
                            }
                            if (coupon_list != null) {
                                coupon_list.adapter = adapter
                            }
                        }
                    })
                }
                return false
            }
        })

    }

    //Funzione per filtrare i coupon sul DB in base al tipo.
    private fun filterYourCoupon() {

        //Per tutti i tipi di coupon
        filterCard0.setOnClickListener {
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("users/$uid/coupons/")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adapter = GroupAdapter<GroupieViewHolder>()
                    snapshot.children.forEach {
                        val coupon = it.getValue(Coupon::class.java)
                        if (coupon != null) {
                            adapter.add(CouponItem(coupon))
                        }

                        if (coupon_list != null) {
                            coupon_list.adapter = adapter
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

        //Alimentari
        filterCard1.setOnClickListener {
            val typeSearch = filterTextView1.text.toString().toLowerCase(Locale.ROOT)
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("users/$uid/coupons/")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adapter = GroupAdapter<GroupieViewHolder>()
                    snapshot.children.forEach {
                        if (it.child("tipo").value.toString() == typeSearch) {
                            val coupon = it.getValue(Coupon::class.java)
                            if (coupon != null) {
                                adapter.add(CouponItem(coupon))
                            }

                            if (coupon_list != null) {
                                coupon_list.adapter = adapter
                            }
                        } else {
                            coupon_list.adapter = adapter
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

        //Abbigliamento
        filterCard2.setOnClickListener {
            val typeSearch = filterTextView2.text.toString().toLowerCase(Locale.ROOT)
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("users/$uid/coupons/")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adapter = GroupAdapter<GroupieViewHolder>()
                    snapshot.children.forEach {
                        if (it.child("tipo").value.toString() == typeSearch) {
                            val coupon = it.getValue(Coupon::class.java)
                            if (coupon != null) {
                                adapter.add(CouponItem(coupon))
                            }

                            if (coupon_list != null) {
                                coupon_list.adapter = adapter
                            }
                        } else {
                            coupon_list.adapter = adapter
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

        //Cultura
        filterCard3.setOnClickListener {
            val typeSearch = filterTextView3.text.toString().toLowerCase(Locale.ROOT)
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("users/$uid/coupons/")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adapter = GroupAdapter<GroupieViewHolder>()
                    snapshot.children.forEach {
                        if (it.child("tipo").value.toString() == typeSearch) {
                            val coupon = it.getValue(Coupon::class.java)
                            if (coupon != null) {
                                adapter.add(CouponItem(coupon))
                            }

                            if (coupon_list != null) {
                                coupon_list.adapter = adapter
                            }
                        } else {
                            coupon_list.adapter = adapter
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

        //Giochi
        filterCard4.setOnClickListener {
            val typeSearch = filterTextView4.text.toString().toLowerCase(Locale.ROOT)
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("users/$uid/coupons/")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adapter = GroupAdapter<GroupieViewHolder>()
                    snapshot.children.forEach {
                        if (it.child("tipo").value.toString() == typeSearch) {
                            val coupon = it.getValue(Coupon::class.java)
                            if (coupon != null) {
                                adapter.add(CouponItem(coupon))
                            }

                            if (coupon_list != null) {
                                coupon_list.adapter = adapter
                            }
                        } else {
                            coupon_list.adapter = adapter
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

        //Vario
        filterCard5.setOnClickListener {
            val typeSearch = filterTextView5.text.toString().toLowerCase(Locale.ROOT)
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("users/$uid/coupons/")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adapter = GroupAdapter<GroupieViewHolder>()
                    snapshot.children.forEach {
                        if (it.child("tipo").value.toString() == typeSearch) {
                            val coupon = it.getValue(Coupon::class.java)
                            if (coupon != null) {
                                adapter.add(CouponItem(coupon))
                            }

                            if (coupon_list != null) {
                                coupon_list.adapter = adapter
                            }
                        } else {
                            coupon_list.adapter = adapter
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }
    }

}




