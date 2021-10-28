package com.example.palermo2go

import com.example.palermo2go.model.RoadModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Networking {


    @GET("api/users/bookings?status=booked")
    fun getActive(

        @retrofit2.http.Header("Authorization") token : String

    ): Call<RoadModel>



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

