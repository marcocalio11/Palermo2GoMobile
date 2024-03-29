package com.example.palermo2go.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.palermo2go.MainActivity
import com.example.palermo2go.Networking
import com.example.palermo2go.R
import com.example.palermo2go.model.Road
import com.example.palermo2go.model.RoadModel
import com.example.palermo2go.model.Veichle
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
import com.google.gson.Gson
import com.stdout.greenurb.adapters.HistoryAdapters
import com.stdout.greenurb.adapters.InCorsoAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import android.content.IntentFilter
import android.location.*
import com.squareup.picasso.Picasso


class MapsFragment(val mainActivity: MainActivity) : Fragment(), OnMapReadyCallback {

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
    var markerList = ArrayList<Marker>()
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
    lateinit var changeButtonNowButton: Button
    var cart: Cart? = null
    var arrayBook = ArrayList<Road?>()
    var historyBook = ArrayList<Road?>()
    lateinit var sharedPreferences: SharedPreferences
    lateinit var inCorsoAdapter: InCorsoAdapter
    var bookNowFragment: BookNowFragment? = null
    var token = ""
    var stores: ArrayList<Networking.Stores>? = null
    var storeIndexClicked = -1
    var veichleArray = ArrayList<Veichle>()
    var indirizzoString = ""
    lateinit var incorsoModal: Dialog
    lateinit var consegna: TextView
    var userData: Networking.UserData? = null
    var registerCart: Dialog? = null
    var timeToPay = false


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
                checkPay()
                if(!timeToPay)
                animate(16f)
            }

        }, 250)
        token = "Bearer " + sharedPreferences.getString("token", "")

        val cartNumber =   sharedPreferences.getString("cartNumber", null)
        val cartExpire =   sharedPreferences.getString("cartExpire", null)
        val cvv =   sharedPreferences.getString("cvv", null)

        if(cartNumber != null && cartExpire != null && cvv != null){
            cart = Cart(cartNumber, cartExpire, cvv)
        }

        findView()
        getLoggedUser()
        checkIfGpsEnabled()
        getCorseAttive()
        requestPermission()
        setLocation()

//        registerFirebaseToken()
        return rootView
    }


    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(rootView.context).registerReceiver(
            mMessageReceiver,
            IntentFilter("MyData")
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(rootView.context).unregisterReceiver(mMessageReceiver)
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.extras?.getString("corsa")
            Log.e("NOTIFICA", "$action ")
        }
    }



    private fun getLoggedUser() {
        checkToken()
        Networking.create().getLoggedUser(token).enqueue(object: Callback<Networking.ResponseLoggedUser>{
            override fun onResponse(call: Call<Networking.ResponseLoggedUser>, response: Response<Networking.ResponseLoggedUser>) {
                if(response.isSuccessful){
                    userData = response.body()?.data
                    nameSurnameTextView.text = userData?.firstName.toString() + " " + userData?.lastName.toString()

                    if(userData?.propic.isNullOrBlank()) {
                        profileImage.setImageDrawable(rootView.resources.getDrawable(R.drawable.account, null))
                    } else {
                        Picasso
                            .get()
                            .load(userData!!.propic)
                            .into(profileImage)
                    }
                } else {
                    logout()
                }
            }

            override fun onFailure(call: Call<Networking.ResponseLoggedUser>, t: Throwable) {

            }

        })
    }

    private fun checkToken() {
        if(token.isNullOrEmpty()) {
            logout()
        }
    }

    fun getCorseAttive() {
        checkToken()

        getHistoryCorse()

        Networking.create().getActive(token).enqueue(object: Callback<RoadModel>{
            override fun onResponse(call: Call<RoadModel>, response: Response<RoadModel>) {
                if(response.isSuccessful){
                    if (response.body() != null){
                        Log.e("getActive", Gson().toJson(response.body()))
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

    private fun getHistoryCorse() {
        Networking.create().getHistory(token).enqueue(object: Callback<RoadModel> {
            override fun onResponse(call: Call<RoadModel>, response: Response<RoadModel>) {
                if(response.isSuccessful){
                    if (response.body() != null){
                        historyBook = response.body()!!.data

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

        if(!isExpress) {
            return
        }

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
            MarkerOptions().position(position).title("Indirizzo Express")
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
        )


    }

    fun getStore() {
        checkToken()

        Log.e("getStores", "INIT")

        Networking.create().getStores(Networking.MyLatLng(latitude, longitude), token = token).enqueue(object : Callback<Networking.ResponseStores> {
            override fun onResponse(call: Call<Networking.ResponseStores>, response: Response<Networking.ResponseStores>) {
                if(response.isSuccessful){
                    Log.e("getStores", Gson().toJson(response.body()))
                    if(response.body()!= null){
                        stores = response.body()!!.data
                        addStorePin(response.body()!!.data!!)
                    }
                    Toast.makeText(rootView.context, "Scegli lo store tra quelli elencati", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(rootView.context, "Errore di rete", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Networking.ResponseStores>, t: Throwable) {
                Log.e("getStores", t.localizedMessage)
                Toast.makeText(rootView.context, "Errore di rete", Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun addStorePin(stores: ArrayList<Networking.Stores>) {
        val icon = requireActivity().getDrawable(R.drawable.pin)?.let { drawableToBitmap(it) }
        removeMark()
        markerList = ArrayList()
        for(store in stores){
            val latLon = store.lat?.let { store.lon?.let { it1 -> LatLng(it, it1) } }
            val marker = mMap.addMarker(MarkerOptions()
                .position(latLon)
                .title(store.address)
                .icon(BitmapDescriptorFactory.fromBitmap(icon)
                ))
            marker.tag = markerList.size
            marker.title = ".sizemarkerList"
            marker.showInfoWindow()
            markerList.add(
                marker
            )
        }

        mMap.setOnMarkerClickListener(object: GoogleMap.OnMarkerClickListener{
            override fun onMarkerClick(p0: Marker): Boolean {
                val position = p0.tag as Int
                Log.e("setOnMarkerListener", position.toString())
                indirizzoString = stores[position].address.toString()
                storeIndexClicked = position
                getVeichle { value ->
                    if(value){


                        removeMark()
                        startFragmentMain(BookFragment(this@MapsFragment))
                    }
                }
                return true
            }

        })

        animate(13f)
    }

    private fun findView() {
        consegna = rootView.findViewById(R.id.consegna)
        finishCorsa = rootView.findViewById(R.id.finishCorsa)
        progressBar = rootView.findViewById(R.id.progressBar)
        cartConteiner = rootView.findViewById(R.id.cartConteiner)
        changeButtonNowButton = rootView.findViewById(R.id.changeButtonNowButton)
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
        ).addToBackStack(null).replace(R.id.frameContainer, fragment).commit()
    }

    fun removeMark() {
        searchMarker?.remove()
        for(value in markerList){
            value.remove()
        }
    }


    private fun onClick() {
        bookNowButton.setOnClickListener {
            checkPay()
            if (!timeToPay) {
                storeIndexClicked = -1
                removeMark()
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
                    bookNowFragment = BookNowFragment(this)
                    startFragment(BookNowFragment(this))
                }

            }
        }
            openDrawerButton.setOnClickListener {
                checkPay()
                Log.e("openDrawerButton", timeToPay.toString())
                if (!timeToPay) {
                    drawer_layout.openDrawer(GravityCompat.START)
                };
            }

            profileImage.setOnClickListener {
                drawer_layout.closeDrawer(GravityCompat.START)
                startFragmentMain(ProfileFragment(userData, token, profileImage, this))
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

                    R.id.logout -> {
                        logout()
                    }

                }
                drawer_layout.closeDrawer(GravityCompat.START)
                true
            })

        if (sharedPreferences.getBoolean("inRoad", false)) {
            Toast.makeText(
                rootView.context,
                "Status recuperato, sei in una corsa",
                Toast.LENGTH_SHORT
            ).show()



            goOnRoad()
        }

        checkPay()

        }


    fun checkPay(){
        timeToPay = sharedPreferences.getBoolean("timeToPay", false)
        if(timeToPay){
            Toast.makeText(rootView.context, "Status recuperato, ti sei dimenticato di pagare", Toast.LENGTH_SHORT).show()
            getCorsaInCorso(false)
        }

    }

    private fun getCorsaInCorso(isRepeater: Boolean) {
        progressBar.visibility = View.VISIBLE
        Networking.create().getCorsaInCorso(token).enqueue(object : Callback<RoadModel>{
            override fun onResponse(call: Call<RoadModel>, response: Response<RoadModel>) {
                progressBar.visibility = View.GONE

                if(response.isSuccessful){
                    openPayModal(response.body())

                } else {
                    Toast.makeText(rootView.context, "Errore nel reperire la corsa attendere...", Toast.LENGTH_SHORT).show()
                    if(!isRepeater) getCorsaInCorso(true) else {
                        Toast.makeText(rootView.context, "Non siamo riusciti a recuperare la corse ti contetteremo a breve", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<RoadModel>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(rootView.context, "Errore nel reperire la corsa attendere...", Toast.LENGTH_SHORT).show()
                if(!isRepeater) getCorsaInCorso(true) else {
                    Toast.makeText(rootView.context, "Non siamo riusciti a recuperare la corse ti contetteremo a breve", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun openPayModal(roadModel: RoadModel?) {
        registerCart = Dialog(rootView.context, R.style.Theme_Palermo2Go)
        registerCart!!.setContentView(R.layout.carrello_dialog)
        registerCart!!.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.lightTrasparent)))
        val prezzo = registerCart!!.findViewById<TextView>(R.id.prezzo)
        val pagaButton = registerCart!!.findViewById<Button>(R.id.pagaButton)
        prezzo.text = roadModel?.data?.first()?.price.toString() + " €"

        pagaButton.setOnClickListener {
            paga(roadModel)
        }

        registerCart!!.show()
    }



    private fun logout() {
        sharedPreferences.edit().putBoolean("isLogged", false).apply()
        sharedPreferences.edit().putString("token", "").apply()
        val intent = Intent(rootView.context, MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    fun openPasswordModal(){
        val historyModal = Dialog(rootView.context, R.style.Theme_Palermo2Go)
        historyModal.setContentView(R.layout.change_password_modal)
        historyModal.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.lightTrasparent)))
        val vecchia = historyModal.findViewById<TextInputEditText>(R.id.textInputEditText)
        val newpasswordtext = historyModal.findViewById<TextInputEditText>(R.id.newpasswordtext)
        val pagaButton = historyModal.findViewById<Button>(R.id.pagaButton)
        historyModal.window!!.setWindowAnimations(R.style.DialogNoAnimation)

        pagaButton.setOnClickListener {
            if(vecchia.text?.isNotEmpty() == false && newpasswordtext.text?.isNotEmpty() == false){
                Toast.makeText(rootView.context, "Inserisci una password", Toast.LENGTH_SHORT).show()
            } else {
                if(vecchia.text == newpasswordtext.text){
                    Toast.makeText(rootView.context, "La vecchia e la nuova password sono uguali", Toast.LENGTH_SHORT).show()
                } else {

                    Networking.create().changePassword(Networking.PasswordBody(vecchia.text.toString(), newpasswordtext.text.toString()), token).enqueue(object : Callback<Gson>{
                        override fun onFailure(call: Call<Gson>, t: Throwable) {
                            Toast.makeText(rootView.context, "Errore di rete la password non è stata cambiata", Toast.LENGTH_SHORT).show()
                        }

                        override fun onResponse(call: Call<Gson>, response: Response<Gson>) {
                            if (response.isSuccessful){
                                Toast.makeText(rootView.context, "Password cambiata con successo", Toast.LENGTH_SHORT).show()
                                historyModal.dismiss()
                            } else {
                                Toast.makeText(rootView.context, "Errore di rete la password non è stata cambiata", Toast.LENGTH_SHORT).show()
                            }
                        }

                    })

                }
            }
        }
        historyModal.show()
    }



    private fun paga(roadModel: RoadModel?) {
        progressBar.visibility = View.VISIBLE
        Networking.create().endRide(roadModel?.data?.first()?.id.toString(), token).enqueue(object: Callback<Gson>{
            override fun onResponse(call: Call<Gson>, response: Response<Gson>) {
                progressBar.visibility = View.GONE
                if(response.isSuccessful){
                    Toast.makeText(rootView.context, "Pagamento effettuato correttamente", Toast.LENGTH_SHORT).show()
                    getCorseAttive()
                    getHistoryCorse()
                    sharedPreferences.edit().remove("timeToPay").apply()
                    registerCart?.dismiss()
                } else {
                    Toast.makeText(rootView.context, "Errore nel pagamento riprova", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Gson>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(rootView.context, "Errore nel pagamento riprova", Toast.LENGTH_SHORT).show()
            }

        })
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
                    sharedPreferences.edit().putString("cartNumber", cartNumber.text.toString()).apply()
                    sharedPreferences.edit().putString("cartExpire", cartExpire.text.toString()).apply()
                    sharedPreferences.edit().putString("cvv", cvv.text.toString()).apply()

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
        historyModal.setCanceledOnTouchOutside(true)
        historyModal.window!!.setWindowAnimations(R.style.DialogNoAnimation)
        //MOKCK

        val historyAdapters = HistoryAdapters(historyBook, nonInCorso)
        inCorsoRecyclerView.layoutManager =
            LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, false)
        inCorsoRecyclerView.adapter = historyAdapters
        historyModal.show()
    }

    private fun openInCorsoModal() {
        incorsoModal = Dialog(rootView.context, R.style.Theme_Palermo2Go)
        incorsoModal.setContentView(R.layout.modal_in_corso)
        incorsoModal.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.lightTrasparent)))
        val inCorsoRecyclerView = incorsoModal.findViewById<RecyclerView>(R.id.inCorsoRecyclerView)
        val nonInCorso = incorsoModal.findViewById<TextView>(R.id.nonInCorso)

        incorsoModal.setCanceledOnTouchOutside(true)
        incorsoModal.window!!.setWindowAnimations(R.style.DialogNoAnimation)
        //MOKCK

        inCorsoAdapter = InCorsoAdapter(arrayBook, this, incorsoModal, nonInCorso)
        inCorsoRecyclerView.layoutManager = LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, false)
        inCorsoRecyclerView.adapter = inCorsoAdapter
        incorsoModal.show()

    }

    fun startRide(road: Road?) {
        checkToken()
        Networking.create().startRide(road!!.id.toString(), token).enqueue(object : Callback<Gson>{
            override fun onResponse(call: Call<Gson>, response: Response<Gson>) {
                if(response.isSuccessful){
                    goOnRoad()

                }else {
                    Toast.makeText(rootView.context, "Errore di rete", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Gson>, t: Throwable) {
                Toast.makeText(rootView.context, "Errore di rete", Toast.LENGTH_SHORT).show()

            }

        })
    }


    fun goOnRoad() {

        sharedPreferences.edit().putBoolean("inRoad", true).apply()
        finishCorsa.visibility = View.VISIBLE
        bookNowButton.visibility = View.GONE
        menulaterale.visibility = View.GONE
        openDrawerButton.visibility = View.GONE
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        removeMark()
        finishCorsa.setOnClickListener {
            parkVeichle()
        }

        getCorsaInCorsoForButtonChange()
    }

    private fun getCorsaInCorsoForButtonChange() {

        Networking.create().getCorsaInCorso(token).enqueue(object : Callback<RoadModel>{
            override fun onResponse(call: Call<RoadModel>, response: Response<RoadModel>) {
                progressBar.visibility = View.GONE
                if(response.isSuccessful){

                    changeButtonNowButton.visibility = View.VISIBLE
                    consegna.visibility = View.VISIBLE
                    consegna.text = consegna.text.toString() + " \n${response.body()?.data?.first()?.end_ride_date}"
                    setPinEndRide(response.body()?.data?.first())
                    changeButtonNowButton.setOnClickListener {
                        changeDateAndPosition(response.body()!!.data.first())
                    }

                } else {

                }
            }

            override fun onFailure(call: Call<RoadModel>, t: Throwable) {

            }

        })
    }

    private fun setPinEndRide(road: Road?) {
        removeMark()
        val geocoder = Geocoder(rootView.context, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(road!!.ride_destination + " Palermo", 1)
        if(addresses.size == 0) return
        val address: Address = addresses[0]
        val icon = requireActivity().getDrawable(R.drawable.elettric_car)?.let { drawableToBitmap(it) }
        val position = LatLng(
            address.latitude,
            address.longitude
        )
        searchMarker?.remove()
        searchMarker = mMap.addMarker(MarkerOptions().position(position).title("Indirizzo consegna").icon(BitmapDescriptorFactory.fromBitmap(icon))
        )
    }

    private fun parkVeichle() {
        finishCorsa.visibility = View.GONE
        consegna.visibility = View.GONE
        changeButtonNowButton.visibility = View.GONE
        bookNowButton.visibility = View.VISIBLE
        menulaterale.visibility = View.VISIBLE
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        openDrawerButton.visibility = View.VISIBLE
        sharedPreferences.edit().putBoolean("inRoad", false).apply()
        sharedPreferences.edit().putBoolean("timeToPay", true).apply()
        getCorsaInCorso(false)
        //deleteRoad(indexInRoad)
    }

    fun deleteRoad(road: Road?) {
        checkToken()
        incorsoModal.dismiss()
        progressBar.visibility = View.VISIBLE
        Networking.create().deleteBook(road!!.id.toString(), token).enqueue(object: Callback<Gson>{
            override fun onResponse(call: Call<Gson>, response: Response<Gson>) {

                progressBar.visibility = View.GONE

                if(response.isSuccessful){

                    getCorseAttive()

                    Toast.makeText(rootView.context, "Prenotazione eliminata correttamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(rootView.context, "Errore nell'eliminazione", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Gson>, t: Throwable) {

                progressBar.visibility = View.GONE

                Toast.makeText(rootView.context, "Errore di rete", Toast.LENGTH_SHORT).show()
            }

        })
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
                            animate(16f)
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

    private fun animate(value: Float) {
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    latitude,
                    longitude
                ), value
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

    fun getVeichle(callback: (Boolean) -> Unit) {
        progressBar.visibility = View.VISIBLE
        Networking.create().getVeichle(token).enqueue(object: Callback<Networking.ResponseVeichle> {
            override fun onResponse(call: Call<Networking.ResponseVeichle>, response: Response<Networking.ResponseVeichle>) {
                progressBar.visibility = View.GONE
                if(response.isSuccessful){
                    veichleArray = response.body()?.data!!
                } else {
                    Toast.makeText(rootView.context, "Errore di rete", Toast.LENGTH_SHORT).show()
                }
                callback(response.isSuccessful)

            }

            override fun onFailure(call: Call<Networking.ResponseVeichle>, t: Throwable) {
                callback(false)
                Toast.makeText(rootView.context, "Errore di rete", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE

            }

        })

    }

    fun callBook() {
        getVeichle {
            if(it){
                Log.e("callBook", "OK ${veichleArray.size}")
                startFragmentMain(BookFragment(this))
                removeMark()
            } else {
                Log.e("callBook", "NON OK")
            }
        }



    }

    fun startFragmentMain(fragment: Fragment) {

        requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in,
            R.anim.slide_in,
            R.anim.slide_out,
            R.anim.slide_out
        ).addToBackStack(null).add(R.id.drawer_layout, fragment).commit()
    }

    fun changeDateAndPosition(road: Road?) {

        val bookDialog = Dialog(rootView.context, R.style.Theme_Palermo2Go)
        bookDialog.setContentView(R.layout.time_dialog)
        bookDialog.window?.setBackgroundDrawable(ColorDrawable(rootView.context.resources.getColor(R.color.lightTrasparent)))
        val number = bookDialog.findViewById<TextInputEditText>(R.id.number)
        number.visibility = View.GONE
        val destinazioneText = bookDialog.findViewById<TextInputEditText>(R.id.destinazioneText)
        val prenotaButton = bookDialog.findViewById<Button>(R.id.prenotaButton)
        var destCheck = false
        prenotaButton.text = "Cambia destinazione"



        number.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                //
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if(s!= null) {
                    if(s.length > 3) {
                        number.setText(s[0].toString() + s[1].toString() + s[2].toString())
                        Toast.makeText(rootView.context, "Devi inserire un valore che va da 0 a 1440(Un Giorno)", Toast.LENGTH_SHORT).show()
                    } else if(s.isNotEmpty()){
                        if(s.toString().toInt() > 1440 || s.toString().toInt() < 0) {
                            number.setText(s[0].toString())
                            Toast.makeText(rootView.context, "Devi inserire un valore che va da 0 a 1440(Un giorno)", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }

            override fun afterTextChanged(s: Editable?) {
                //
            }
        })

        prenotaButton.setOnClickListener {



            destCheck = !destinazioneText.text.toString().isNullOrEmpty()

          if(destinazioneText.text.toString() == "") {
                Toast.makeText(rootView.context, "Inserisci una via valida", Toast.LENGTH_SHORT).show()
            } else if(!destCheck){
                Toast.makeText(rootView.context, "Inserisci una via valida", Toast.LENGTH_SHORT).show()
            } else {

                val start_date = road!!.start_ride_date

                val minutes = null

                val veichleId = road?.id

                val with_driver =road.with_driver

                val ride_destination = destinazioneText.text.toString()

                val positionIndex = null

                val store = null

                val start_location = road.start_location

                val isExpress = road.is_express

              progressBar.visibility = View.VISIBLE
                Networking.create().changeDestinationHour(road!!.id.toString(),
                    body = Networking.ChangeDestBody(ride_destination),
                    token).enqueue(object: retrofit2.Callback<Gson>{
                    override fun onResponse(call: Call<Gson>, response: Response<Gson>) {
                        progressBar.visibility = View.GONE
                        if(response.isSuccessful) {
                            Log.e("confirmBook", response.isSuccessful.toString())
                            getCorseAttive()
                            bookDialog.dismiss()
                            Toast.makeText(rootView.context, "Destinazione cambiata con successo", Toast.LENGTH_SHORT).show()
                            if (sharedPreferences.getBoolean("inRoad", false)) {
                                getCorsaInCorsoForButtonChange()
                            }

                        }
                    }

                    override fun onFailure(call: Call<Gson>, t: Throwable) {
                        Log.e("confirmBook", t.localizedMessage)
                        progressBar.visibility = View.GONE
                    }

                })

            }
        }
        bookDialog.setCanceledOnTouchOutside(true)
        bookDialog.window!!.setWindowAnimations(R.style.DialogNoAnimation)

        bookDialog.show()

    }

}