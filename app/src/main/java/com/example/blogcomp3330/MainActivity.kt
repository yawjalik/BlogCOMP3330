package com.example.blogcomp3330

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.toObject

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private var posts = ArrayList<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Instantiate Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Get current user
        val currentUser = auth.currentUser

        // If user is not logged in, redirect to LoginActivity
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Get posts from Firestore
        val postsRef = db.collection("Post")
        postsRef.get().addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                val noPostsTextView: TextView = findViewById(R.id.noPostsTextView)
                noPostsTextView.visibility = View.VISIBLE
                return@addOnSuccessListener
            }

            for (document in documents) {
                Log.d("MainActivity", "${document.id} => ${document.data}")
                val post = document.toObject<Post>()
                posts.add(post)
            }

            // Set up RecyclerView
            val recyclerView: RecyclerView = findViewById(R.id.postsRecyclerView)
            recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
            recyclerView.adapter = PostsAdapter(posts)
        }
    }
}

data class Post(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val date: String = "",
    val image: String = ""
)

class PostsAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    private val storage = Firebase.storage

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.postTitle)
        val content: TextView = itemView.findViewById(R.id.postContent)
        val date: TextView = itemView.findViewById(R.id.postDate)
        val image: ImageView = itemView.findViewById(R.id.postImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        holder.title.text = post.title
        holder.date.text = post.date
        holder.content.text = post.content

        // Download image
        if (post.image != "") {
            val imageRef = storage.reference.child(post.image)
            val ONE_MEGABYTE: Long = 1024 * 1024
            imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.image.setImageBitmap(bitmap)
            }.addOnFailureListener {
                Log.e("MainActivity", "Failed to get image")
            }
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}
