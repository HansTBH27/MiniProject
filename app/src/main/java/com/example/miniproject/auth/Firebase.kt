package com.example.miniproject.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseManager{
    val auth: FirebaseAuth by lazy{
        Firebase.auth
    }

    val firestore: FirebaseFirestore by lazy{
        Firebase.firestore
    }
}