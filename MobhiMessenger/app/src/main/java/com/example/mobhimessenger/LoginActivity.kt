package com.example.mobhimessenger

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        loginButton.setOnClickListener {
            performLogin()
        }

            registerHere_text.setOnClickListener {
                finish();
            }

    }

    private fun performLogin(){
        val auth = FirebaseAuth.getInstance()
        val email = loginEmail.text.toString()
        val password = passLogin.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){

                    Toast.makeText(this, "Welcome back $email", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                else
                    Toast.makeText(this, "Invalid email/pass", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.d("loginDebug", "Error in login due to ${it.message}")
            }
    }
}