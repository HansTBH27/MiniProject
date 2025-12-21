package com.example.miniproject.auth

import com.example.miniproject.user.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val usersCollection = FirebaseManager.firestore.collection("user")

    suspend fun signIn(email: String, password: String): AuthResult {
        return FirebaseManager.auth.signInWithEmailAndPassword(email, password).await()
    }
    
    /**
     * Signs in using displayId (ID) instead of email.
     * Finds the user by displayId, gets their email, then signs in.
     */
    suspend fun signInByDisplayId(displayId: String, password: String): AuthResult {
        // Find user by displayId
        val user = findUserByDisplayId(displayId)
            ?: throw Exception("User with ID $displayId not found")
        
        // Get the email from the user document
        val email = user.email
        if (email.isBlank()) {
            throw Exception("User email not found for ID $displayId")
        }
        
        // Sign in using email and password
        return FirebaseManager.auth.signInWithEmailAndPassword(email, password).await()
    }

    /**
     * Signs up a new user with email, password, name, role, and displayId.
     * The displayId will be formatted based on the role:
     * - Student: numeric only (e.g., "1001")
     * - Staff: "S" prefix + numbers (e.g., "S1001")
     */
    suspend fun signUp(email: String, password: String, name: String, role: String = "student", displayId: String): AuthResult {
        // Format displayId based on role
        val formattedDisplayId = when (role.lowercase()) {
            "staff" -> {
                // Staff format: "S" + numbers (e.g., "S1001")
                if (displayId.startsWith("S", ignoreCase = true)) {
                    displayId.uppercase()
                } else {
                    "S${displayId.uppercase()}"
                }
            }
            "student" -> {
                // Student format: numbers only (e.g., "1001")
                displayId.trim()
            }
            else -> {
                // Default format
                displayId.trim()
            }
        }
        
        // Check if displayId already exists
        val existingUserById = findUserByDisplayId(formattedDisplayId)
        if (existingUserById != null) {
            throw Exception("User with ID $formattedDisplayId already exists")
        }
        
        // Check if email already exists in Firestore
        try {
            val existingEmailQuery = usersCollection
                .whereEqualTo("email", email.lowercase())
                .limit(1)
                .get()
                .await()
            
            if (!existingEmailQuery.isEmpty) {
                throw Exception("User with email $email already exists")
            }
        } catch (e: Exception) {
            // If it's our custom exception, rethrow it
            if (e.message?.contains("already exists") == true) {
                throw e
            }
            // Otherwise, log but continue (query might fail for other reasons)
            println("Warning: Could not check email existence: ${e.message}")
        }
        
        // Create Firebase Auth user
        val authResult = FirebaseManager.auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
        
        if (firebaseUser != null) {
            val newUser = User(
                id = firebaseUser.uid,
                displayId = formattedDisplayId,
                email = email.lowercase(), // Store email in lowercase for consistency
                name = name,
                role = role.lowercase()
            )
            
            // Save to Firestore
            try {
                usersCollection.document(firebaseUser.uid).set(newUser).await()
                println("User created successfully in Firestore: ${firebaseUser.uid}, displayId: $formattedDisplayId")
            } catch (e: Exception) {
                println("Error saving user to Firestore: ${e.message}")
                // Try to delete the auth user if Firestore save fails
                try {
                    firebaseUser.delete().await()
                } catch (deleteError: Exception) {
                    println("Error deleting auth user after Firestore failure: ${deleteError.message}")
                }
                throw Exception("Failed to save user data: ${e.message}")
            }
        } else {
            throw Exception("Failed to create authentication user")
        }
        
        return authResult
    }

    /**
     * Creates a new user (staff or student) by admin.
     * This function creates both Firebase Auth account and Firestore document.
     */
    suspend fun createUser(
        email: String,
        password: String,
        username: String,
        role: String // "staff" or "student"
    ): Result<User> {
        return try {
            // Create Firebase Auth account
            val authResult = FirebaseManager.auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Generate display ID based on role
                val displayIdPrefix = when (role.lowercase()) {
                    "staff" -> "STF"
                    "student" -> "STU"
                    else -> "USR"
                }

                val newUser = User(
                    id = firebaseUser.uid,
                    displayId = "$displayIdPrefix-${firebaseUser.uid.take(6).uppercase()}",
                    email = email,
                    name = username,
                    role = role.lowercase()
                )

                // Save to Firestore
                usersCollection.document(firebaseUser.uid).set(newUser).await()

                Result.success(newUser)
            } else {
                Result.failure(Exception("Failed to create user account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves a single user's data by their unique Firebase Auth UID.
     */
    suspend fun getUserData(id: String): User? {
        val documentSnapshot = usersCollection.document(id).get().await()
        return documentSnapshot.toObject<User>()
    }

    /**
     * Finds a single user by their display ID.
     */
    suspend fun findUserByDisplayId(displayId: String): User? {
        val snapshot = usersCollection
            .whereEqualTo("displayId", displayId)
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject(User::class.java)
    }

    /**
     * Gets all users with a specific role (staff or student).
     */
    suspend fun getUsersByRole(role: String): List<User> {
        val snapshot = usersCollection
            .whereEqualTo("role", role.lowercase())
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toObject(User::class.java) }
    }

    /**
     * Updates user information in Firestore.
     * Note: This does NOT update Firebase Auth email/password.
     */
    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates a user's email in both Firebase Auth and Firestore.
     */
    suspend fun updateUserEmail(userId: String, newEmail: String): Result<Unit> {
        return try {
            // Update in Firebase Auth (requires user to be signed in)
            val currentUser = FirebaseManager.auth.currentUser
            if (currentUser?.uid == userId) {
                currentUser.updateEmail(newEmail).await()
            }

            // Update in Firestore
            usersCollection.document(userId).update("email", newEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a user from both Firebase Auth and Firestore.
     * Note: Deleting from Firebase Auth requires the user to be recently authenticated.
     */
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            // Delete from Firestore first
            usersCollection.document(userId).delete().await()

            // Note: Deleting from Firebase Auth can only be done by the user themselves
            // or through Firebase Admin SDK on the backend
            // For now, we just delete the Firestore document

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Searches users by display ID pattern (for search functionality).
     */
    suspend fun searchUsersByDisplayId(searchQuery: String): List<User> {
        return try {
            // Firestore doesn't support full-text search, so we need to get all users
            // and filter on the client side for partial matches
            val snapshot = usersCollection.get().await()
            val allUsers = snapshot.documents.mapNotNull { it.toObject(User::class.java) }

            allUsers.filter { user ->
                user.displayId.contains(searchQuery, ignoreCase = true)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Searches users by name pattern.
     */
    suspend fun searchUsersByName(searchQuery: String): List<User> {
        return try {
            val snapshot = usersCollection.get().await()
            val allUsers = snapshot.documents.mapNotNull { it.toObject(User::class.java) }

            allUsers.filter { user ->
                user.name.contains(searchQuery, ignoreCase = true)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Gets all users (for admin purposes).
     */
    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(User::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun signOut() {
        FirebaseManager.auth.signOut()
    }
}