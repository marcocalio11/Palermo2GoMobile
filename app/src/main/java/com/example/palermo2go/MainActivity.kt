package com.example.palermo2go


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.KeyEvent
import android.view.View

import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.palermo2go.fragments.RegistratiFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.palermo2go.fragments.MapsFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Response


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
    lateinit var progressBar: ProgressBar
    lateinit var view: View
    var maps: MapsFragment? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreference = getSharedPreferences("com.example.palermo2go", Context.MODE_PRIVATE)
        isLogged = sharedPreference.getBoolean("isLogged", false)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAGTOKR", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast

            if (token != null) {
                Log.d("TAGTOKR", token)
            }

        })

        if(isLogged) {
            setContentView(R.layout.logged)
            maps = MapsFragment(this)
            startFragmentOnBack(maps!!)
        } else {
            setContentView(R.layout.activity_main)
            findViewLogin()
            onClick()

        }

    }

     fun setProgressBar(visibility :Int){
         progressBar.visibility = visibility
         view.visibility = visibility
    }



    private fun startFragmentOnBack(fragment: Fragment) {
        supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in,
            R.anim.slide_in,
            R.anim.slide_out,
            R.anim.slide_out
        ).add(R.id.parent, fragment).commit()
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }


    private fun onClick() {

        passwordForgottenTextView.setOnClickListener {
            openForgottenPasswordModal()
        }

        loginButton.setOnClickListener {



            if(textInputEmail.text.toString().isEmpty() || !isValidEmail(textInputEmail.text) ){
                Log.e("INVALID", isValidEmail(textInputEmail.text).toString())
                Log.e("INVALID", textInputEmail.text.toString().isEmpty().toString())
                texInputLayoutEmail.error = "Inserisci un' email valida"
                return@setOnClickListener
            } else {
                texInputLayoutEmail.error = null
            }

            if(texInputPassword.text.toString().isNullOrEmpty()){
                texInputLayoutPassword.error = "Inserisci una password"
                return@setOnClickListener
            } else {
                texInputLayoutPassword.error = null
            }


            setProgressBar(View.VISIBLE)
           Networking.create().login(Networking.LoginModel(textInputEmail.text.toString(), texInputPassword.text.toString(), true)).enqueue(object : retrofit2.Callback<Networking.LoginResponse?>{
         // Networking.create().login(Networking.LoginModel("igandalffi+2@gmail.com", "Alberto89")).enqueue(object : retrofit2.Callback<Networking.LoginResponse?>{
                override fun onResponse(call: Call<Networking.LoginResponse?>, response: Response<Networking.LoginResponse?>) {
             setProgressBar(View.GONE)
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
                  setProgressBar(View.GONE)
                  Toast.makeText(this@MainActivity, "Errore di rete", Toast.LENGTH_SHORT).show()
              }

          })
        }

        registratiButton.setOnClickListener {

            startFragment(RegistratiFragment())
        }
    }

    private fun openForgottenPasswordModal() {
        TODO("Not yet implemented")
    }

    private fun findViewLogin() {
        progressBar = findViewById(R.id.progressBar)
        view = findViewById(R.id.view)
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