package com.reminder.data.entity

enum class FilterCategory {
    ALL,
    WORK,
    PERSONAL,
    SHOPPING,
    HEALTH,
    OTHER
}

enum class FilterPriority {
    ALL,
    HIGH,
    MEDIUM,
    LOW
}

enum class FilterDate {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    OVERDUE
}

enum class SortOption {
    BY_DATE_ASC,
    BY_DATE_DESC,
    BY_PRIORITY_HIGH_FIRST,
    BY_PRIORITY_LOW_FIRST,
    BY_TITLE_ASC,
    BY_TITLE_DESC,
    BY_CREATED_ASC,
    BY_CREATED_DESC
}
