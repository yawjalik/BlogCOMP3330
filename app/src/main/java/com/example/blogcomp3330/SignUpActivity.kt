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
            // Basic validation
            val firstNameText = findViewById<EditText>(R.id.signUpFirstName).text.toString()
            if (firstNameText.isEmpty()) {
                Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val lastNameText = findViewById<EditText>(R.id.signUpLastName).text.toString()
            if (lastNameText.isEmpty()) {
                Toast.makeText(this, "Please enter your last name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val emailText = findViewById<EditText>(R.id.signUpEmail).text.toString()
            if (!Regex("[^@]+@[^\\.]+\\..+").matches(emailText)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val passwordText = findViewById<EditText>(R.id.signUpPassword).text.toString()
            if (passwordText.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Create user
            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Save user data
                        val user = auth.currentUser
                        user?.let { firebaseUser ->
                            val currentUserUID = firebaseUser.uid
                            val userDocumentRef = db.collection("User").document(currentUserUID)
                            val userData = hashMapOf(
                                "firstName" to firstNameText,
                                "lastName" to lastNameText,
                            )
                            userDocumentRef.set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT)
                                        .show()
                                    // Redirect to MainActivity
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT)
                                        .show()
                                }
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