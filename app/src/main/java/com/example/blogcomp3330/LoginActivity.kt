package com.example.blogcomp3330

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Get current user
        val currentUser = auth.currentUser

        // If user is already logged in, redirect to MainActivity
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Set login behavior
        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            val emailText: String = findViewById<EditText>(R.id.loginEmail).text.toString()
            val passwordText = findViewById<EditText>(R.id.loginPassword).text.toString()

            // Perform basic validation
            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Please check your credentials", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, launch MainActivity
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Set sign up behavior
        val signUpText: TextView = findViewById(R.id.signUpText)
        signUpText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}