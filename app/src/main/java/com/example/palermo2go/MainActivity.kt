package com.example.palermo2go


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.palermo2go.fragments.RegistratiFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.palermo2go.fragments.MapsFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback


var isLogged = false




class MainActivity : AppCompatActivity() {


    lateinit var texInputLayoutEmail: TextInputLayout
    lateinit var textInputEmail: TextInputEditText
    lateinit var texInputLayoutPassword: TextInputLayout
    lateinit var texInputPassword: TextInputEditText
    lateinit var loginButton: Button
    lateinit var passwordForgottenTextView: TextView
    lateinit var registratiButton: Button
    lateinit var sharedPreference: SharedPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreference = getSharedPreferences("com.example.palermo2go", Context.MODE_PRIVATE)
        isLogged = sharedPreference.getBoolean("isLogged", false)
        if(isLogged) {
            setContentView(R.layout.logged)
            startFragmentOnBack(MapsFragment())
        } else {
            setContentView(R.layout.activity_main)
            findViewLogin()
            onClick()

        }

    }



    private fun startFragmentOnBack(fragment: Fragment) {
        supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in,
            R.anim.slide_in,
            R.anim.slide_out,
            R.anim.slide_out
        ).add(R.id.parent, fragment).commit()
    }


    private fun onClick() {

        loginButton.setOnClickListener {
          Networking.create().login(Networking.LoginModel("igandalffi+2@gmail.com", "Alberto89")).enqueue(object : retrofit2.Callback<Networking.LoginResponse?>{
                override fun onResponse(call: Call<Networking.LoginResponse?>, response: Response<Networking.LoginResponse?>) {

                    if(response.isSuccessful){
                        if(response.body()?.data?.accessToken != null) {
                            Log.e("RESPONSE", response.body()!!.data!!.accessToken.toString() )
                            sharedPreference.edit().putBoolean("isLogged", true).apply()
                            sharedPreference.edit().putString("token", response.body()!!.data!!.accessToken).apply()
                            val intent = Intent(this@MainActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Email o Password errata", Toast.LENGTH_SHORT).show()
                    }

                }

              override fun onFailure(call: Call<Networking.LoginResponse?>, t: Throwable) {
                  Toast.makeText(this@MainActivity, "Errore di rete", Toast.LENGTH_SHORT).show()
              }

          })
        }

        registratiButton.setOnClickListener {

            startFragment(RegistratiFragment())
        }
    }

    private fun findViewLogin() {
        texInputLayoutEmail = findViewById(R.id.texInputLayoutEmail)
        textInputEmail = findViewById(R.id.textInputEmail)
        texInputLayoutPassword = findViewById(R.id.texInputLayoutPassword)
        texInputPassword = findViewById(R.id.texInputPassword)
        loginButton = findViewById(R.id.loginButton)
        passwordForgottenTextView = findViewById(R.id.passwordForgottenTextView)
        registratiButton = findViewById(R.id.registratiButton)

    }

    fun startFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in,
            R.anim.slide_in,
            R.anim.slide_out,
            R.anim.slide_out
        ).addToBackStack(null).add(R.id.mainContainer, fragment).commit()
    }
}