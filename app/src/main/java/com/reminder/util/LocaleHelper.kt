package com.reminder.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.reminder.data.preferences.Language
import java.util.Locale

/**
 * v1.30.0: 언어 설정 유틸리티
 *
 * 앱의 언어를 동적으로 변경하는 헬퍼 클래스
 * 앱 재시작 없이 언어 변경 적용
 */
object LocaleHelper {

    /**
     * 주어진 언어로 Context 업데이트
     *
     * @param context 업데이트할 Context
     * @param language 적용할 언어 (Language.SYSTEM이면 시스템 기본값 사용)
     * @return 업데이트된 Context
     */
    fun updateLocale(context: Context, language: Language): Context {
        val locale = language.toLocale() ?: return context

        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

    /**
     * 현재 Context의 언어 가져오기
     *
     * @param context Context
     * @return 현재 언어 코드 (예: "ko", "en", "zh")
     */
    fun getCurrentLocale(context: Context): String {
        return context.resources.configuration.locales[0].language
    }

    /**
     * Context에서 Language enum으로 변환
     *
     * @param context Context
     * @return 현재 언어에 해당하는 Language enum
     */
    fun getLanguageFromContext(context: Context): Language {
        val currentLocale = getCurrentLocale(context)
        return when (currentLocale) {
            "ko" -> Language.KOREAN
            "en" -> Language.ENGLISH
            "zh" -> Language.CHINESE
            else -> Language.SYSTEM
        }
    }

    /**
     * 시스템 기본 언어 가져오기
     *
     * @return 시스템 기본 Locale
     */
    fun getSystemDefaultLocale(): Locale {
        return Configuration().locales[0]
    }
}
