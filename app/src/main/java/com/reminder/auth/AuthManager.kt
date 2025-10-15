package com.reminder.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val userId: String?
        get() = currentUser?.uid

    val isSignedIn: Boolean
        get() = currentUser != null

    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("익명 로그인 실패: 사용자 정보 없음"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureSignedIn(): Result<FirebaseUser> {
        return currentUser?.let { user ->
            Result.success(user)
        } ?: signInAnonymously()
    }

    fun signOut() {
        auth.signOut()
    }
}
