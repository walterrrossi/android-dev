package com.faberapps.mishi.structures

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import com.faberapps.mishi.R
import com.faberapps.mishi.activity.SingleQrActivity
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.custom_coupon.view.*
import java.util.*

//Classe che rappresenta il singolo coupon che viene mostrato nel RecyclerView.
class CouponItem(private val coupon: Coupon) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.custom_coupon
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.couponTextView.text = coupon.nomeNegozio
        viewHolder.itemView.valoreTextView.text = coupon.valore
        viewHolder.itemView.dataScadTextView.text = coupon.dataScadenza
        when (coupon.tipo.toLowerCase(Locale.ROOT)) {
            "alimentari" -> viewHolder.itemView.typeImageView.setImageResource(R.drawable.pattern_cibo)
            "giochi" -> viewHolder.itemView.typeImageView.setImageResource(R.drawable.pattern_giochi)
            "cultura" -> viewHolder.itemView.typeImageView.setImageResource(R.drawable.pattern_cultura)
            "abbigliamento" -> viewHolder.itemView.typeImageView.setImageResource(R.drawable.pattern_vestiti)
            "vario" -> viewHolder.itemView.typeImageView.setImageResource(R.drawable.pattern_vario)
        }
        Picasso.get().load(coupon.logoImage).into(viewHolder.itemView.logoImageView)
        viewHolder.itemView.createQrImageView.setOnClickListener {
            val uid = FirebaseAuth.getInstance().uid
            val intentSingleQrActivity = Intent(
                viewHolder.itemView.context,
                SingleQrActivity::class.java
            )
            intentSingleQrActivity.putExtra("idcouponQr", "${coupon.idCoupon}///$uid")
            intentSingleQrActivity.putExtra("id coupon", coupon.idCoupon)
            intentSingleQrActivity.putExtra("nomeNeg", coupon.nomeNegozio)
            intentSingleQrActivity.putExtra("valore", coupon.valore)
            intentSingleQrActivity.putExtra("dataAcquisizione", coupon.dataAcquisizione)
            intentSingleQrActivity.putExtra("dataScadenza", coupon.dataScadenza)

            //Options necessarie per la transition.
            val options = ActivityOptions.makeSceneTransitionAnimation(
                viewHolder.itemView.context as Activity,
                viewHolder.itemView,
                "shared_element_container"
            )
            viewHolder.itemView.context.startActivity(intentSingleQrActivity, options.toBundle())
        }
    }
}