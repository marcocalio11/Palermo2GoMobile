package com.example.palermo2go.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.palermo2go.R
import com.stdout.greenurb.fragments.MapsFragment

class BookNowFragment(val mapsFragment: MapsFragment) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_now, container, false)
    }

    override fun onDestroy() {
        mapsFragment.cartConteiner.visibility = View.GONE
        super.onDestroy()
    }

}