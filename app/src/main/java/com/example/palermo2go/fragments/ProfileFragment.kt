package com.example.palermo2go.fragments

import android.content.ContentValues
import android.content.Context
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
import okhttp3.MediaType
import java.io.ByteArrayOutputStream
import java.io.File

import okhttp3.RequestBody
import android.os.Environment
import android.widget.Toast
import okhttp3.MultipartBody
import java.io.FileOutputStream
import java.lang.Exception


class ProfileFragment(
    val userData: Networking.UserData?,
    val token: String,
    val profileImage: ImageView,
    val mapsFragment: MapsFragment
) : Fragment() {

    private val OPERATION_CHOOSE_PHOTO: Int = 999
    lateinit var imageView: ImageView
    lateinit var name: TextView
    lateinit var surname: TextView
    lateinit var patenteButton: Button
    lateinit var v: View
    lateinit var closeButton: Button
    lateinit var cambiaPassword: Button
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

        cambiaPassword.setOnClickListener {
            requireActivity().onBackPressed()
            mapsFragment.openPasswordModal()
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
        cambiaPassword = v.findViewById(R.id.cambiaPassword)
        closeButton = v.findViewById(R.id.closeButton)
        imageView = v.findViewById(R.id.imageView)
        name = v.findViewById(R.id.name)
        surname = v.findViewById(R.id.surname)
        patenteButton = v.findViewById(R.id.patenteButton)

        name.text = userData?.firstName

        surname.text = userData?.lastName

        if(!userData?.drivingLicence.isNullOrEmpty()) {
            patenteButton.visibility = View.GONE
        }
        if(!userData?.propic.isNullOrEmpty()) {
            loadImage()
        } else {
            imageView.setImageDrawable(v.resources.getDrawable(R.drawable.account, null))
        }
    }

    private fun loadImage() {
        imageView.setImageDrawable(profileImage.drawable)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == -1){

            Log.e("FILEEX", requestCode.toString() + " ${data!=null}")
            //chiama api
            if(data!= null || isPick){
                if(isPick) {

                   var bitmap = MediaStore.Images.Media.getBitmap(v.context.contentResolver, imageUri)

                    val file = bitmapToFile(v.context, bitmap, "drivingLicence.jpg")

                    patenteButton.visibility = View.GONE
                    userData!!.drivingLicence = "true"

                    //val file = File(imageUri!!.path)


                    Log.e("FILEEX", file!!.absolutePath + " " + imageUri)

                    val fbody = RequestBody.create(
                        MediaType.parse("image/*"),
                        file
                    )


                    val multipart = MultipartBody.Part.createFormData("driving_licence", file!!.name, fbody)

                    Networking.create().uploadLicence(multipart, token).enqueue(object : Callback<Gson>{
                        override fun onResponse(call: Call<Gson>, response: Response<Gson>) {
                            if(response.isSuccessful){
                                Toast.makeText(v.context, "Patente caricata correttamente", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(v.context, "Errore di rete", Toast.LENGTH_SHORT).show()

                            }

                        }

                        override fun onFailure(call: Call<Gson>, t: Throwable) {
                            Toast.makeText(v.context, "Errore di rete", Toast.LENGTH_SHORT).show()
                        }

                    })


                } else {


                    var bitmap = MediaStore.Images.Media.getBitmap(v.context.contentResolver, data!!.data)

                    imageView.setImageBitmap(bitmap)
                    profileImage.setImageBitmap(bitmap)


                    val file = bitmapToFile(v.context, bitmap, "propic.jpg")

                    //val file = File(imageUri!!.path)


                    Log.e("FILEEX", file!!.absolutePath + " " + imageUri)

                    val fbody = RequestBody.create(
                        MediaType.parse("image/*"),
                       file
                    )


                    val multipart = MultipartBody.Part.createFormData("propic", file!!.name, fbody)



                        Networking.create().updatePropic(multipart, token).enqueue(object : Callback<Gson>{
                            override fun onResponse(call: Call<Gson>, response: Response<Gson>) {
                                if(response.isSuccessful){
                                    Toast.makeText(v.context, "Immagine aggiornata correttamente", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(v.context, "Errore di rete", Toast.LENGTH_SHORT).show()

                                }

                            }

                            override fun onFailure(call: Call<Gson>, t: Throwable) {
                                Toast.makeText(v.context, "Errore di rete", Toast.LENGTH_SHORT).show()
                            }

                        })
                    }

                }
            }
        }


    fun bitmapToFile(
        context: Context?,
        bitmap: Bitmap,
        fileNameToSave: String
    ): File? {
        var file: File? = null
        return try {
            file = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + fileNameToSave
            )
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

}