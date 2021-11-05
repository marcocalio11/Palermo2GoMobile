package com.stdout.greenurb.adapters


import android.app.Dialog
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
import com.example.palermo2go.R
import com.example.palermo2go.fragments.MapsFragment
import com.example.palermo2go.model.Road
import com.google.gson.Gson
import kotlin.collections.ArrayList


class InCorsoAdapter(
    private val book: ArrayList<Road?>,
    val mapsFragment: MapsFragment,
    val incorsoModal: Dialog,
    val nonInCorso: TextView
) :
    RecyclerView.Adapter<InCorsoAdapter.ViewHolder>() {
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val image = view.findViewById<ImageView>(R.id.image)
        val attiva = view.findViewById<Button>(R.id.attiva)
        val disdici = view.findViewById<Button>(R.id.disdici)
        val partenza = view.findViewById<TextView>(R.id.partenza)
        val consegna = view.findViewById<TextView>(R.id.arrivo)
        val changeData = view.findViewById<Button>(R.id.changeData)

        fun binding(
            position: Int,
            book: ArrayList<Road?>,
            mapsFragment: MapsFragment,
            incorsoModal: Dialog
        ) {

            partenza.text = view.resources.getText(R.string.partenza_da).toString() + "${book[position]?.start_location}"
            consegna.text = consegna.text.toString() + " ${book[position]?.ride_destination}"

            Log.e("VEICOLO", Gson().toJson(book[position]?.veichle))

            image.setImageDrawable(view.resources.getDrawable(Helper().getImageByString(book[position]!!.veichle!!.vehicle!!), null))

            changeData.setOnClickListener {
                incorsoModal.dismiss()
                mapsFragment.changeDateAndPosition(book[position])
            }

            attiva.setOnClickListener {
                incorsoModal.dismiss()
                Toast.makeText(
                    view.context,
                    "Hai sbloccato correttamente il dispositivo",
                    Toast.LENGTH_SHORT
                ).show()
                mapsFragment.startRide(book[position])
                mapsFragment.indexInRoad = position
            }

            disdici.setOnClickListener {
                Log.e("POSITION", position.toString())
                mapsFragment.deleteRoad(book[position])
            }
        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.book_in_corso_list, viewGroup, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.binding(position, book, mapsFragment, incorsoModal)
    }


    override fun getItemCount(): Int {
        if (book.size > 0) {
            nonInCorso.visibility = View.GONE
            return book.size
        } else {
            nonInCorso.visibility = View.VISIBLE
            return 0
        }
    }
}
