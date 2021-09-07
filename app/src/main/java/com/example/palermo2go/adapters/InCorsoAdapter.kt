package com.stdout.greenurb.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.palermo2go.models.BookModel
import com.example.palermo2go.R


class InCorsoAdapter(private val book: ArrayList<BookModel>) :
    RecyclerView.Adapter<InCorsoAdapter.ViewHolder>() {
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val image = view.findViewById<ImageView>(R.id.image)
        val attiva = view.findViewById<Button>(R.id.attiva)
        val parcheggia = view.findViewById<Button>(R.id.parcheggia)
        val disdici = view.findViewById<Button>(R.id.disdici)

        fun binding(position: Int, book: java.util.ArrayList<BookModel>) {

        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.book_in_corso_list, viewGroup, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.binding(position, book)
    }


    override fun getItemCount() = book.size

}