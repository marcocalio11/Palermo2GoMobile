package com.example.palermo2go.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.example.palermo2go.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.w3c.dom.Text
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import com.example.palermo2go.Networking
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.util.*


class RegistratiFragment : Fragment() {


    lateinit var v: View
    lateinit var backButton: ImageButton
    lateinit var texInputLayoutName: TextInputLayout
    lateinit var textInputName: TextInputEditText
    lateinit var texInputLayoutSurname: TextInputLayout
    lateinit var textInputSurname: TextInputEditText
    lateinit var texInputLayoutEmail: TextInputLayout
    lateinit var textInputEmail: TextInputEditText
    lateinit var texInputLayoutConfirmEmail: TextInputLayout
    lateinit var textInputConfirmEmail: TextInputEditText
    lateinit var texInputLayoutPass: TextInputLayout
    lateinit var textInputPass: TextInputEditText
    lateinit var texInputLayoutConfirmPass: TextInputLayout
    lateinit var textInputConfirmPass: TextInputEditText
    lateinit var dateTextLayout: TextInputLayout
    lateinit var dateText: TextInputEditText
    lateinit var registratiButton: Button
    lateinit var phoneLayout: TextInputLayout
    lateinit var phone: TextInputEditText
    var birth: String = ""

    var nameCheck = false
    var surnnameCheck = false
    var emailCheck = false
    var confirmEmailCheck = false
    var passwordCheck = false
    var confirmPasswordCheck = false
    var dateCheck = false

    var cal = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_registrati, container, false)
        findView()
        onClick()
        textWatcher()

        return v

    }

    private fun textWatcher() {
        textInputName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                print(null)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    nameCheck = s.isNotEmpty()
                    if(!nameCheck) texInputLayoutName.error = "Inserisci nome"

                }
            }

            override fun afterTextChanged(s: Editable?) {
                print(null)
            }

        })

        textInputSurname.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                print(null)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    surnnameCheck = s.isNotEmpty()
                    if(!surnnameCheck) texInputLayoutSurname.error = "Inserisci cognome"
                }
            }

            override fun afterTextChanged(s: Editable?) {
                print(null)
            }

        })

        textInputEmail.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                print(null)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                   emailCheck= isValidEmail(s)
                    if(!emailCheck) {
                        emailCheck = false
                        texInputLayoutEmail.error = "Inserisci un'email valida"
                    } else {
                        emailCheck = true
                        texInputLayoutEmail.error = null
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                print(null)
            }

        })

        texInputLayoutConfirmEmail.visibility=View.GONE

        textInputConfirmEmail.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                print(null)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    Log.e("ConfirmEmail", s.toString() + " Confidm " + (textInputConfirmEmail.text.toString() != textInputEmail.text.toString()))
                    Log.e("ConfirmEmail", s.toString() + " confirm  " + (textInputConfirmEmail.text.toString() + " " + textInputEmail.text.toString()))
                    confirmEmailCheck = isValidEmail(s)
                    if(!confirmEmailCheck) {
                        texInputLayoutConfirmEmail.error = "Inserisci un'email valida"
                    } else {
                        if(textInputConfirmEmail.text.toString() != textInputEmail.text.toString()) {

                            confirmEmailCheck = false
                            texInputLayoutConfirmEmail.error = "Le email non coincidono"
                        } else {
                            confirmEmailCheck = true
                            texInputLayoutEmail.error = null
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                print(null)
            }

        })


        textInputPass.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                print(null)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if( s.length> 6) {
                        texInputLayoutPass.error = null
                        passwordCheck = true
                    } else {
                        texInputLayoutPass.error = "Inserisci una password con almeno 6 caratteri"
                        passwordCheck = false
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                print(null)
            }

        })

        textInputConfirmPass.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                print(null)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if( textInputConfirmPass.text.toString() == textInputPass.text.toString()) {
                        texInputLayoutConfirmPass.error = null
                        confirmPasswordCheck = true
                    } else {
                        texInputLayoutConfirmPass.error = "Le password non coincidono"
                        confirmPasswordCheck = false
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                print(null)
            }

        })

        phone.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(isValidPhoneNumber(s)){
                    phoneLayout.error = null
                } else {
                    phoneLayout.error = "Inserisci un numero di telefono valido"
                }
            }

            override fun afterTextChanged(s: Editable?) {
               //
            }

        })

    }

    private fun onClick() {

        registratiButton.setOnClickListener {
            if(nameCheck && surnnameCheck && emailCheck && true && passwordCheck && confirmPasswordCheck && dateCheck && isValidPhoneNumber(phone.text)){
                Networking.create().register(Networking.RegisterBody(textInputName.text.toString(), textInputSurname.text.toString(), textInputEmail.text.toString(), birth,
                textInputPass.text.toString(),
                phone.toString(),
                textInputConfirmPass.text.toString())).enqueue(object: Callback<Gson>{
                    override fun onResponse(call: Call<Gson>, response: Response<Gson>) {
                        if (response.isSuccessful){

                            requireActivity().onBackPressed()
                            Toast.makeText(v.context, "Registrazione avvenuta con successo", Toast.LENGTH_LONG).show()
                            Toast.makeText(v.context, "Conferma l'account dall'indirizzo email", Toast.LENGTH_LONG).show()

                        } else {
                            Toast.makeText(v.context, "Errrore nella registrazione", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Gson>, t: Throwable) {
                        Toast.makeText(v.context, "Errrore di rete", Toast.LENGTH_SHORT).show()
                    }

                })

            } else {
                Toast.makeText(v.context, "Compila tutti i campi", Toast.LENGTH_LONG).show()
            }
        }

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            dateText.setText("${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH)}/${cal[Calendar.YEAR]}")
            birth = "${cal[Calendar.YEAR]}-${cal[Calendar.MONTH]}-${cal.get(Calendar.DAY_OF_MONTH)}"
            dateCheck = true

            }



        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        dateText.setOnClickListener {
            Log.e("CLICK", "CLICK")
            DatePickerDialog(v.context, dateSetListener,cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun findView() {
        backButton = v.findViewById(R.id.backButton)
        texInputLayoutName = v.findViewById(R.id.texInputLayoutName)
        textInputName = v.findViewById(R.id.textInputName)
        texInputLayoutSurname = v.findViewById(R.id.texInputLayoutSurname)
        textInputSurname = v.findViewById(R.id.textInputSurname)
        texInputLayoutEmail = v.findViewById(R.id.texInputLayoutEmail)
        textInputEmail = v.findViewById(R.id.textInputEmail)
        texInputLayoutConfirmEmail = v.findViewById(R.id.texInputLayoutConfirmEmail)
        textInputConfirmEmail = v.findViewById(R.id.textInputConfirmEmail)
        texInputLayoutPass = v.findViewById(R.id.texInputLayoutPass)
        textInputPass = v.findViewById(R.id.textInputPass)
        texInputLayoutConfirmPass = v.findViewById(R.id.texInputLayoutConfirmPass)
        textInputConfirmPass = v.findViewById(R.id.textInputConfirmPass)
        dateTextLayout = v.findViewById(R.id.dateTextLayout)
        dateText = v.findViewById(R.id.dateText)
        registratiButton = v.findViewById(R.id.registratiButton)
        phoneLayout = v.findViewById(R.id.phoneLayout)
        phone = v.findViewById(R.id.phone)
    }


    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

}

fun isValidPhoneNumber(target: CharSequence?): Boolean {
    return if (target == null || TextUtils.isEmpty(target)) {
        false
    } else {
        Patterns.PHONE.matcher(target).matches()
    }
}