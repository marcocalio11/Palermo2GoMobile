package com.stdout.greenurb.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.palermo2go.Helper
import com.example.palermo2go.R
import com.example.palermo2go.model.Road
import java.util.*
import kotlin.collections.ArrayList


class HistoryAdapters(private val book: ArrayList<Road?>, val nonInCorso: TextView) :
    RecyclerView.Adapter<HistoryAdapters.ViewHolder>() {
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val image = view.findViewById<ImageView>(R.id.image)
        val start = view.findViewById<TextView>(R.id.start)
        val arrive = view.findViewById<TextView>(R.id.arrive)
        val date = view.findViewById<TextView>(R.id.date)



        fun binding(position: Int, book: ArrayList<Road?>) {
            val newPoition = book.size - 1 -position
            image.setImageDrawable(view.resources.getDrawable(Helper().getImageByString(book[newPoition]!!.veichle!!.vehicle!!), null))

            start.text = start.text.toString() + " ${book[newPoition]?.start_location}"
            arrive.text = arrive.text.toString() + " ${book[newPoition]?.ride_destination}"
            date.text = date.text.toString() + " ${book[newPoition]?.end_ride_date}"

        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.book_history, viewGroup, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.binding(position, book)
    }


    override fun getItemCount(): Int {
        if(book.size == 0){
            nonInCorso.text = "Non hai effettuato nessuna prenotazione"
            nonInCorso.visibility = View.VISIBLE
        } else {
            nonInCorso.visibility = View.GONE
        }
        return book.size
    }

}