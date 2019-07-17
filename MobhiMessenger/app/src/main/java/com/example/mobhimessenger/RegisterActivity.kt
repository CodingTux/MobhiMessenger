package com.example.mobhimessenger

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        reg_button.setOnClickListener {
            performRegister()
        }

        loginHere_text.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        profilePicture_register.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data!=null){
            Log.d("RegisterActivity", "Photo was selected $data")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            //val bitmapDrawable = BitmapDrawable(bitmap)
            select_circularImage_register.setImageBitmap(bitmap)
            profilePicture_register.alpha = 0f
            profilePicture_text_register.setText(null)
        }
    }

    private fun performRegister(){
        val auth = FirebaseAuth.getInstance()
        val email = email_reg.text.toString()
        val username = username_reg.text.toString()
        val password = password_reg.text.toString()
        Log.d("RegisterActivity", "Username is: " + username)
        Log.d("RegisterActivity", "Email is: " + email)
        Log.d("RegisterActivity", "Password is: " + password)

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
        }else {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("RegisterActivity", "createUserWithEmail:success")
                        uploadImageToFirebase()
                        val user = auth.currentUser
                        //  updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            this, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        // updateUI(null)
                    }

                    // ...
                }
        }
    }

    private fun uploadImageToFirebase(){

        //https://firebasestorage.googleapis.com/v0/b/mobhi-messenger.appspot.com/o/images%2Fuser.png?alt=media&token=a1e387bd-2a3b-44bd-a270-4fe0fa7c76b4

        if(selectedPhotoUri != null) {

            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "successfully uploaded dp at ${it.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener {
                        it.toString()
                        Log.d("RegisterActivity", "Download uri is ${it.toString()}")

                        saveUserToFirebaseDatabase(it.toString())
                    }
                }
        }else{
            saveUserToFirebaseDatabase()
        }
    }

    private fun saveUserToFirebaseDatabase(profilePicUrl: String = "https://firebasestorage.googleapis.com/v0/b/mobhi-messenger.appspot.com/o/images%2Fuser.png?alt=media&token=05658c43-012b-4da7-b8e3-3ee6234805b6"){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val user = User(uid, username_reg.text.toString(), profilePicUrl)
        val database = FirebaseDatabase.getInstance().getReference("/users/$uid")

        database.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "User registered to database successfully...")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "fAILURE in CREATING DATABASE: ${it.message}")
            }

    }
}

@Parcelize
class User(val uid: String, val username: String, val profileImageUrl: String):Parcelable{
    constructor(): this("", "", "")
}