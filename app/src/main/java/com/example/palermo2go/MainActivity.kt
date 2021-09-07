package com.example.palermo2go


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.palermo2go.fragments.RegistratiFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.stdout.greenurb.fragments.MapsFragment

var isLogged = false

lateinit var texInputLayoutEmail: TextInputLayout
lateinit var textInputEmail: TextInputEditText
lateinit var texInputLayoutPassword: TextInputLayout
lateinit var texInputPassword: TextInputEditText
lateinit var loginButton: Button
lateinit var passwordForgottenTextView: TextView
lateinit var registratiButton: Button




class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(!isLogged) {

            startFragment(MapsFragment())
        } else {

            findViewLogin()
            onClick()

        }

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