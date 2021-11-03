package com.example.palermo2go.fragments

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.palermo2go.Networking
import com.example.palermo2go.R
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.graphics.Bitmap
import android.util.Base64
import androidx.core.net.toFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI


class ProfileFragment(val userData: Networking.UserData?, val token: String) : Fragment() {

    private val OPERATION_CHOOSE_PHOTO: Int = 999
    lateinit var imageView: ImageView
    lateinit var name: TextView
    lateinit var surname: TextView
    lateinit var patenteButton: Button
    lateinit var v: View
    lateinit var closeButton: Button
    var hasLoaded = false
    var hasImage = false
    var imageUri : Uri? = null
    var isPick = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_profile, container, false)

        findView()
        onClick()

        return v
    }


    private fun onClick() {
        closeButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        imageView.setOnClickListener {
            openGallery()
        }
        patenteButton.setOnClickListener {
            pickImage()
        }
    }

    private fun pickImage(){
        isPick = true
        imageUri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        Log.e("IMAGEURI", imageUri.toString())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, 1240)
    }

    private fun openGallery(){
        isPick = false
        val intent = Intent("android.intent.action.GET_CONTENT")
        imageUri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        intent.type = "image/*"
        startActivityForResult(intent, OPERATION_CHOOSE_PHOTO)
    }

    private fun findView() {
        closeButton = v.findViewById(R.id.closeButton)
        imageView = v.findViewById(R.id.imageView)
        name = v.findViewById(R.id.name)
        surname = v.findViewById(R.id.surname)
        patenteButton = v.findViewById(R.id.patenteButton)

        name.text = userData?.firstName

        surname.text = userData?.lastName

        if(userData?.drivingLicence == true) {
            patenteButton.visibility = View.GONE
        }
        if(!userData?.propic.isNullOrEmpty()) {
            loadImage()
        } else {
            imageView.setImageDrawable(v.resources.getDrawable(R.drawable.account, null))
        }
    }

    private fun loadImage() {
        TODO("Not yet implemented")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == -1){
            //chiama api
            if(data!= null){
                if(isPick) {

//                    var bitmap = MediaStore.Images.Media.getBitmap(v.context.contentResolver, data.data)


                } else {




                    var bitmap = MediaStore.Images.Media.getBitmap(v.context.contentResolver, data.data)

                    imageView.setImageBitmap(bitmap)


                    val file = File(URI(data.data!!.path))

                    Networking.create().updatePropic(Networking.DataPropic(file), token).enqueue(object : Callback<Gson>{
                        override fun onResponse(call: Call<Gson>, response: Response<Gson>) {

                        }

                        override fun onFailure(call: Call<Gson>, t: Throwable) {

                        }

                    })

                }
            }
        }
    }


    fun getStringImage(bmp: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes: ByteArray = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

}