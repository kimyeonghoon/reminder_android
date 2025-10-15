package com.reminder.viewmodel

import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.dao.ReminderImageDao
import com.reminder.data.entity.ReminderImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

/**
 * AttachmentViewModel 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AttachmentViewModelTest {

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var reminderImageDao: ReminderImageDao

    @Mock
    private lateinit var analyticsHelper: AnalyticsHelper

    private lateinit var viewModel: AttachmentViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AttachmentViewModel(reminderImageDao, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getImages returns flow from dao`() {
        // given: 리마인더 ID와 이미지 리스트
        val reminderId = 1L
        val images = listOf(
            ReminderImage(id = 1, reminderId = reminderId, imageUri = "content://image1.jpg"),
            ReminderImage(id = 2, reminderId = reminderId, imageUri = "content://image2.jpg")
        )
        whenever(reminderImageDao.getImagesByReminderId(reminderId)).thenReturn(flowOf(images))

        // when: getImages 호출
        viewModel.getImages(reminderId)

        // then: dao 메서드가 호출됨
        verify(reminderImageDao).getImagesByReminderId(reminderId)
    }

    @Test
    fun `addImage inserts new image`() = runTest(testDispatcher) {
        // given: reminderId와 imageUri
        val reminderId = 1L
        val imageUri = "content://image.jpg"

        // when: addImage 호출
        viewModel.addImage(reminderId, imageUri)
        testScheduler.advanceUntilIdle()

        // then: dao insert 메서드 호출 및 analytics 로깅
        val captor = argumentCaptor<ReminderImage>()
        verify(reminderImageDao).insert(captor.capture())
        val insertedImage = captor.firstValue

        assert(insertedImage.reminderId == reminderId)
        assert(insertedImage.imageUri == imageUri)
        verify(analyticsHelper).logImageAttached()
    }

    @Test
    fun `deleteImage deletes image from dao`() = runTest(testDispatcher) {
        // given: 이미지
        val image = ReminderImage(id = 1, reminderId = 1L, imageUri = "content://image.jpg")

        // when: deleteImage 호출
        viewModel.deleteImage(image)
        testScheduler.advanceUntilIdle()

        // then: dao delete 메서드 호출
        verify(reminderImageDao).delete(image)
    }
}
