package com.example.palermo2go

import android.graphics.Bitmap
import com.example.palermo2go.model.RoadModel
import com.example.palermo2go.model.Veichle
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File

interface Networking {

    @POST("api/users/booking")
    fun confirmBook(
        @Body body: ConfirmBook,
        @retrofit2.http.Header("Authorization") token : String
    ): Call<String>
    data class ConfirmBook(
        var start_date: String,
        var minutes: Int,
        var vehicle: String,
        var with_driver: Boolean,
        var ride_destination: String,
        var store: String,
        var start_location: String,
        var isExpress: Boolean
    )

    @GET("api/vehicles")
    fun getVeichle(@retrofit2.http.Header("Authorization") token : String): Call<ResponseVeichle>
    data class ResponseVeichle(
        @SerializedName("data")
        @Expose
        var data: ArrayList<Veichle>?
    )


    @POST("api/stores")
    fun getStores(
        @Body myLatLng: MyLatLng,
        @retrofit2.http.Header("Authorization") token : String
    ) : Call<ResponseStores>

    data class ResponseStores(
        @SerializedName("data")
        @Expose
        var data: ArrayList<Stores>?
    )

    data class MyLatLng(var lat: Double, var lng: Double)

    data class Stores(

        @SerializedName("id")
        @Expose
        var id: Int? = null,
        @SerializedName("address")
        @Expose
        var address: String? = null,
        @SerializedName("lon")
        @Expose
        var lon: Double? = null,
        @SerializedName("lat")
        @Expose
        var lat: Double? = null,
        @SerializedName("distance")
        @Expose
        var distance: Double? = null,
        @SerializedName("heading")
        @Expose
        var heading: Double? = null,

        )
    @POST("/api/users/propic")
    fun updatePropic(
        @Body propic: DataPropic,
        @retrofit2.http.Header("Authorization") token : String
    ): Call<Gson>

    data class DataPropic(
        val propic: File
    )

    @GET("api/users/bookings?status=booked")
    fun getActive(

        @retrofit2.http.Header("Authorization") token : String

    ): Call<RoadModel>

    @GET("api/users/logged")
    fun getLoggedUser(
        @retrofit2.http.Header("Authorization") token : String
    ): Call<ResponseLoggedUser>

     class ResponseLoggedUser(

         @SerializedName("success")
         @Expose
         var success: Boolean? = null,
         @SerializedName("data")
         @Expose
         var data: UserData? = null

     )


    class UserData(
        @SerializedName("firstName")
        @Expose
        var firstName: String? = null,
        @SerializedName("lastName")
        @Expose
        var lastName: String? = null,
        @SerializedName("phone")
        @Expose
        var phone: String? = null,
        @SerializedName("id")
        @Expose
        var id: Int? = null,
        @SerializedName("propic")
        @Expose
        var propic: String? = null,
        @SerializedName("drivingLicence")
        @Expose
        var drivingLicence: Boolean? = null

    )


    @GET("api/users/bookings?status=completed")
    fun getHistory(

        @retrofit2.http.Header("Authorization") token : String

    ): Call<RoadModel>

    @DELETE("/api/users/bookings/{id}")
    fun deleteBook(
        @Path("id") id: String,
        @retrofit2.http.Header("Authorization") token : String
    ) : Call<Gson>

    @POST("api/users/login")
    fun login(@Body loginModel: LoginModel): Call<LoginResponse?>

    data class LoginModel(var email: String, var password: String)

     class LoginResponse(

         @SerializedName("success")
         @Expose
         var success: Boolean? = null,
         @SerializedName("data")
         @Expose
         var data: LoginData? = null

        )

     class LoginData(
        @SerializedName("message")
        @Expose
        var message: String? = null,
        @SerializedName("accessToken")
        @Expose
        var accessToken: String? = null
        )

    companion object {

        var BASE_URL = "https://palermo2go.herokuapp.com/"

        fun create() : Networking {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(Networking::class.java)

        }
    }


}

