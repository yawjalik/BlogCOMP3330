package com.example.blogcomp3330

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.storage.storage

class PostsAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    private val storage = Firebase.storage

    /**
     * Provides a reference to the views for each data item
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val author: TextView = itemView.findViewById(R.id.postAuthor)
        val title: TextView = itemView.findViewById(R.id.postTitle)
        val content: TextView = itemView.findViewById(R.id.postContent)
        val date: TextView = itemView.findViewById(R.id.postDate)
        val image: ImageView = itemView.findViewById(R.id.postImage)
    }

    /**
     * Creates new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false)
        return ViewHolder(view)
    }

    /**
     * Binds the data to the TextView in each row
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        holder.author.text = post.author
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
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }

    /**
     * Returns the number of items in the list
     */
    override fun getItemCount(): Int {
        return posts.size
    }
}