package com.example.messenger.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
//FIXME user class name changed to User1
//i dont know if we need this class?
class User1(val uid:String, val username:String):Parcelable{
    constructor(): this("","")
}