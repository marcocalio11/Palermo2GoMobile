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
import android.widget.Toast
import com.example.palermo2go.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.w3c.dom.Text

class ProfileFragment : Fragment() {

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
        if(hasLoaded) {
            patenteButton.visibility = View.GONE
        }
        if(hasImage) {
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

                }
            }
        }
    }

}