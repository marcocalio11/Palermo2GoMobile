package com.stdout.greenurb.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.palermo2go.R
import com.example.palermo2go.model.Road


class HistoryAdapters(private val book: ArrayList<Road>) :
    RecyclerView.Adapter<HistoryAdapters.ViewHolder>() {
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val image = view.findViewById<ImageView>(R.id.image)

        val parcheggia = view.findViewById<Button>(R.id.history)


        fun binding(position: Int, book: ArrayList<Road>) {

        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.book_history, viewGroup, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.binding(position, book)
    }


    override fun getItemCount() = book.size

}