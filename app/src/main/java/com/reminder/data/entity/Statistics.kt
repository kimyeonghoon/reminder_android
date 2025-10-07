package com.reminder.data.entity

/**
 * 통계 데이터 모델
 */
data class Statistics(
    // 전체 통계
    val totalReminders: Int = 0,
    val completedReminders: Int = 0,
    val pendingReminders: Int = 0,
    val completionRate: Float = 0f,

    // 우선순위별 분포
    val highPriorityCount: Int = 0,
    val mediumPriorityCount: Int = 0,
    val lowPriorityCount: Int = 0,

    // 카테고리별 분포
    val categoryDistribution: Map<String, Int> = emptyMap(),

    // 시간별 통계
    val weeklyCompleted: List<Int> = List(7) { 0 },  // 최근 7일
    val monthlyCompleted: List<Int> = List(30) { 0 }  // 최근 30일
)
