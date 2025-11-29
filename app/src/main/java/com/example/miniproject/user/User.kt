package com.example.miniproject.user

import com.google.firebase.firestore.DocumentId

data class User(

    @DocumentId
    val id: String = "",
    val email: String = "",
    val name: String = ""

)


