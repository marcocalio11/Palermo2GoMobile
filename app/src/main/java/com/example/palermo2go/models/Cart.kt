package com.example.palermo2go.models

class Cart(val number: String, val expire: String, val cvv: String) {

    fun getNumberCensured(): String{
        return "************${number[12]}${number[13]}${number[14]}${number[15]}"
    }
}