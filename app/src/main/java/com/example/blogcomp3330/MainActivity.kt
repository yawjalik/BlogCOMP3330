package com.example.blogcomp3330

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private var posts = ArrayList<Post>()
    private lateinit var recyclerView: RecyclerView


    @RequiresApi(Build.VERSION_CODES.O)
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

        // Setup recycler view
        recyclerView = findViewById(R.id.postsRecyclerView)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recyclerView.adapter = PostsAdapter(posts)

        // Get posts from Firestore, order by date
        getPosts()

        // Set up create post
        val createPostButton: AppCompatImageButton = findViewById(R.id.createPostButton)
        createPostButton.setOnClickListener {
            val createPostTitle: EditText = findViewById(R.id.createPostTitle)
            val createPostContent: EditText = findViewById(R.id.createPostContent)

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

            // Create post
            val post = hashMapOf(
                "userId" to currentUser?.uid,
                "title" to title,
                "content" to content,
                "date" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )
            db.collection("Post").add(post).addOnSuccessListener {
                Toast.makeText(this, "Post created", Toast.LENGTH_SHORT).show()

                // Refetch data and update recycler view
                getPosts()

                // Clear input fields
                createPostTitle.text.clear()
                createPostContent.text.clear()
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
     * Fetch posts from Firestore and update recycler view
     */
    private fun getPosts() {
        posts.clear()
        val postsRef = db.collection("Post")
        postsRef.orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener { documents ->
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
