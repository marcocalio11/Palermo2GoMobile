package com.example.palermo2go

import android.content.Context
import android.util.Log
import android.view.View

class Helper {


    fun getImageByString(string: String): Int{
        if(string == "car") {
            Log.e("IFIMAGE", "Catr")
           return R.drawable.car
        } else if(string == "bike"){
            Log.e("IFIMAGE", "Bike")
            return R.drawable.bici
        } else if (string == "scooter") {
            Log.e("IFIMAGE", "Scooter")
           return R.drawable.monopattino
        } else if(string == "motorcycle") {
            Log.e("IFIMAGE", "motorino")
            return R.drawable.motorino
        }
        return -1
    }

}