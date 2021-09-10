package com.example.palermo2go


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.palermo2go.fragments.RegistratiFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.palermo2go.fragments.MapsFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


var isLogged = false




class MainActivity : AppCompatActivity() {


    lateinit var texInputLayoutEmail: TextInputLayout
    lateinit var textInputEmail: TextInputEditText
    lateinit var texInputLayoutPassword: TextInputLayout
    lateinit var texInputPassword: TextInputEditText
    lateinit var loginButton: Button
    lateinit var passwordForgottenTextView: TextView
    lateinit var registratiButton: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!isLogged) {
            FirebaseApp.initializeApp(this)
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