package com.stdout.greenurb.adapters


import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.palermo2go.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.example.palermo2go.fragments.MapsFragment
import com.example.palermo2go.model.Veichle
import java.util.*
import kotlin.collections.ArrayList


class BookNowAdapter(private val book: ArrayList<Veichle>, val mapsFragment: MapsFragment) :
    RecyclerView.Adapter<BookNowAdapter.ViewHolder>() {
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val detailTextView = view.findViewById<TextView>(R.id.detailTextView)
        val address = view.findViewById<TextView>(R.id.address)
        val image = view.findViewById<ImageView>(R.id.image)
        val prenotaButton = view.findViewById<Button>(R.id.attiva)
        val pilotSwitch = view.findViewById<SwitchMaterial>(R.id.auristaSwitch)

        fun binding(position: Int, book: ArrayList<Veichle>, mapsFragment: MapsFragment) {

            book[position]!!.vehicle?.let { Log.e("IFIMAGE", it) }

            if(book[position].vehicle == "car") {
                Log.e("IFIMAGE", "Catr")
                image.setImageDrawable(view.resources.getDrawable(R.drawable.car, null))
            } else if(book[position].vehicle == "bike"){
                Log.e("IFIMAGE", "Bike")
                image.setImageDrawable(view.resources.getDrawable(R.drawable.bici, null))
            } else if (book[position].vehicle == "scooter") {
                Log.e("IFIMAGE", "Scooter")
                image.setImageDrawable(view.resources.getDrawable(R.drawable.monopattino, null))
            } else if(book[position].vehicle == "motorcycle") {
                Log.e("IFIMAGE", "motorino")
                image.setImageDrawable(view.resources.getDrawable(R.drawable.motorino, null))
            }

            address.text = address.text.toString() + " ${mapsFragment.indirizzoString}"

            pilotSwitch.visibility = if(book[position].vehicle == "car") {
                View.VISIBLE
            } else {
                View.GONE
            }

            detailTextView.text = "Prezzo: ${book[position].price_km} €"

            prenotaButton.setOnClickListener {
                val bookDialog = Dialog(view.context, R.style.Theme_Palermo2Go)
                bookDialog.setContentView(R.layout.time_dialog)
                bookDialog.window?.setBackgroundDrawable(ColorDrawable(view.context.resources.getColor(R.color.lightTrasparent)))
                val number = bookDialog.findViewById<TextInputEditText>(R.id.number)
                val destinazioneText = bookDialog.findViewById<TextInputEditText>(R.id.destinazioneText)
                val prenotaButton = bookDialog.findViewById<Button>(R.id.prenotaButton)
                var destCheck = false



                number.addTextChangedListener(object : TextWatcher{
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        //
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if(s!= null) {
                            if(s.length > 2) {
                                number.setText(s[0].toString() + s[1].toString())
                                Toast.makeText(view.context, "Devi inserire un valore che va da 0 a 24", Toast.LENGTH_SHORT).show()
                            } else if(s.isNotEmpty()){
                                if(s.toString().toInt() > 24 || s.toString().toInt() < 0) {
                                    number.setText(s[0].toString())
                                    Toast.makeText(view.context, "Devi inserire un valore che va da 0 a 24", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                    }

                    override fun afterTextChanged(s: Editable?) {
                        //
                    }
                })

                prenotaButton.setOnClickListener {

                    destCheck = getCoordinate(destinazioneText.text.toString(), view, mapsFragment)

                    if(number.text.toString() == ""){
                        Toast.makeText(view.context, "Inserisci le ore per cui vuoi prenotare il mezzo", Toast.LENGTH_SHORT).show()
                    } else if(destinazioneText.text.toString() == "") {
                        Toast.makeText(view.context, "Inserisci una via valida", Toast.LENGTH_SHORT).show()
                    } else if(!destCheck){
                        Toast.makeText(view.context, "Inserisci una via valida", Toast.LENGTH_SHORT).show()
                    } else {
                        mapsFragment.timeToBook = number.text.toString().toInt()
                        mapsFragment.destBook = prenotaButton.text.toString()
                        bookDialog.dismiss()
                        mapsFragment.requireActivity().onBackPressed()
                        Toast.makeText(view.context, "Prenotazione compleatata", Toast.LENGTH_SHORT).show()
                    }
                }
                bookDialog.setCanceledOnTouchOutside(true)
                bookDialog.window!!.setWindowAnimations(R.style.DialogNoAnimation)

                bookDialog.show()
            }
        }


        private fun getCoordinate(location: String, view: View, mapsFragment: MapsFragment): Boolean {
            Log.e("DESTINAZION", location)
            if(location == "") {
                Toast.makeText(view.context, "Inserisci una via valida", Toast.LENGTH_SHORT).show()
                return false
            } else {

                val location2 = if(!location.contains("Palermo", false)){
                    "$location Palermo"
                } else {
                    location
                }

                Log.e("DESTINAZION",  " 2" + location2)
                val geocoder = Geocoder(mapsFragment.requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocationName(location2, 1)
                val address: Address = addresses[0]
                if(address.latitude == 0.0 && address.longitude == 0.0){
                    Log.e("DESTINAZION", "NON VALiA")
                    Toast.makeText(view.context, "Inserisci una via valida", Toast.LENGTH_SHORT).show()
                    return false
                } else {
                    mapsFragment.destinationLon = address.longitude
                    mapsFragment.destinationLat = address.latitude
                    return true
                }

            }
        }



    }






    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.book_now_list, viewGroup, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.binding(position, book, mapsFragment)
    }


    override fun getItemCount() = book.size

}