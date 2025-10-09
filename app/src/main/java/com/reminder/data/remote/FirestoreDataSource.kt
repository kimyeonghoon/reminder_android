package com.reminder.data.remote

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.reminder.auth.AuthManager
import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreDataSource(
    private val authManager: AuthManager
) : RemoteDataSource {

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "FirestoreDataSource"

    private fun getRemindersCollection() = authManager.userId?.let { userId ->
        db.collection("users").document(userId).collection("reminders")
    }

    override fun getAllReminders(): Flow<List<ReminderEntity>> = callbackFlow {
        val collection = getRemindersCollection()
        if (collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Listen failed", error)
                trySend(emptyList())
                return@addSnapshotListener
            }

            val reminders = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(FirestoreReminder::class.java)?.toEntity()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing document ${doc.id}", e)
                    null
                }
            } ?: emptyList()

            trySend(reminders)
        }

        awaitClose { listener.remove() }
    }

    override suspend fun getReminderById(id: Long): ReminderEntity? {
        val collection = getRemindersCollection() ?: return null

        return try {
            val doc = collection.document(id.toString()).get().await()
            doc.toObject(FirestoreReminder::class.java)?.toEntity()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting reminder $id", e)
            null
        }
    }

    override suspend fun upsertReminder(reminder: ReminderEntity): Result<Unit> {
        val collection = getRemindersCollection()
            ?: return Result.failure(Exception("사용자 미인증"))

        return try {
            val firestoreReminder = FirestoreReminder.fromEntity(reminder)
            collection.document(reminder.id.toString())
                .set(firestoreReminder)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error upserting reminder ${reminder.id}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteReminder(id: Long): Result<Unit> {
        val collection = getRemindersCollection()
            ?: return Result.failure(Exception("사용자 미인증"))

        return try {
            collection.document(id.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting reminder $id", e)
            Result.failure(e)
        }
    }

    override suspend fun getRemindersModifiedAfter(timestamp: Long): List<ReminderEntity> {
        val collection = getRemindersCollection() ?: return emptyList()

        return try {
            val timestampObj = Timestamp(timestamp / 1000, ((timestamp % 1000) * 1000000).toInt())
            val snapshot = collection
                .whereGreaterThan("updatedAt", timestampObj)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(FirestoreReminder::class.java)?.toEntity()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing document ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting modified reminders", e)
            emptyList()
        }
    }

    override suspend fun uploadAll(reminders: List<ReminderEntity>): Result<Unit> {
        val collection = getRemindersCollection()
            ?: return Result.failure(Exception("사용자 미인증"))

        return try {
            val batch = db.batch()
            reminders.forEach { reminder ->
                val docRef = collection.document(reminder.id.toString())
                val firestoreReminder = FirestoreReminder.fromEntity(reminder)
                batch.set(docRef, firestoreReminder)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading all reminders", e)
            Result.failure(e)
        }
    }
}
