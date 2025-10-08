package com.reminder.data.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.reminder.auth.AuthManager
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

/**
 * Firestore 통합 테스트
 *
 * 실제 Firebase Firestore와 통신하여 데이터 동기화를 검증
 */
@RunWith(AndroidJUnit4::class)
class FirestoreDataSourceTest {

    private lateinit var authManager: AuthManager
    private lateinit var dataSource: FirestoreDataSource
    private lateinit var firestore: FirebaseFirestore
    private lateinit var testUserId: String

    private val testReminder = ReminderEntity(
        id = System.currentTimeMillis(), // 고유 ID 생성
        title = "Firestore 테스트",
        description = "통합 테스트 리마인더",
        dueDateTime = LocalDateTime.now().plusDays(1),
        priority = Priority.HIGH,
        category = "테스트",
        isCompleted = false,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @Before
    fun setup() {
        runBlocking {
            // Firebase 초기화
            FirebaseApp.initializeApp(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext)

            // AuthManager 초기화 및 익명 로그인
            authManager = AuthManager()
            authManager.signInAnonymously()

            // userId가 설정될 때까지 대기
            var attempts = 0
            while (authManager.userId == null && attempts < 10) {
                kotlinx.coroutines.delay(500)
                attempts++
            }

            testUserId = authManager.userId ?: throw IllegalStateException("사용자 인증 실패")

            // FirestoreDataSource 초기화
            dataSource = FirestoreDataSource(authManager)
            firestore = FirebaseFirestore.getInstance()
        }
    }

    @After
    fun teardown() {
        // 테스트 데이터 정리
        runBlocking {
            try {
                val collection = firestore.collection("users")
                    .document(testUserId)
                    .collection("reminders")

                collection.document(testReminder.id.toString()).delete().await()
            } catch (e: Exception) {
                // 정리 실패는 무시
            }
        }
    }

    @Test
    fun 리마인더를_Firestore에_저장하고_조회한다() = runTest {
        // When: 리마인더 저장
        val result = dataSource.upsertReminder(testReminder)

        // Then: 저장 성공
        assertTrue(result.isSuccess)

        // When: 저장된 리마인더 조회
        val retrieved = dataSource.getReminderById(testReminder.id)

        // Then: 조회 성공 및 데이터 일치
        assertNotNull(retrieved)
        assertEquals(testReminder.title, retrieved?.title)
        assertEquals(testReminder.description, retrieved?.description)
        assertEquals(testReminder.priority, retrieved?.priority)
        assertEquals(testReminder.category, retrieved?.category)
    }

    @Test
    fun 모든_리마인더를_실시간으로_가져온다() = runTest {
        // Given: 리마인더 저장
        dataSource.upsertReminder(testReminder)
        kotlinx.coroutines.delay(1000) // Firestore 반영 대기

        // When: 실시간 Flow로 조회
        val reminders = dataSource.getAllReminders().first()

        // Then: 저장한 리마인더 포함
        assertTrue(reminders.any { it.id == testReminder.id })
    }

    @Test
    fun 리마인더를_Firestore에서_삭제한다() = runTest {
        // Given: 리마인더 저장
        dataSource.upsertReminder(testReminder)
        kotlinx.coroutines.delay(500)

        // When: 삭제
        val deleteResult = dataSource.deleteReminder(testReminder.id)

        // Then: 삭제 성공
        assertTrue(deleteResult.isSuccess)

        // When: 삭제된 리마인더 조회
        val retrieved = dataSource.getReminderById(testReminder.id)

        // Then: 조회 실패 (null 반환)
        assertNull(retrieved)
    }

    @Test
    fun 여러_리마인더를_일괄_업로드한다() = runTest {
        // Given: 여러 리마인더 생성
        val reminders = listOf(
            testReminder,
            testReminder.copy(
                id = testReminder.id + 1,
                title = "두 번째 테스트"
            ),
            testReminder.copy(
                id = testReminder.id + 2,
                title = "세 번째 테스트"
            )
        )

        // When: 일괄 업로드
        val result = dataSource.uploadAll(reminders)

        // Then: 업로드 성공
        assertTrue(result.isSuccess)

        // When: 업로드된 리마인더들 조회
        kotlinx.coroutines.delay(1000)
        val retrieved = dataSource.getAllReminders().first()

        // Then: 모든 리마인더 존재
        assertTrue(retrieved.any { it.id == testReminder.id })
        assertTrue(retrieved.any { it.id == testReminder.id + 1 })
        assertTrue(retrieved.any { it.id == testReminder.id + 2 })

        // Cleanup: 추가된 테스트 데이터 삭제
        dataSource.deleteReminder(testReminder.id + 1)
        dataSource.deleteReminder(testReminder.id + 2)
    }

    @Test
    fun 수정된_리마인더만_조회한다() = runTest {
        // Given: 리마인더 저장
        dataSource.upsertReminder(testReminder)
        kotlinx.coroutines.delay(500)

        val baselineTime = System.currentTimeMillis()
        kotlinx.coroutines.delay(500)

        // When: 리마인더 수정
        val updated = testReminder.copy(
            title = "수정된 제목",
            updatedAt = LocalDateTime.now()
        )
        dataSource.upsertReminder(updated)
        kotlinx.coroutines.delay(500)

        // When: 특정 시간 이후 수정된 리마인더 조회
        val modifiedReminders = dataSource.getRemindersModifiedAfter(baselineTime)

        // Then: 수정된 리마인더 포함
        assertTrue(modifiedReminders.any { it.id == testReminder.id && it.title == "수정된 제목" })
    }
}
