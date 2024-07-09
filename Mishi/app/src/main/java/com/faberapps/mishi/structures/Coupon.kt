package com.faberapps.mishi.structures

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//Classe che rappresenta il singolo coupon che viene salvato nel DB.
@Parcelize
class Coupon(
    val idCoupon: String,
    val valore: String,
    val tipo: String,
    val idNegozio: String,
    val nomeNegozio: String,
    val dataAcquisizione: String,
    val dataScadenza: String,
    val logoImage: String
) : Parcelable {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    )
}