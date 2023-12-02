package com.example.blogcomp3330

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
)
