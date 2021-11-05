package com.stdout.greenurb.adapters


import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.os.NetworkOnMainThreadException
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
import com.example.palermo2go.Helper
import com.example.palermo2go.Networking
import com.example.palermo2go.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.example.palermo2go.fragments.MapsFragment
import com.example.palermo2go.model.Veichle
import com.google.rpc.Help
import com.google.type.DateTime
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import javax.security.auth.callback.Callback
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


            image.setImageDrawable(view.resources.getDrawable(Helper().getImageByString(book[position]!!.vehicle!!), null))

            address.text = address.text.toString() + " ${mapsFragment.indirizzoString}"

            mapsFragment.bookCalendar?.set(Calendar.HOUR_OF_DAY, mapsFragment.bookHour)
            mapsFragment.bookCalendar?.set(Calendar.MINUTE, mapsFragment.bookHour)

            val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
            val bookString = df.format(mapsFragment.bookCalendar!!.time)


            pilotSwitch.visibility = if(book[position].vehicle == "car") {
                View.VISIBLE
            } else {
                View.GONE
            }

            detailTextView.text = "Prezzo: ${book[position].price_km} €"




            Log.e("BOOK_START", mapsFragment.bookMinute.toString() + " " + mapsFragment.bookHour.toString() + " " + mapsFragment.bookCalendar?.time.toString())

            prenotaButton.setOnClickListener {

                if(mapsFragment.userData?.drivingLicence.isNullOrEmpty() && (book[position]!!.vehicle == "car")  && !pilotSwitch.isChecked){
                    Toast.makeText(view.context, "Devi caricare la patente prima di utilizzare questi mezzi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

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
                            if(s.length > 3) {
                                number.setText(s[0].toString() + s[1].toString() + s[2].toString())
                                Toast.makeText(view.context, "Devi inserire un valore che va da 0 a 1440(Un Giorno)", Toast.LENGTH_SHORT).show()
                            } else if(s.isNotEmpty()){
                                if(s.toString().toInt() > 1440 || s.toString().toInt() < 0) {
                                    number.setText(s[0].toString())
                                    Toast.makeText(view.context, "Devi inserire un valore che va da 0 a 1440(Un giorno)", Toast.LENGTH_SHORT).show()
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

                        val start_date = bookString

                        val minutes = number.text.toString()

                        val veichleId = book[position].id

                        val with_driver = pilotSwitch.isChecked

                        val ride_destination = destinazioneText.text.toString()

                        val positionIndex = mapsFragment.storeIndexClicked

                        val store = if(positionIndex == -1) {
                            null
                        } else {
                            mapsFragment.stores?.get(positionIndex)!!.id
                        }

                        val start_location = mapsFragment.indirizzoString

                        val isExpress = positionIndex == -1


                        Networking.create().confirmBook(
                            body = Networking.ConfirmBook(start_date, minutes.toInt(), veichleId!!, with_driver, ride_destination,
                                store.toString(),start_location, isExpress), mapsFragment.token
                        ).enqueue(object: retrofit2.Callback<String>{
                            override fun onResponse(
                                call: Call<String>,
                                response: Response<String>
                            ) {
                                Log.e("confirmBook", response.isSuccessful.toString())
                                mapsFragment.getCorseAttive()

                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Log.e("confirmBook", t.localizedMessage)
                            }

                        })

                        mapsFragment.timeToBook = number.text.toString().toInt()
                        mapsFragment.destBook = prenotaButton.text.toString()
                        mapsFragment.getCorseAttive()
                        bookDialog.dismiss()
                        mapsFragment.requireActivity().onBackPressed()
                        Toast.makeText(view.context, "Prenotazione compleatata", Toast.LENGTH_SHORT).show()
                        mapsFragment.getCorseAttive()
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