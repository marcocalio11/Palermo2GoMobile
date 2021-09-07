package com.stdout.greenurb.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.palermo2go.MainActivity
import com.example.palermo2go.models.BookModel
import com.example.palermo2go.R
import com.example.palermo2go.fragments.BookFragment
import com.example.palermo2go.fragments.BookNowFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.stdout.greenurb.adapters.HistoryAdapters
import com.stdout.greenurb.adapters.InCorsoAdapter
import java.util.*
import kotlin.collections.ArrayList


class MapsFragment : Fragment(), OnMapReadyCallback {

    var destBook: String = ""
    var destinationLon: Double = 0.0
    var destinationLat: Double = 0.0
    var timeToBook: Int = 0
    var bookHour: Int = 0
    var bookMinute: Int = 0
    var isToday: Boolean = false
    var bookCalendar: Calendar? = null
    private lateinit var mMap: GoogleMap
    lateinit var rootView: View
    lateinit var marker: Marker
    var searchMarker: Marker? = null
    lateinit var locateMeButton: FloatingActionButton
    lateinit var openDrawerButton: FloatingActionButton
    private val REQUESTCODE = 128
    lateinit var locationManager: LocationManager
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var latitudeSearch: Double = 0.0
    var longitudeSearch: Double = 0.0
    var firstOpen = true
    lateinit var bookNowButton: Button
    lateinit var menulaterale: NavigationView
    lateinit var drawer_layout: DrawerLayout
    lateinit var headerView: View
    var isExpress = false
    lateinit var profileImage: ImageView
    lateinit var nameSurnameTextView: TextView
    lateinit var cartConteiner: CardView
    lateinit var progressBar: ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.activity_maps, container, false)
        Handler().postDelayed({
            val mapFragment = childFragmentManager.fragments[0] as SupportMapFragment?
            locateMeButton = rootView.findViewById(R.id.locateMeButton)
            mapFragment!!.getMapAsync(this)

            locateMeButton.setOnClickListener {
                animate()
            }

        }, 250)
        findView()
        checkIfGpsEnabled()
        requestPermission()
        setLocation()
        return rootView
    }

    fun moveMapBySearch(lat: Double, lon: Double, isExpress: Boolean){
        val normalLat = lat + 0.0017
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    normalLat,
                    lon
                ), 16f
            )
        )

        this.latitudeSearch = lat
        this.longitudeSearch = lon
        this.isExpress = isExpress

        val icon = requireActivity().getDrawable(R.drawable.pin)?.let { drawableToBitmap(it) }
        val position = LatLng(
            lat,
            lon
        )
        searchMarker?.remove()
        searchMarker = mMap.addMarker(MarkerOptions().position(position).title("position").icon(BitmapDescriptorFactory.fromBitmap(icon)))
    }

    private fun findView() {
        progressBar = rootView.findViewById(R.id.progressBar)
        cartConteiner = rootView.findViewById(R.id.cartConteiner)
        bookNowButton = rootView.findViewById(R.id.bookNowButton)
        menulaterale = rootView.findViewById(R.id.menulaterale)
        openDrawerButton = rootView.findViewById(R.id.openDrawerButton)
        drawer_layout = rootView.findViewById(R.id.drawer_layout)
        headerView = menulaterale.getHeaderView(0)
        profileImage = headerView.findViewById(R.id.imageView)
        nameSurnameTextView = headerView.findViewById(R.id.nomeCognomeTextview)
        onClick()

    }

    fun startFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in,
            R.anim.slide_in,
            R.anim.slide_out,
            R.anim.slide_out
        ).addToBackStack(null).add(R.id.frameContainer, fragment).commit()
    }



    private fun onClick() {


        bookNowButton.setOnClickListener {

            if(cartConteiner.visibility == View.VISIBLE) {
                Toast.makeText(rootView.context, "La pagina è già aperta", Toast.LENGTH_SHORT).show()
            } else {
                cartConteiner.visibility = View.VISIBLE
                startFragment(BookNowFragment(this))
            }

        }
        openDrawerButton.setOnClickListener {
            drawer_layout.openDrawer( GravityCompat.START);
        }

        profileImage.setOnClickListener {
            Toast.makeText(rootView.context,"Profilo", Toast.LENGTH_SHORT).show()
        }

        menulaterale.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { menuItem ->
            val id = menuItem.itemId

            when(id){
               R.id.bookInCorso -> {
                   Handler().postDelayed({
                       openInCorsoModal()
                   }, 100)
                }

               R.id.lastBook -> {
                   Handler().postDelayed({
                       openHistoryModal()
                   }, 100)
                }

                R.id.payment -> {
                    Toast.makeText(rootView.context,"Payment", Toast.LENGTH_SHORT).show()
                }

                R.id.cart -> {
                    Toast.makeText(rootView.context,"cart", Toast.LENGTH_SHORT).show()
                }

            }
            drawer_layout.closeDrawer(GravityCompat.START)
            true
        })
    }

    private fun openHistoryModal() {
        val historyModal = Dialog(rootView.context, R.style.Theme_Palermo2Go)
        historyModal.setContentView(R.layout.modal_in_corso)
        historyModal.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.lightTrasparent)))
        val inCorsoRecyclerView = historyModal.findViewById<RecyclerView>(R.id.inCorsoRecyclerView)
        val nonInCorso = historyModal.findViewById<TextView>(R.id.nonInCorso)
        val title = historyModal.findViewById<TextView>(R.id.title)
        title.text = getString(R.string.prenotazioni_passate)
        val arrayBook = ArrayList<BookModel>()
        historyModal.setCanceledOnTouchOutside(true)
        historyModal.window!!.setWindowAnimations(R.style.DialogNoAnimation)
        //MOKCK
        arrayBook.add(BookModel())
        arrayBook.add(BookModel())
        arrayBook.add(BookModel())
        arrayBook.add(BookModel())
        arrayBook.add(BookModel())
        val historyAdapters = HistoryAdapters(arrayBook)
        inCorsoRecyclerView.layoutManager = LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, false)
        inCorsoRecyclerView.adapter = historyAdapters
        historyModal.show()
    }

    private fun openInCorsoModal() {
        val incorsoModal = Dialog(rootView.context, R.style.Theme_Palermo2Go)
        incorsoModal.setContentView(R.layout.modal_in_corso)
        incorsoModal.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.lightTrasparent)))
        val inCorsoRecyclerView = incorsoModal.findViewById<RecyclerView>(R.id.inCorsoRecyclerView)
        val nonInCorso = incorsoModal.findViewById<TextView>(R.id.nonInCorso)
        val arrayBook = ArrayList<BookModel>()
        incorsoModal.setCanceledOnTouchOutside(true)
        incorsoModal.window!!.setWindowAnimations(R.style.DialogNoAnimation)
        //MOKCK
        arrayBook.add(BookModel())
        arrayBook.add(BookModel())
        arrayBook.add(BookModel())
        arrayBook.add(BookModel())
        arrayBook.add(BookModel())
        val inCorsoAdapter = InCorsoAdapter(arrayBook)
        inCorsoRecyclerView.layoutManager = LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, false)
        inCorsoRecyclerView.adapter = inCorsoAdapter
        incorsoModal.show()

    }


    private fun setLocation() {
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                rootView.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                rootView.context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
            return
        } else {
            val locationListener = (object : LocationListener {
                @SuppressLint("UseCompatLoadingForDrawables")
                override fun onLocationChanged(location: Location) {
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.e("LocationManager", location.latitude.toString() + " " + location.longitude)

                    val position = LatLng(
                        latitude,
                        longitude
                    )
                    val icon = requireActivity().getDrawable(R.drawable.current_pos)?.let { drawableToBitmap(it) }
                    Handler().postDelayed({
                        if(!firstOpen) marker.remove()
                        marker = mMap.addMarker(MarkerOptions().position(position).title("position").icon(BitmapDescriptorFactory.fromBitmap(icon)))
                        if(firstOpen) {
                            firstOpen = !firstOpen
                            animate()
                        }
                    }, 100)

                }

                override fun onProviderDisabled(provider: String) {
                    Log.e("LocationManager", "DISABLE $provider")


                }

                override fun onProviderEnabled(provider: String) {
                    Log.e("LocationManager", "ENABLE $provider")
                }
            })

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1f, locationListener);

        }



    }


    private fun requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUESTCODE)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = googleMap
        }
    }

    private fun animate() {
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    latitude,
                    longitude
                ), 16f
            )
        )
    }


    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


    fun checkIfGpsEnabled() {
        var manager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ( !manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            buildAlertMessageNoGps()
        }
        return
    }

    fun buildAlertMessageNoGps() {
        var builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage("Il tuo gps è disabilitato, vuoi attivarlo?")
            .setCancelable(false)
            .setPositiveButton("Si") { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton("No") { dialog, id -> dialog.cancel() }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    fun callBook() {
        progressBar.visibility = View.VISIBLE
        Handler().postDelayed({
            progressBar.visibility = View.GONE
            startFragmentMain(BookFragment(this))
        },2000)
    }

    fun startFragmentMain(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in,
            R.anim.slide_in,
            R.anim.slide_out,
            R.anim.slide_out
        ).addToBackStack(null).add(R.id.drawer_layout, fragment).commit()
    }

}