package com.example.palermo2go.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.location.Address
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.palermo2go.R
import com.google.android.material.textfield.TextInputEditText
import android.location.Geocoder
import android.os.Build
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class BookNowFragment(val mapsFragment: MapsFragment) : Fragment() {

    var longitude: Double = 0.0
    var latitude: Double = 0.0
    lateinit var textInputEditText: TextInputEditText
    lateinit var expressSwitch: com.google.android.material.switchmaterial.SwitchMaterial
    lateinit var avantiButton: Button
    lateinit var annullaButton: Button
    lateinit var v: View
    lateinit var view1: RelativeLayout
    lateinit var view2: RelativeLayout
    lateinit var view3: RelativeLayout
    lateinit var timePicker: TimePicker
    lateinit var dateInputText: TextInputEditText
    var cal = Calendar.getInstance()
    var hour = 0
    var minute = 0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_book_now, container, false)
        findView()
//        setInitialView()
        return v
    }

    fun setInitialView() {
        val visible = View.VISIBLE
        view1.visibility = visible
        textInputEditText.visibility = visible
        expressSwitch.visibility = visible
        avantiButton.visibility = visible
        annullaButton.visibility = visible
        view2.visibility = View.GONE
        view3.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun findView() {
        timePicker = v.findViewById(R.id.timePicker)
        dateInputText = v.findViewById(R.id.dateInputText)
        textInputEditText = v.findViewById(R.id.textInputEditText)
        expressSwitch = v.findViewById(R.id.expressSwitch)
        avantiButton= v.findViewById(R.id.avantiButton)
        annullaButton= v.findViewById(R.id.annullaButton)
        view1 = v.findViewById(R.id.view1)
        view2 = v.findViewById(R.id.view2)
        view3 = v.findViewById(R.id.view3)
        onClick()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun onClick() {

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val tempCal = Calendar.getInstance()
            if(tempCal.get(Calendar.YEAR) + 2 < year) {
                Toast.makeText(v.context, "Non puoi prenotare per un anno superiore a due", Toast.LENGTH_SHORT).show()
            } else if(tempCal.get(Calendar.YEAR) > year) {
                Toast.makeText(v.context, "Non puoi prenotare per un anno passato", Toast.LENGTH_SHORT).show()
            }else if(tempCal.get(Calendar.YEAR)  == year && monthOfYear < tempCal.get(Calendar.MONTH)){
                Toast.makeText(v.context, "Non puoi prenotare per un mese passato", Toast.LENGTH_SHORT).show()
            } else if(tempCal.get(Calendar.YEAR)  == year && monthOfYear == tempCal.get(Calendar.MONTH) && tempCal.get(Calendar.DAY_OF_MONTH) > dayOfMonth) {
                Log.e("DAYOFMONTH", " " + tempCal.get(Calendar.DAY_OF_MONTH) + " " + dayOfMonth)
                Toast.makeText(v.context, "Non puoi prenotare per un giorno passato", Toast.LENGTH_SHORT).show()
            } else {
                mapsFragment.isToday = cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == monthOfYear && cal.get(Calendar.DAY_OF_MONTH) == dayOfMonth
                Log.e("ISTODAY", mapsFragment.isToday.toString())
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                mapsFragment.bookCalendar = cal
                dateInputText.setText("${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal[Calendar.YEAR]}")
            }
        }

        dateInputText.setOnClickListener {
            DatePickerDialog(v.context, dateSetListener,cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }


        annullaButton.setOnClickListener {
            requireActivity().onBackPressed()
            mapsFragment.removeMark()
        }
        avantiButton.setOnClickListener {

            if(view1.visibility == View.VISIBLE) {
                if(!textInputEditText.text.toString().isNullOrEmpty()){
                    val address = if(textInputEditText.text.toString().contains("palermo", true)) {
                        textInputEditText.text.toString()
                    } else {
                        textInputEditText.text.toString() + " Palermo"
                    }
                    hideKeyboardFrom(v.context, v)
                    getCoordinate(address)
                } else {
                    Toast.makeText(v.context, "Inserisci un indirizzo valido", Toast.LENGTH_SHORT).show()
                }

            } else if (view2.visibility == View.VISIBLE) {

                if(dateInputText.text.isNullOrEmpty()){
                    Toast.makeText(v.context, "Inserisci una data valida", Toast.LENGTH_SHORT).show()
                } else {
                    setView()
                }
            } else if(view3.visibility == View.VISIBLE){
                hour = timePicker.hour
                minute = timePicker.minute
                val hourCalendat = Calendar.getInstance()
                if(mapsFragment.isToday && hour < hourCalendat.get(Calendar.HOUR_OF_DAY) || (hour == hourCalendat.get(Calendar.HOUR_OF_DAY) && minute < hourCalendat.get(Calendar.MINUTE))){
                    Toast.makeText(v.context, "Se hai prenotato per oggi non puoi prenotare per un ora passata", Toast.LENGTH_SHORT).show()
                } else {
                    mapsFragment.bookHour = hour
                    mapsFragment.bookMinute = minute
                    if(mapsFragment.isExpress){
                        mapsFragment.callBook()
                    } else {
                        mapsFragment.getStore()
                    }

                    requireActivity().onBackPressed()
                }
            }
        }
    }

    fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun getCoordinate(location: String) {

        val geocoder = Geocoder(v.context, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(location, 1)
        val address: Address = addresses[0]
        longitude = address.longitude
        latitude =  address.latitude

        Log.e("GetCoordinate", "$location $latitude $longitude")
        if(longitude == 0.0 && latitude == 0.0 ){
            Toast.makeText(v.context, "Indirizzo non trovato ($address)", Toast.LENGTH_SHORT).show()
        } else {
            mapsFragment.indirizzoString = location
            mapsFragment.moveMapBySearch(latitude, longitude, expressSwitch.isChecked)
            setView()
        }

    }

    private fun setView() {
      if(view1.visibility == View.VISIBLE) {
          Log.e("SETVIEW", "1")
          view2.visibility = View.VISIBLE
          view1.visibility = View.GONE
      } else if(view2.visibility == View.VISIBLE) {
          Log.e("SETVIEW", "2")
          view2.visibility = View.GONE
          view3.visibility = View.VISIBLE
      } else {
          Log.e("SETVIEW", "VAI QUI")
      }
    }

    override fun onDestroy() {
        mapsFragment.cartConteiner.visibility = View.GONE
        Log.e("DESTOEY", ":D")
        mapsFragment.bookNowFragment = null
        super.onDestroy()
    }

    override fun onStop() {
        mapsFragment.cartConteiner.visibility = View.GONE
        super.onStop()
    }



}