package com.example.blogcomp3330

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private var posts = ArrayList<Post>()
    private var imageUri: Uri? = null
    private lateinit var recyclerView: RecyclerView
    private val imageSelector =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            val createPostImagePreview: ImageView = findViewById(R.id.createPostImagePreview)
            imageUri = uri
            createPostImagePreview.setImageURI(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get current user
        val currentUser = auth.currentUser

        // If user is not logged in, redirect to LoginActivity
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Get current user's name
        var name = ""
        db.collection("User").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject<User>()
                if (user != null) {
                    name = "${user.firstName} ${user.lastName}"
                }
            }.addOnFailureListener {
                Log.e("MainActivity", "Failed to get user")
            }

        // Setup recycler view
        recyclerView = findViewById(R.id.postsRecyclerView)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recyclerView.adapter = PostsAdapter(posts)

        // Get posts from Firestore, order by date
        getPosts()

        // Image selector
        val uploadImageButton: AppCompatButton = findViewById(R.id.uploadImageButton)
        uploadImageButton.setOnClickListener {
            imageSelector.launch(PickVisualMediaRequest())
        }

        // Set up create post
        val createPostButton: AppCompatImageButton = findViewById(R.id.createPostButton)
        createPostButton.setOnClickListener {
            val createPostTitle: EditText = findViewById(R.id.createPostTitle)
            val createPostContent: EditText = findViewById(R.id.createPostContent)
            val createPostImagePreview: ImageView = findViewById(R.id.createPostImagePreview)

            // Basic validation
            val title = createPostTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val content = createPostContent.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "Please enter content", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Upload image
            val imagePath = if (imageUri != null) "images/${currentUser.uid}/${System.currentTimeMillis()}.jpg" else ""
            if (imageUri != null) {
                val imageRef =
                    storage.reference.child(imagePath)
                imageRef.putFile(imageUri!!).addOnSuccessListener {
                    Log.d("MainActivity", "Image uploaded")
                }.addOnFailureListener {
                    Log.e("MainActivity", "Failed to upload image")
                }
            }

            // Create post
            val post = Post(
                userId = currentUser.uid,
                title = title,
                content = content,
                image = imagePath,
                author = name,
                date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )
            db.collection("Post").add(post).addOnSuccessListener {
                Toast.makeText(this, "Post created", Toast.LENGTH_SHORT).show()

                // Refetch data and update recycler view
                getPosts()

                // Clear input fields and image preview
                createPostTitle.text.clear()
                createPostContent.text.clear()
                imageUri = null
                createPostImagePreview.setImageResource(R.drawable.ic_launcher_foreground)
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to create post", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up logout button
        val logoutButton: AppCompatButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Fetch posts from Firestore sorted by date and update recycler view
     */
    private fun getPosts() {
        posts.clear()
        val postsRef = db.collection("Post")
        postsRef.orderBy("date", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { documents ->
                val noPostsTextView: TextView = findViewById(R.id.noPostsTextView)
                noPostsTextView.visibility = if (documents.isEmpty) View.VISIBLE else View.GONE

                for (document in documents) {
                    Log.d("MainActivity", "${document.id} => ${document.data}")
                    val post = document.toObject<Post>()
                    posts.add(post)
                }

                // Update recycler view
                recyclerView.adapter?.notifyDataSetChanged()
            }
    }
}
