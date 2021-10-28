package com.example.palermo2go.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import android.text.Editable
import android.text.TextWatcher
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
import com.example.palermo2go.Networking
import com.example.palermo2go.R
import com.example.palermo2go.adapters.CartAdapter
import com.example.palermo2go.model.Road
import com.example.palermo2go.model.RoadModel
import com.example.palermo2go.models.Cart
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
import com.google.android.material.textfield.TextInputEditText
import com.stdout.greenurb.adapters.HistoryAdapters
import com.stdout.greenurb.adapters.InCorsoAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class MapsFragment : Fragment(), OnMapReadyCallback {

    var indexInRoad: Int = 0
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
    lateinit var finishCorsa: Button
    var cart: Cart? = null
    var arrayBook = ArrayList<Road?>()
    lateinit var sharedPreferences: SharedPreferences
    lateinit var inCorsoAdapter: InCorsoAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences = requireActivity().getSharedPreferences("com.example.palermo2go", Context.MODE_PRIVATE)
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
        getCorseAttive()
        requestPermission()
        setLocation()
//        registerFirebaseToken()
        return rootView
    }

    private fun getCorseAttive() {

        val finalToken = "Bearer " + sharedPreferences.getString("token", "")
        Log.e("Bearer", finalToken)
        if(finalToken == "Bearer "){
            logout()
        }
        Networking.create().getActive(finalToken).enqueue(object: Callback<RoadModel>{
            override fun onResponse(call: Call<RoadModel>, response: Response<RoadModel>) {
                if(response.isSuccessful){
                    if (response.body() != null){
                        arrayBook = response.body()!!.data
                    }

                } else {
                    Log.e("response", "FAILURE")
                }

            }

            override fun onFailure(call: Call<RoadModel>, t: Throwable) {

            }

        })
    }


//    private fun registerFirebaseToken() {
//        FirebaseApp.initializeApp(rootView.context)
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w("TOKEN", "Fetching FCM registration token failed", task.exception)
//                return@OnCompleteListener
//            }
//
//            // Get new FCM registration token
//            val token = task.result
//
//            Toast.makeText(rootView.context, token.toString(), Toast.LENGTH_SHORT).show()
//        })
//    }


    fun moveMapBySearch(lat: Double, lon: Double, isExpress: Boolean) {
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
        searchMarker = mMap.addMarker(
            MarkerOptions().position(position).title("position")
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
        )
    }

    private fun findView() {
        finishCorsa = rootView.findViewById(R.id.finishCorsa)
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
        Log.e("FRAGMENT", "PUSH")
        requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in,
            R.anim.slide_in,
            R.anim.slide_out,
            R.anim.slide_out
        ).addToBackStack(null).add(R.id.frameContainer, fragment).commit()
    }


    private fun onClick() {


        bookNowButton.setOnClickListener {
            if (cart == null) {
                Toast.makeText(
                    rootView.context,
                    "Registra la carta prima di fare una prenotazione",
                    Toast.LENGTH_SHORT
                ).show()
                drawer_layout.openDrawer(GravityCompat.START)
            } else if (cartConteiner.visibility == View.VISIBLE) {
                Toast.makeText(rootView.context, "La pagina è già aperta", Toast.LENGTH_SHORT)
                    .show()
            } else {
                cartConteiner.visibility = View.VISIBLE
                startFragment(BookNowFragment(this))
            }

        }
        openDrawerButton.setOnClickListener {
            drawer_layout.openDrawer(GravityCompat.START);
        }

        profileImage.setOnClickListener {
            drawer_layout.closeDrawer(GravityCompat.START)
            startFragmentMain(ProfileFragment())
        }

        menulaterale.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { menuItem ->
            val id = menuItem.itemId

            when (id) {
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
                    Handler().postDelayed({
                        showPaymentmodal()
                    }, 100)
                }

                R.id.cart -> {
                   loadCart()
                }

                R.id.logout -> {
                    logout()
                }

            }
            drawer_layout.closeDrawer(GravityCompat.START)
            true
        })

       if(sharedPreferences.getBoolean("inRoad", false)) {
           Toast.makeText(rootView.context, "Status recuperato, sei in una corsa", Toast.LENGTH_SHORT).show()
           goOnRoad()
       }

    }

    private fun logout() {
        sharedPreferences.edit().putBoolean("isLogged", false).apply()
        sharedPreferences.edit().putString("token", "").apply()
        val intent = Intent(rootView.context, MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun loadCart() {
        val registerCart = Dialog(rootView.context, R.style.Theme_Palermo2Go)
        registerCart.setContentView(R.layout.carrello_dialog)
        registerCart.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.lightTrasparent)))
        val cartRecycler = registerCart.findViewById<RecyclerView>(R.id.cartRecycler)
        val pagaButton = registerCart.findViewById<Button>(R.id.pagaButton)
        val cartadpater = CartAdapter(arrayBook, this)
        cartRecycler.layoutManager = LinearLayoutManager(rootView.context, LinearLayoutManager.VERTICAL, false)
        cartRecycler.adapter = cartadpater

        pagaButton.setOnClickListener {
            paga()
            registerCart.dismiss()
        }

        registerCart.show()
    }

    private fun paga() {
        //
    }

    private fun showPaymentmodal() {
        if (cart == null) {
            registerCart()
        } else {
            detailCard()
        }
    }

    private fun detailCard() {
        val registerCart = Dialog(rootView.context, R.style.Theme_Palermo2Go)
        registerCart.setContentView(R.layout.view_cart_dialog)
        registerCart.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.lightTrasparent)))
        val cartNumber = registerCart.findViewById<TextView>(R.id.cardView)
        val cartExpire = registerCart.findViewById<TextView>(R.id.date)
        val cvv = registerCart.findViewById<TextView>(R.id.cvv)
        val eliminaCarta = registerCart.findViewById<CardView>(R.id.eliminaCarta)
        val exitButton = registerCart.findViewById<CardView>(R.id.exitButton)
        cartNumber.text = cart?.getNumberCensured()
        cartExpire.text = cart?.expire
        cvv.text = cart?.cvv

        eliminaCarta.setOnClickListener {
            cart = null
            registerCart.dismiss()
        }

        exitButton.setOnClickListener {
            registerCart.dismiss()
        }
        registerCart.show()
    }

    private fun registerCart() {
        val registerCart = Dialog(rootView.context, R.style.Theme_Palermo2Go)
        registerCart.setContentView(R.layout.register_cart_dialog)
        registerCart.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.lightTrasparent)))
        val cartNumber = registerCart.findViewById<TextInputEditText>(R.id.cartNumber)
        val cartExpire = registerCart.findViewById<TextInputEditText>(R.id.cartExpire)
        val cvv = registerCart.findViewById<TextInputEditText>(R.id.cvv)
        val registerButton = registerCart.findViewById<CardView>(R.id.registerButton)

        var cartBool = false
        var cartExpireBool = false
        var cvvBool = false
        var lastPositiosTree = false


        cvv.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    if (s.length > 3) {
                        Toast.makeText(rootView.context, "Non puoi inserire piu di 3 caratteri", Toast.LENGTH_SHORT).show()
                        cvv.setText(s[0].toString() + s[1].toString() + s[2].toString())
                        cvv.setSelection(3)
                        cvvBool = true
                    } else cvvBool = s.length == 3
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //
            }

        })
        cartExpire.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (!s.isNullOrEmpty()) {
                    if (s.length == 3) {
                        lastPositiosTree = true
                    }
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    if (s.length == 2) {
                        if (lastPositiosTree) {
                            lastPositiosTree = false
                            cartExpire.setText(s[0].toString())
                            cartExpire.setSelection(1)
                        } else {
                            cartExpire.setText(cartExpire.text.toString() + "/")
                            cartExpire.setSelection(3)
                        }
                    } else if (s.length > 5) {

                        cartExpire.setText(s[0].toString() + s[1].toString() + s[2].toString() + s[3].toString() + s[4].toString())
                        cartExpire.setSelection(5)
                        cartExpireBool = true
                    } else cartExpireBool = s.length == 5
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //
            }

        })


        cartNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    if (s.length > 16) {
                        var newString = ""
                        for (value in 0..15) {
                            Log.e("FORVALUE", value.toString())
                            newString += s[value]
                        }
                        Toast.makeText(rootView.context, "Non puoi inserire piu di 16 caratteri", Toast.LENGTH_SHORT).show()
                        cartNumber.setText(newString)
                        cartBool = true
                    } else cartBool = s.length == 16
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //
            }

        })

        registerButton.setOnClickListener {
            when {
                !cartBool -> {

                    Toast.makeText(
                        rootView.context,
                        "Inserisci una carta valida (16 caratteri) ",
                        Toast.LENGTH_SHORT
                    ).show()

                }
                !cartExpireBool -> {
                    Toast.makeText(
                        rootView.context,
                        "Inserisci una scadenza valida ",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                !cvvBool -> {
                    Toast.makeText(
                        rootView.context,
                        "Inserisci il codice cvv valido (3 caratteri)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    registerCart.dismiss()
                    cart = Cart(cartNumber.text.toString(), cartExpire.text.toString(), cvv.text.toString())
                    Toast.makeText(
                        rootView.context,
                        "Carta registrata con successo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        registerCart.show()
    }


    private fun openHistoryModal() {
        val historyModal = Dialog(rootView.context, R.style.Theme_Palermo2Go)
        historyModal.setContentView(R.layout.modal_in_corso)
        historyModal.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.lightTrasparent)))
        val inCorsoRecyclerView = historyModal.findViewById<RecyclerView>(R.id.inCorsoRecyclerView)
        val nonInCorso = historyModal.findViewById<TextView>(R.id.nonInCorso)
        val title = historyModal.findViewById<TextView>(R.id.title)
        title.text = getString(R.string.prenotazioni_passate)
        val arrayBook = ArrayList<Road>()
        historyModal.setCanceledOnTouchOutside(true)
        historyModal.window!!.setWindowAnimations(R.style.DialogNoAnimation)
        //MOKCK

        val historyAdapters = HistoryAdapters(arrayBook)
        inCorsoRecyclerView.layoutManager =
            LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, false)
        inCorsoRecyclerView.adapter = historyAdapters
        historyModal.show()
    }

    private fun openInCorsoModal() {
        val incorsoModal = Dialog(rootView.context, R.style.Theme_Palermo2Go)
        incorsoModal.setContentView(R.layout.modal_in_corso)
        incorsoModal.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.lightTrasparent)))
        val inCorsoRecyclerView = incorsoModal.findViewById<RecyclerView>(R.id.inCorsoRecyclerView)
        val nonInCorso = incorsoModal.findViewById<TextView>(R.id.nonInCorso)

        incorsoModal.setCanceledOnTouchOutside(true)
        incorsoModal.window!!.setWindowAnimations(R.style.DialogNoAnimation)
        //MOKCK

        inCorsoAdapter = InCorsoAdapter(arrayBook, this, incorsoModal, nonInCorso)
        inCorsoRecyclerView.layoutManager =
            LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, false)
        inCorsoRecyclerView.adapter = inCorsoAdapter
        incorsoModal.show()

    }


    fun goOnRoad() {
        sharedPreferences.edit().putBoolean("inRoad", true).apply()
        finishCorsa.visibility = View.VISIBLE
        bookNowButton.visibility = View.GONE
        menulaterale.visibility = View.GONE
        openDrawerButton.visibility = View.GONE
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        finishCorsa.setOnClickListener {
            parkVeichle()
        }
    }

    private fun parkVeichle() {
        finishCorsa.visibility = View.GONE
        bookNowButton.visibility = View.VISIBLE
        menulaterale.visibility = View.VISIBLE
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        openDrawerButton.visibility = View.VISIBLE
        sharedPreferences.edit().putBoolean("inRoad", false).apply()
        deleteRoad(indexInRoad)
    }

    fun deleteRoad(position: Int) {
        arrayBook.removeAt(position)
        inCorsoAdapter.notifyItemRemoved(position)
        indexInRoad = 0
        Handler().postDelayed({
            inCorsoAdapter.notifyDataSetChanged()
        }, 300)
    }

    private fun setLocation() {
        locationManager =
            requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
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
                    Log.e(
                        "LocationManager",
                        location.latitude.toString() + " " + location.longitude
                    )

                    val position = LatLng(
                        latitude,
                        longitude
                    )
                    val icon = requireActivity().getDrawable(R.drawable.current_pos)
                        ?.let { drawableToBitmap(it) }
                    Handler().postDelayed({
                        if (!firstOpen) marker.remove()
                        marker = mMap.addMarker(
                            MarkerOptions().position(position).title("position")
                                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                        )
                        if (firstOpen) {
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

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                1f,
                locationListener
            );

        }


    }


    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUESTCODE
        )
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
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
        }, 2000)
    }

    fun startFragmentMain(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in,
            R.anim.slide_in,
            R.anim.slide_out,
            R.anim.slide_out
        ).addToBackStack(null).add(R.id.drawer_layout, fragment).commit()
    }

}