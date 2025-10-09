package com.reminder.data.remote

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.FirebaseNetworkException
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
            logError("getting reminder $id", e)
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
            logError("upserting reminder ${reminder.id}", e)
            Result.failure(createUserFriendlyException(e))
        }
    }

    override suspend fun deleteReminder(id: Long): Result<Unit> {
        val collection = getRemindersCollection()
            ?: return Result.failure(Exception("사용자 미인증"))

        return try {
            collection.document(id.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            logError("deleting reminder $id", e)
            Result.failure(createUserFriendlyException(e))
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
            logError("uploading all reminders", e)
            Result.failure(createUserFriendlyException(e))
        }
    }

    /**
     * 에러를 로깅하고 에러 타입을 구분합니다
     */
    private fun logError(operation: String, error: Exception) {
        val errorType = when (error) {
            is FirebaseNetworkException -> "네트워크 오류"
            is FirebaseFirestoreException -> "Firestore 오류"
            else -> "알 수 없는 오류"
        }
        Log.e(TAG, "[$errorType] Error $operation: ${error.message}", error)
    }

    /**
     * 사용자 친화적인 예외 메시지를 생성합니다
     */
    private fun createUserFriendlyException(error: Exception): Exception {
        return when (error) {
            is FirebaseNetworkException -> {
                Exception("네트워크 연결을 확인해주세요. 오프라인 상태에서는 데이터가 로컬에만 저장됩니다.", error)
            }
            is FirebaseFirestoreException -> {
                when (error.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                        Exception("데이터 접근 권한이 없습니다. 다시 로그인해주세요.", error)
                    }
                    FirebaseFirestoreException.Code.UNAVAILABLE -> {
                        Exception("서버에 일시적으로 연결할 수 없습니다. 잠시 후 다시 시도해주세요.", error)
                    }
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                        Exception("요청 시간이 초과되었습니다. 네트워크 연결을 확인해주세요.", error)
                    }
                    else -> {
                        Exception("데이터 동기화 중 오류가 발생했습니다: ${error.code}", error)
                    }
                }
            }
            else -> {
                Exception("알 수 없는 오류가 발생했습니다: ${error.message}", error)
            }
        }
    }
}
