package com.example.palermo2go.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.palermo2go.Helper
import com.example.palermo2go.R
import com.example.palermo2go.fragments.MapsFragment
import com.example.palermo2go.model.Road
import com.example.palermo2go.model.RoadModel
import com.google.rpc.Help


class CartAdapter(private val book: RoadModel?, val mapsFragment: MapsFragment) :
    RecyclerView.Adapter<CartAdapter.ViewHolder>() {
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val prezzo = view.findViewById<TextView>(R.id.prezzo)
        val image = view.findViewById<ImageView>(R.id.imageView)

        fun binding(position: Int, book: ArrayList<Road?>, mapsFragment: MapsFragment) {

            prezzo.text = book[position]!!.price.toString() + "€"
            image.setImageDrawable(view.resources.getDrawable(Helper().getImageByString(book[position]!!.veichle!!.vehicle!!), null))

        }
    }






    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.cart_list, viewGroup, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.binding(position, book!!.data, mapsFragment)
    }


    override fun getItemCount() = book!!.data.size

}