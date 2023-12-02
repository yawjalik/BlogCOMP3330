package com.example.blogcomp3330

import com.google.firebase.firestore.DocumentId

data class Post(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val author: String = "",
    val title: String = "",
    val content: String = "",
    val date: String = "",
    val image: String = ""
)