package com.example.palermo2go.model

import com.google.gson.annotations.SerializedName

data class RoadModel(
    var success: Boolean?,

    @SerializedName("data")
                     var data: ArrayList<Road?>) {

}

data class Road(
    @SerializedName("id")
    var id: Int?,
    @SerializedName("start_ride_date")
    var start_ride_date: String?,
    @SerializedName("end_ride_date")
    var end_ride_date: String?,
    @SerializedName("status")
    var status: String?,
    @SerializedName("with_driver")
    var with_driver: Boolean?,
    @SerializedName("ride_destination")
    var ride_destination: String?,
    @SerializedName("is_express")
    var is_express: Boolean?,
    @SerializedName("start_location")
    var start_location: String?,
    @SerializedName("price")
    var price: Double?,
    @SerializedName("end_lat")
    var end_lat: Double?,
    @SerializedName("end_lon")
    var end_lon: Double?,
    @SerializedName("isAlerted")
    var isAlerted: Boolean,
    @SerializedName("veichle")
    var veichle: Veichle?


) {

}

data class Veichle(
    @SerializedName("id")
    var id: Int?,
    @SerializedName("veichle")
    var veichle: String?,
    @SerializedName("price_km")
    var price_km: Double?,
) {


}
