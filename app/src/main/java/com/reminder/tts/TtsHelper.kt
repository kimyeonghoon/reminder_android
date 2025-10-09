package com.reminder.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * TTS (Text-to-Speech) 헬퍼 클래스
 *
 * Android TextToSpeech API를 사용하여 텍스트를 음성으로 읽어줍니다.
 */
class TtsHelper(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    /**
     * TTS 초기화
     *
     * @param onSuccess 초기화 성공 시 콜백
     * @param onError 초기화 실패 시 콜백
     */
    fun initialize(
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.KOREAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // 한국어 지원 안 됨, 기본 언어(영어) 사용
                    textToSpeech?.setLanguage(Locale.getDefault())
                    Log.w(TAG, "Korean language not supported, using default language")
                }
                isInitialized = true
                onSuccess?.invoke()
                Log.d(TAG, "TTS initialized successfully")
            } else {
                isInitialized = false
                onError?.invoke("TTS initialization failed")
                Log.e(TAG, "TTS initialization failed with status: $status")
            }
        }
    }

    /**
     * 텍스트를 음성으로 읽기
     *
     * @param text 읽을 텍스트
     * @param utteranceId 발화 ID (선택사항)
     * @param onStart 읽기 시작 시 콜백 (선택사항)
     * @param onDone 읽기 완료 시 콜백 (선택사항)
     * @param onError 읽기 오류 시 콜백 (선택사항)
     */
    fun speak(
        text: String,
        utteranceId: String = "reminder_tts",
        onStart: (() -> Unit)? = null,
        onDone: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized, initializing now")
            initialize(
                onSuccess = {
                    speakInternal(text, utteranceId, onStart, onDone, onError)
                },
                onError = { error ->
                    onError?.invoke(error)
                }
            )
            return
        }

        speakInternal(text, utteranceId, onStart, onDone, onError)
    }

    /**
     * 내부적으로 텍스트를 음성으로 읽기
     */
    private fun speakInternal(
        text: String,
        utteranceId: String,
        onStart: (() -> Unit)?,
        onDone: (() -> Unit)?,
        onError: ((String) -> Unit)?
    ) {
        if (text.isBlank()) {
            Log.w(TAG, "Text is empty, nothing to speak")
            onError?.invoke("Text is empty")
            return
        }

        // UtteranceProgressListener 설정
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "TTS started: $utteranceId")
                onStart?.invoke()
            }

            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "TTS completed: $utteranceId")
                onDone?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "TTS error: $utteranceId")
                onError?.invoke("TTS error occurred")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e(TAG, "TTS error: $utteranceId, code: $errorCode")
                onError?.invoke("TTS error occurred with code: $errorCode")
            }
        })

        // 텍스트 읽기
        val result = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.ERROR) {
            Log.e(TAG, "Failed to speak text")
            onError?.invoke("Failed to speak text")
        }
    }

    /**
     * 리마인더 제목과 설명을 음성으로 읽기
     *
     * @param title 리마인더 제목
     * @param description 리마인더 설명 (선택사항)
     */
    fun speakReminder(
        title: String,
        description: String? = null,
        onStart: (() -> Unit)? = null,
        onDone: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        val textToSpeak = buildString {
            append("리마인더. ")
            append(title)
            if (!description.isNullOrBlank()) {
                append(". ")
                append(description)
            }
        }

        speak(textToSpeak, "reminder_notification", onStart, onDone, onError)
    }

    /**
     * 현재 읽기 중지
     */
    fun stop() {
        if (isInitialized) {
            textToSpeech?.stop()
            Log.d(TAG, "TTS stopped")
        }
    }

    /**
     * TTS가 현재 말하고 있는지 확인
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking ?: false
    }

    /**
     * TTS 리소스 해제
     */
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        isInitialized = false
        Log.d(TAG, "TTS shutdown")
    }

    companion object {
        private const val TAG = "TtsHelper"
    }
}
