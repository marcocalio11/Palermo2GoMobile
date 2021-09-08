package com.example.palermo2go.adapters


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
import com.example.palermo2go.models.BookModel
import com.example.palermo2go.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.example.palermo2go.fragments.MapsFragment
import com.stdout.greenurb.adapters.BookNowAdapter
import java.util.*
import kotlin.collections.ArrayList


class CartAdapter(private val book: ArrayList<BookModel>, val mapsFragment: MapsFragment) :
    RecyclerView.Adapter<CartAdapter.ViewHolder>() {
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val prezzo = view.findViewById<TextView>(R.id.prezzo)
        val image = view.findViewById<ImageView>(R.id.imageView)

        fun binding(position: Int, book: java.util.ArrayList<BookModel>, mapsFragment: MapsFragment) {



        }
    }






    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.cart_list, viewGroup, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.binding(position, book, mapsFragment)
    }


    override fun getItemCount() = book.size

}