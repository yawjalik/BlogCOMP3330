package com.example.blogcomp3330

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class SignUpActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Set sign up behavior
        val signUpButton: Button = findViewById(R.id.signUpButton)
        signUpButton.setOnClickListener {
            val firstNameText = findViewById<EditText>(R.id.signUpFirstName).text.toString()
            val lastNameText = findViewById<EditText>(R.id.signUpLastName).text.toString()
            val emailText = findViewById<EditText>(R.id.signUpEmail).text.toString()
            val passwordText = findViewById<EditText>(R.id.signUpPassword).text.toString()
            // You can add validation here

            // Create user in Firebase Auth
            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Save user data in Firestore
                        val currentUserId = auth.currentUser?.uid
                        val userDocumentRef = db.collection("User").document(currentUserId!!)
                        userDocumentRef.set(User(firstNameText, lastNameText, emailText))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                                // Redirect to MainActivity
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // If sign up fails, display a message to the user.
                        Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Set login behavior
        val loginText: TextView = findViewById(R.id.loginText)
        loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}