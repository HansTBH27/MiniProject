package com.example.miniproject.auth

import android.R.attr.name
import com.example.miniproject.user.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class AuthRepository{

    private val usersCollection = FirebaseManager.firestore.collection("users")
//    suspend fun signIn(email: String, password: String): AuthResult{
//        val authResult = FirebaseManager.auth.signInWithEmailAndPassword(email, password).await()
//        val firebaseUser: FirebaseUser? = authResult.user
//        if(firebaseUser != null){
//            val newUser = User(id = firebaseUser.uid, email = email, name = name)
//            usersCollection.document(firebaseUser.uid).set(newUser).await()
//        }
//        return authResult
//    }

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
                email = email,
                name = name
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

    fun signOut(){
        FirebaseManager.auth.signOut()
    }
}

    val authRepository = AuthRepository()


