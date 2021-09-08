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
import com.example.palermo2go.models.BookModel
import com.example.palermo2go.R
import com.example.palermo2go.fragments.MapsFragment


class InCorsoAdapter(
    private val book: ArrayList<BookModel>,
    val mapsFragment: MapsFragment,
    val incorsoModal: Dialog,
    val nonInCorso: TextView
) :
    RecyclerView.Adapter<InCorsoAdapter.ViewHolder>() {
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val image = view.findViewById<ImageView>(R.id.image)
        val attiva = view.findViewById<Button>(R.id.attiva)
        val parcheggia = view.findViewById<Button>(R.id.parcheggia)
        val disdici = view.findViewById<Button>(R.id.disdici)

        fun binding(
            position: Int,
            book: java.util.ArrayList<BookModel>,
            mapsFragment: MapsFragment,
            incorsoModal: Dialog
        ) {
            attiva.setOnClickListener {
                incorsoModal.dismiss()
                Toast.makeText(
                    view.context,
                    "Hai sbloccato correttamente il dispositivo",
                    Toast.LENGTH_SHORT
                ).show()
                mapsFragment.goOnRoad()
                mapsFragment.indexInRoad = position
            }

            disdici.setOnClickListener {
                Log.e("POSITION", position.toString())
                mapsFragment.deleteRoad(position)
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
