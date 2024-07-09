package com.faberapps.mishi.structures

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//Classe che rappresenta il singolo utente che viene salvato sul DB.
@Parcelize
class User(val uid: String, val email: String) : Parcelable {
    constructor() : this("", "")
}