package com.example.palermo2go.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.palermo2go.R
import com.example.palermo2go.models.BookModel
import com.stdout.greenurb.adapters.BookNowAdapter
import java.util.*
import kotlin.collections.ArrayList


class BookFragment(val mapsFragment: MapsFragment) : Fragment() {

    lateinit var v: View
    lateinit var resultTextView: TextView
    lateinit var bookRecycler: RecyclerView
    lateinit var backButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_book, container, false)
        findView()
        return v
    }

    private fun findView() {
        backButton = v.findViewById(R.id.backButton)
        bookRecycler = v.findViewById(R.id.bookRecycler)
        resultTextView = v.findViewById(R.id.resultTextView)
        onClick()
        setValue()
        buildRecycler()
    }

    private fun onClick() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun buildRecycler() {
        val array = ArrayList<BookModel>()
        array.add(BookModel())
        array.add(BookModel())
        array.add(BookModel())
        array.add(BookModel())
        val adapter = BookNowAdapter(mapsFragment.veichleArray, mapsFragment)
        bookRecycler.layoutManager = LinearLayoutManager(v.context, LinearLayoutManager.VERTICAL, false)
        bookRecycler.adapter = adapter

    }

    private fun setValue() {
       val month = mapsFragment.bookCalendar?.get(Calendar.MONTH)?.plus(1)
        resultTextView.text = mapsFragment.bookCalendar?.get(Calendar.DAY_OF_MONTH).toString() + "/" +   month + "/" +  mapsFragment.bookCalendar?.get(Calendar.YEAR).toString()

    }

}