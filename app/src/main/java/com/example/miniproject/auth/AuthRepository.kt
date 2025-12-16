package com.example.miniproject.auth

import com.example.miniproject.user.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class AuthRepository{

    private val usersCollection = FirebaseManager.firestore.collection("users")

    suspend fun signIn(email: String, password: String): AuthResult {
        return FirebaseManager.auth.signInWithEmailAndPassword(email, password).await()
    }


    suspend fun signUp(email: String, password: String, name: String): AuthResult {
        // Step 1: Use the Auth tool to create the user
        val authResult = FirebaseManager.auth.createUserWithEmailAndPassword(email, password).await()

        // Step 2: Use the Firestore tool to create the user's data document
        val firebaseUser = authResult.user
        if (firebaseUser != null) {
            val newUser = User(
                id = firebaseUser.uid,
                displayId = "U-${firebaseUser.uid.take(6).uppercase()}", // Generate a display ID
                email = email,
                name = name,
                role = "user" // Set default role for new users
            )
            // Use the user's UID as the document ID for easy lookup
            usersCollection.document(firebaseUser.uid).set(newUser).await()
        }

        return authResult
    }

    suspend fun getUserData(id: String): User? {
      val documentSnapshot = usersCollection.document(id).get().await()
        return documentSnapshot.toObject<User>()
    }

    // Function to get all users, useful for admin panels
    suspend fun getAllUsers(): List<User> {
        val querySnapshot = usersCollection.get().await()
        return querySnapshot.documents.mapNotNull { it.toObject<User>() }
    }

    fun signOut(){
        FirebaseManager.auth.signOut()
    }
}
